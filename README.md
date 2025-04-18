# simplified_bank_db_model

Упрощённая модель автоматизированной банковской системы с поддержкой кредитования, управления сотрудниками и анализа финансовых показателей.

## Технологии
- **СУБД**: PostgreSQL 14+
- **Генерация данных**: PL/SQL, Java (для массовых вставок)

## Поставленная задача:
Спроектировать упрощённую БД банковской системы с таблицами: сотрудники (иерархия руководителей), кредитные/вкладные тарифы, клиенты (ФИО, ПД, кредитные тарифы), выданные кредиты (баланс, график платежей, каналы оплаты с комиссиями).
Заполнить данными: 5 тыс. сотрудников, 150 тыс. клиентов, 70 тыс. кредитов, 150 тыс. платежей.
Реализовать запросы:
- Статистика по выданным кредитам (сумма, средняя, топ-клиент).
- Анализ возвратов кредитов с группировкой по дням/месяцам/годам.
- Иерархия сотрудников от выбранного до верхнего руководства.
- Процент просроченных кредитов за период.
- Прибыльность кредитов на текущий день и через 2 месяца после погашения.
Обеспечить целостность: кредиты только с одобренными тарифами, платежи — к существующим кредитам.
Проверить корректность на укороченной БД и под нагрузкой.

Для заполнения данными нужно исполнить `/workspaces/simplified_bank_db_model/sql/dml/insert_lab3/Main.java`, а затем все sql-скрипты из папки dml. Предупреждение: заполнение некоторых таблиц очень долгое, так как генерируется большой массив данных. Позже добавлю оптимизацию.
Для ускорения работы запросов были добавлены индексы, их создание также проискодит в `ddl/create.sql`.
