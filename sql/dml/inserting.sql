/* count_clients      integer := 70000;
count_loan_rates   integer := 1000;
count_employees    integer := 5000;
count_loans        integer := 70000;
count_payments     integer := 150000;
years              integer := 3;
start_date_of_loan date    := CURRENT_DATE;
*/

/*
WITH existing_clients AS (SELECT id as client_id
                          FROM clients
                          ORDER BY RANDOM()),
     existing_loan_rates AS (SELECT id as loan_rate_id FROM loan_rates ORDER BY RANDOM()),
INTO loan_applications (client_id, loan_rate_id, is_approved, filing_date)
SELECT existing_clients.client_id,
       existing_loan_rates.id as loan_rate_id,
       RANDOM() < 0.8                         AS is_approved,
       (CURRENT_DATE - (FLOOR(RANDOM() * 365 * 3 + 1)) * INTERVAL '1 day')
FROM;*/

INSERT INTO loan_rates (min_term, max_term, min_amount, max_amount, percentage_rate, purpose)
SELECT FLOOR(RANDOM() * 12) + 1                        AS min_term,
       FLOOR(RANDOM() * 12) + 13                       AS max_term,
       ROUND((RANDOM() * 50000)::numeric, 2)::numeric  AS min_amount,
       ROUND((RANDOM() * 150000)::numeric, 2)::numeric AS max_amount,
       ROUND((RANDOM() * 20)::numeric, 2)              AS percentage_rate,
       'Цель_' || i                                    AS purpose
FROM generate_series(1, 100) AS i;


INSERT INTO loan_applications (client_id, loan_rate_id, is_approved, filing_date)
SELECT FLOOR(RANDOM() * 70000) + 1 AS client_id,
       FLOOR(RANDOM() * 100) + 1   AS loan_rate_id,
       RANDOM() < 0.8              AS is_approved,
       (CURRENT_DATE - (FLOOR(RANDOM() * 365 * 3 + 1)) * INTERVAL '1 day')
FROM generate_series(1, 70000);

do
$$
    declare
        start_date_of_loan int;
    begin
        WITH approved_loans AS (SELECT *
                                FROM loan_applications
                                         JOIN loan_rates ON loan_applications.loan_rate_id = loan_rates.id
                                WHERE is_approved is true)
        INSERT
        INTO loans (approved_application_id, amount, date_start, date_end, interest_start_date)
        SELECT FLOOR(RANDOM() * 70000) + 1                                                                  AS approved_application_id,
               (RANDOM() * (max_amount - min_amount) + min_amount) ::numeric                                AS amount,
               start_date_of_loan = CURRENT_DATE - (FLOOR(RANDOM() * 365 * 3) + 1) *
                                                   INTERVAL '1 day'                                         AS date_start,
               start_date_of_loan + FLOOR(RANDOM() * (max_term - min_term) + min_term) * INTERVAL '1 month' AS date_end,
               start_date_of_loan + floor(random() * 30 * 3) * INTERVAL '1 day'                             AS interest_start_date
        FROM approved_loans;
    end
$$;

INSERT INTO payment_channels (type_of_channel, commission_percentage, commission_fixed_amount)
VALUES ('Банковский счёт', 1, 0),
       ('Карта', 0.5, 50),
       ('Yandex.numeric', 0.75, 0),
       ('QIWI', 1.5, 0);


INSERT INTO payments (date_of_payment, loan_id, amount, payment_channel_id)
SELECT CURRENT_TIMESTAMP - (FLOOR(RANDOM() * 365 * 3) + 1) * INTERVAL '1 day' AS date_of_payment,
       FLOOR(RANDOM() * 70000) + 1                                            AS loan_id,
       ROUND(RANDOM() * 2000, 2)::numeric                                     AS amount,
       FLOOR(RANDOM() * 4) + 1                                                AS payment_channel_id
FROM generate_series(1, 150000);

WITH dataForInserting AS (SELECT percentage_rate,
                                 amount,
                                 amount * (lr.percentage_rate / 365) *
                                 (loans.date_start - loans.date_end) as interest_payment,
                                 (loans.date_start - loans.date_end) as period,
                                 date_end,
                                 date_start,
                                 loans.id                            as loan_id
                          from loans
                                   JOIN loan_applications la on la.id = loans.approved_application_id
                                   JOIN loan_rates lr
                                        on lr.id = la.loan_rate_id),
     planning_dates as (SELECT generate_series(date_start, date_end, '1 month')::date AS planning_date, loan_id
                        FROM dataForInserting),
     insert_to_payment_schedule as (
         INSERT
             INTO payment_schedule (loan_id, planning_date, principal_payment, interest_payment) -- todo: ask parents how see payment_schedule in reality???
                 SELECT dataForInserting.loan_id,
                        planning_date,
                        (amount / period)::numeric AS principal_payment,
                        interest_payment
                 FROM dataForInserting
                          JOIN planning_dates
                               on planning_dates.loan_id = dataForInserting.loan_id),

     insert_to_payments as (
         INSERT
             INTO payments (date_of_payment, loan_id, amount, payment_channel_id)
                 SELECT planning_date +
                        (case when ((-1) * random() < 0.2) then -1 else 1 end) * floor(random() * 365) *
                        INTERVAL '1 day'                       AS date_of_payment,
                        loan_id,
                        (principal_payment + interest_payment) as amount,
                        FLOOR(RANDOM() * 4) + 1                AS payment_channel_id
                 FROM payment_schedule)

/* INSERT
INTO interest_accrual (loan_id, accrual_date, percentage_amount, penalties)
SELECT FLOOR(RANDOM() * 70000) + 1                                            AS loan_id,
       CURRENT_TIMESTAMP - (FLOOR(RANDOM() * 365 * 3) + 1) * INTERVAL '1 day' AS accrual_date,
       ROUND(RANDOM() * 500, 2)::numeric                                        AS percentage_amount,
       ROUND(RANDOM() * 200, 2)::numeric                                        AS penalties
FROM payment_schedule; */
select *
from planning_dates;