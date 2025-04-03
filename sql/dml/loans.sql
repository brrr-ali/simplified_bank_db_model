WITH approved_loans AS (SELECT *,
                               CURRENT_DATE - (FLOOR(RANDOM() * 365 * 3) + 1) * INTERVAL '1 day' as start_date_of_loan
                        FROM loan_applications
                                 JOIN loan_rates ON loan_applications.loan_rate_id = loan_rates.id
                        WHERE is_approved is true)
INSERT
INTO loans (approved_application_id, amount, date_start, date_end, interest_start_date)
SELECT FLOOR(RANDOM() * 70000) + 1                                                                  AS approved_application_id,
       (RANDOM() * (max_amount - min_amount) + min_amount) ::numeric                                AS amount,

       start_date_of_loan                                                                           AS date_start,
       start_date_of_loan + FLOOR(RANDOM() * (max_term - min_term) + min_term) * INTERVAL '1 month' AS date_end,
       start_date_of_loan + floor(random() * 29) * INTERVAL '1 day'                                 AS interest_start_date
FROM approved_loans;

SELECT *
from loans
where date_end < interest_start_date;