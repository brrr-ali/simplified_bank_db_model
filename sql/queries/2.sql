-- Объём возвратов по кол-ву и сумме по кредитам, выданным за период, на момент: текущий день (т.е. платежи считаем за всё время).
-- Отчёт с агрегацией последовательно по <дням => месяцам => годам, итого за весь период> (см. ROLLUP).
SELECT DISTINCT EXTRACT(YEAR FROM date_of_payment)  AS year,
                EXTRACT(MONTH FROM date_of_payment) AS month,
                EXTRACT(DAY FROM date_of_payment)   AS day,
                COUNT(*)                            AS payment_count,
                SUM(payments.amount)                AS total_payment_amount
FROM payments
         JOIN loans ON payments.loan_id = loans.id
WHERE loans.date_start >= CURRENT_DATE - INTERVAL '1 year'
GROUP BY ROLLUP (EXTRACT(YEAR FROM date_of_payment),
                 EXTRACT(MONTH FROM date_of_payment),
                 EXTRACT(DAY FROM date_of_payment))
ORDER BY year, month, day;