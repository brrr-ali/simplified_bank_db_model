-- Итоговая прибыльность (<все платежи по кредитам с плановой датой погашения в заданный период> /
-- <сумма тел кредитов с плановой датой погашения, попадающей в заданный период>) на момент <текущий день>.
SELECT SUM(payments.amount) / SUM(loans.amount)
FROM payments
         JOIN loans ON payments.loan_id = loans.id
WHERE loans.date_end BETWEEN CURRENT_DATE - INTERVAL '2 years' AND CURRENT_DATE
  AND payments.date_of_payment < CURRENT_DATE;


WITH loansWithPlanningDateInInterval as (select id, amount
                                         from loans l
                                         where l.date_end BETWEEN CURRENT_DATE - INTERVAL '2 years' AND
                                                           CURRENT_DATE),
     result as (SELECT sum(p.amount) as paid, sum(lwpd.amount) as got
                from payments p
                         JOIN loansWithPlanningDateInInterval lwpd ON p.loan_id = lwpd.id
                where date_of_payment < CURRENT_DATE)
SELECT CASE
           WHEN got > 0 THEN
               paid / got
           ELSE
               0
           END
from result r;