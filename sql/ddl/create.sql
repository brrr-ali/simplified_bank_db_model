CREATE TABLE employees
(
    id           SERIAL PRIMARY KEY,
    first_name   VARCHAR(50) NOT NULL,
    last_name    VARCHAR(50) NOT NULL,
    email        VARCHAR(50),
    phone_number VARCHAR(15),
    manager_id   INTEGER REFERENCES employees (id)
);

CREATE TABLE clients
(
    id            SERIAL PRIMARY KEY,
    passport_info JSONB,
    email         VARCHAR(50),
    phone_number  VARCHAR(15)

);

CREATE TABLE loan_rates
(
    id              SERIAL PRIMARY KEY,
    min_term        INTEGER,
    max_term        INTEGER,
    min_amount      NUMERIC(11, 2),
    max_amount      NUMERIC(11, 2),
    percentage_rate DECIMAL(5, 2),
    purpose         VARCHAR(100)
);


CREATE TABLE loan_applications
(
    id           SERIAL PRIMARY KEY,
    client_id    INTEGER REFERENCES clients (id),
    loan_rate_id INTEGER REFERENCES loan_rates (id),
    is_approved  BOOLEAN,
    filing_date  date
);

CREATE TABLE loans
(
    id                      SERIAL PRIMARY KEY,
    approved_application_id INTEGER REFERENCES loan_applications (id),
    amount                  NUMERIC(11, 2),
    date_start              date,
    date_end                date,
    interest_start_date     date
);


CREATE TABLE payment_channels
(
    id                      SERIAL PRIMARY KEY,
    type_of_channel         VARCHAR(30),
    commission_percentage   DECIMAL(5, 2),
    commission_fixed_amount NUMERIC(11, 2) DEFAULT 0
);

-- ('Банковский счёт', 'Карта', 'Yandex.NUMERIC(11, 2)', 'QIWI');


CREATE TABLE payments
(
    date_of_payment    TIMESTAMP,
    loan_id            INTEGER REFERENCES loans (id),
    amount             NUMERIC(11, 2),
    payment_channel_id INTEGER REFERENCES payment_channels (id),
    CONSTRAINT chk_payment_amount CHECK (amount > 0::NUMERIC(11, 2))
);


CREATE TABLE payment_schedule
(
    loan_id           INTEGER REFERENCES loans (id),
    planning_date     TIMESTAMP,
    principal_payment NUMERIC(11, 2),
    interest_payment  NUMERIC(11, 2)
);


CREATE TABLE interest_accrual
(
    loan_id           INTEGER REFERENCES loans (id),
    accrual_date      timestamp,
    percentage_amount NUMERIC(11, 2),
    penalties         NUMERIC(11, 2)
);


CREATE TABLE balance
(
    date_of_balance   date,
    principal_balance NUMERIC(11, 2),
    accrued_interest  NUMERIC(11, 2),
    penalty_amount    NUMERIC(11, 2),
    service_fees      NUMERIC(11, 2),
    loan_id           INTEGER REFERENCES loans (id)

);


CREATE INDEX date_ ON payments (date_of_payment);
CREATE INDEX date__ ON payment_schedule (planning_date);
CREATE INDEX loan_id ON balance USING hash (loan_id);
DROP INDEX loan_id;

CREATE INDEX approved_application_id ON loans (approved_application_id);
CREATE INDEX loan_rate_id ON loan_applications (loan_rate_id);