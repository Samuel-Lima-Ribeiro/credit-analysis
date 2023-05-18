CREATE TABLE IF NOT EXISTS ANALYSIS(
    id uuid NOT NULL,
    approved boolean NOT NULL,
    approved_limit decimal(10,2),
    withdraw decimal(10, 2),
    annual_interest decimal(10, 2),
    client_id uuid NOT NULL,
    client_cpf varchar(11),
    date timestamp,
    PRIMARY KEY(id)
);