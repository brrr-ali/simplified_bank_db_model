DO
$$
    DECLARE
        loan_rec                RECORD;
        current_date_v          DATE;
        previous_date           DATE;
        days_since_last_payment INT;
        principal_amount_v      NUMERIC;
        interest_payment_v      NUMERIC;
        principal_payment_v     NUMERIC;
        total_paid              NUMERIC;
    BEGIN
        FOR loan_rec IN
            SELECT loans.id, amount, date_start, date_end, interest_start_date, percentage_rate
            FROM loans
                     JOIN loan_applications la ON la.id = loans.approved_application_id
                     JOIN loan_rates lr ON lr.id = la.loan_rate_id
            LOOP
                principal_amount_v := loan_rec.amount;
                previous_date := loan_rec.date_start;
                days_since_last_payment := 0;
                total_paid := 0;

                principal_payment_v := loan_rec.amount * 1.0 / (case
                                                                    when (loan_rec.date_end - loan_rec.date_start) / 30 = 0
                                                                        then 1
                                                                    else (loan_rec.date_end - loan_rec.date_start) / 30 end);
                while (loan_rec.amount - total_paid > 0)
                    LOOP
                        current_date_v := previous_date + INTERVAL '1 month';
                        days_since_last_payment := current_date_v - previous_date;
                        interest_payment_v :=
                                (principal_amount_v * (loan_rec.percentage_rate / 100) / 365) * days_since_last_payment;
                        if (principal_amount_v < principal_payment_v) then
                            principal_payment_v := loan_rec.amount - total_paid;
                        end if;

                        INSERT INTO payment_schedule(loan_id, planning_date, principal_payment, interest_payment)
                        VALUES (loan_rec.id, current_date_v, principal_payment_v, interest_payment_v);
                        total_paid := total_paid + principal_payment_v;
                        principal_amount_v := principal_amount_v - principal_payment_v;
                        previous_date := current_date_v;
                    END LOOP;
            END LOOP;
    END;
$$;

SElECT loan_id, sum(principal_payment), sum(interest_payment), sum(DISTINCT amount)
from payment_schedule
         JOIN loans on payment_schedule.loan_id = loans.id
group by loan_id;