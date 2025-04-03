-- Объём выдач по кол-ву и сумме по кредитам, выданным за период,
-- добавить столбцы <Средняя сумма выданного кредита в данном кредитном тарифе> и
-- <Клиент с самым большим выданным кредитом в данном тарифном тарифе>

WITH clients_with_max_credit AS (SELECT loan_rate_id, client_id
                                 FROM loans
                                 JOIN public.loan_applications la on loans.approved_application_id = la.id
                                 WHERE date_start >= CURRENT_DATE - INTERVAL '1 years'
                                 GROUP BY loan_rate_id, client_id, amount
                                 HAVING amount = max(amount))
SELECT DISTINCT clients_with_max_credit.loan_rate_id,
                count(*),
                sum(amount),
                sum(amount) / count(amount)            AS average,
                max(amount),
                max(clients_with_max_credit.client_id) as client_with_max_credit_and_id
FROM loans
         JOIN clients_with_max_credit ON clients_with_max_credit.loan_rate_id = loans.id
WHERE date_start >= CURRENT_DATE - INTERVAL '1 years'
GROUP BY clients_with_max_credit.loan_rate_id;