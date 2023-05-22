CREATE TABLE IF NOT EXISTS ANALYSIS(
    id uuid NOT NULL,
    approved boolean NOT NULL,
    approved_limit NUMERIC(10,2),
    withdraw NUMERIC(10, 2),
    annual_interest NUMERIC(10, 2),
    client_id uuid NOT NULL,
    date timestamp,
    PRIMARY KEY(id)
);