-- Процент по кол-ву и по суммам просроченных (в текущий момент просрочен любой из платежей в Графике)
-- кредитов с датой погашения, попадающий в заданный период.

SELECT sum(case when (penalty_amount > 0) then 1 else 0 end) * 100.0 / count(*) as overdue_persent,
       sum(case when (penalty_amount > 0) then principal_balance end)
from (SELECT penalty_amount, principal_balance, RANK() over (partition by loan_id order by date_of_balance desc) as rank
      FROM balance
               JOIN loans ON balance.loan_id = loans.id
      where date_end BETWEEN CURRENT_DATE - INTERVAL '2 years' AND CURRENT_DATE - INTERVAL '5 months')
WHERE rank = 1;