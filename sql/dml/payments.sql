/* INSERT INTO payments (date_of_payment, loan_id, amount, payment_channel_id)
SELECT LEAST(GREATEST(planning_date +
                      (case when (random() < 0.5) then -1 else 1 end) * floor(random() * 365) * INTERVAL '1 day',
                      date_start + INTERVAL '10 days'), CURRENT_DATE) AS date_of_payment,
       loan_id,
       (principal_payment + interest_payment)                         as amount,
       FLOOR(RANDOM() * 4) + 1                                        AS payment_channel_id

FROM payment_schedule
         JOIN loans on payment_schedule.loan_id = loans.id
WHERE (interest_payment + principal_payment) > 0;


-- SELECT * from payments JOIN loans on payments.loan_id = loans.id WHERE date_of_payment
SELECT *
from payment_schedule TABLESAMPLE bernoulli(0.05);
*/

INSERT INTO payments (date_of_payment, loan_id, amount, payment_channel_id)
SELECT planning_date                          AS date_of_payment,
       loan_id,
       (principal_payment + interest_payment) as amount,
       FLOOR(RANDOM() * 4) + 1                AS payment_channel_id

FROM payment_schedule TABLESAMPLE system(0.99)
         JOIN loans on payment_schedule.loan_id = loans.id
WHERE (interest_payment + principal_payment) > 0
  AND planning_date <= CURRENT_DATE;

SELECT count(*) FROM payment_schedule WHERE planning_date <= CURRENT_DATE
