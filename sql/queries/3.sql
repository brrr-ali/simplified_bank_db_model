-- Список сотрудников с иерархией от самого высокого рук-ля (может быть несколько, это граф) до всех нижних.
-- Если не хочется 5, то можно вывести иерархию по одному заданному сотруднику: от него до самого верха.
WITH RECURSIVE employee_hierarchy AS (SELECT id,
                                             first_name,
                                             last_name,
                                             email,
                                             phone_number,
                                             manager_id,
                                             0  AS LEVEL,
                                             '' AS path
                                      FROM employees
                                      WHERE manager_id IS NULL
                                      UNION ALL
                                      SELECT e.id,
                                             e.first_name,
                                             e.last_name,
                                             e.email,
                                             e.phone_number,
                                             e.manager_id,
                                             LEVEL + 1,
                                             path || '/' || e.id
                                      FROM employees e

                                               JOIN employee_hierarchy m ON e.manager_id = m.id)
SELECT *
FROM employee_hierarchy;