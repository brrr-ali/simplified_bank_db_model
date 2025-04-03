-- Прибыльность +2M (2 месяца с даты погашения по договору по каждому из кредитов) по кредитам, выданным в заданный период.
-- Итоговая прибыльность (<все платежи по кредитам с плановой датой погашения в заданный период> /
-- <сумма тел кредитов с плановой датой погашения, попадающей в заданный период>) на момент <текущий день>.

SELECT SUM(payments.amount) / SUM(loans.amount)
FROM payments
         JOIN loans ON payments.loan_id = loans.id
WHERE payments.date_of_payment <= loans.date_end + INTERVAL '2 months'
  AND loans.date_start BETWEEN CURRENT_DATE - INTERVAL '2 years' AND CURRENT_DATE - INTERVAL '5 months';
