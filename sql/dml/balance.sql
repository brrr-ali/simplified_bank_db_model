DO
$$
    DECLARE
        current_loan_info   RECORD;
        dt                  RECORD;
        previous_date       DATE    := NULL;
        days_difference     INT;
        principal_balance_v NUMERIC := 0;
        principal_payment_v NUMERIC := 0;
        accrued_interest_v  NUMERIC := 0;
        penalty             NUMERIC := 0;
    BEGIN
         FOR current_loan_info IN (SELECT loans.id, date_start, date_end, percentage_rate, amount
                                  FROM loans
                                           JOIN public.loan_applications la ON la.id = loans.approved_application_id
                                           JOIN public.loan_rates lr ON lr.id = la.loan_rate_id)
            LOOP
                principal_balance_v := current_loan_info.amount;
                accrued_interest_v := 0;
                penalty := 0;

                INSERT INTO balance(date_of_balance, principal_balance, accrued_interest, penalty_amount, service_fees,
                                     loan_id)
                VALUES (current_loan_info.date_start, principal_balance_v, 0, 0, 0, current_loan_info.id);

                previous_date := current_loan_info.date_start;
                principal_payment_v :=
                        current_loan_info.amount / (case when (current_loan_info.date_end - current_loan_info.date_start)= 0 then 1 else  (current_loan_info.date_end - current_loan_info.date_start))  / 30;

                FOR dt IN (SELECT date_of_payment AS date_, amount
                           FROM payments
                           WHERE loan_id = current_loan_info.id
                             AND date_of_payment <= CURRENT_DATE
                           UNION ALL
                           SELECT planning_date AS date_, 0
                           FROM payment_schedule
                           WHERE loan_id = current_loan_info.id
                             AND planning_date <= CURRENT_DATE
                           ORDER BY date_)
                    LOOP
                        days_difference := EXTRACT(DAY FROM dt.date_ - previous_date);
                        accrued_interest_v := accrued_interest_v + (days_difference * principal_balance_v *
                                                                    current_loan_info.percentage_rate / 100 / 365);

                        IF (dt.amount > 0) THEN
                            IF (accrued_interest_v > 0) THEN
                                IF (dt.amount <= accrued_interest_v) THEN
                                    accrued_interest_v := accrued_interest_v - dt.amount;
                                ELSE
                                    IF (penalty > 0) THEN
                                        IF (penalty >= dt.amount) THEN
                                            penalty := penalty - dt.amount;
                                        ELSE
                                            principal_balance_v :=
                                                    principal_balance_v - (dt.amount - accrued_interest_v - penalty);
                                            penalty := 0;
                                            accrued_interest_v := 0;
                                        END IF;
                                    ELSE
                                        principal_balance_v := principal_balance_v - (dt.amount - accrued_interest_v);
                                        accrued_interest_v := 0;
                                    END IF;
                                END IF;
                            END IF;
                        ELSE
                            IF (accrued_interest_v > 0) THEN
                                penalty := penalty + 0.01 * principal_balance_v / 365 * days_difference;
                            END IF;
                        END IF;
                        previous_date := dt.date_;


                        INSERT INTO balance(date_of_balance, principal_balance, accrued_interest, penalty_amount,
                                             service_fees, loan_id)
                        VALUES (dt.date_, principal_balance_v, accrued_interest_v, penalty, 0, current_loan_info.id);


                        IF (accrued_interest_v > 0 OR penalty > 0) THEN
                            INSERT INTO interest_accrual(loan_id, accrual_date, percentage_amount, penalties)
                            VALUES (current_loan_info.id, dt.date_, accrued_interest_v, penalty);
                        END IF;
                    END LOOP;
            END LOOP;
    END
$$;


SELECT DISTINCT loan_id,
                max(date_of_balance) over (partition by loan_id),
                date_of_balance,
                penalty_amount,
                principal_balance,
                accrued_interest
from balance;

EXPLAIN SELECT DISTINCT loan_id, max(date_of_balance) over (partition by loan_id) as last_date
from balance;

SELECT *
from balance
WHERE principal_balance = 0;
