CREATE TABLE IF NOT EXISTS ANALYSIS(
    id uuid NOT NULL,
    approved boolean NOT NULL,
    approvedLimit decimal(10,2) NOT NULL,
    requestedAmount decimal(10, 2) NOT NULL,
    withdraw decimal(10, 2) NOT NULL,
    annualInterest decimal(10, 2) NOT NULL,
    clientId uuid NOT NULL,
    date timestamp,
    PRIMARY KEY(id)
);