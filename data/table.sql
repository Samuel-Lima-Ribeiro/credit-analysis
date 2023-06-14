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

-- CLIENT-API

CREATE TABLE IF NOT EXISTS CLIENT(
        id uuid NOT NULL,
        name varchar(300),
        cpf varchar(11) UNIQUE,
        birthdate date,
        address_id uuid unique,
        created_at timestamp,
        updated_at timestamp,
        PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS ADDRESS(
        id uuid NOT NULL,
        street varchar(256),
        neighborhood varchar(100),
        city varchar(100),
        state varchar(100),
        house_number integer,
        complement varchar(256),
        cep varchar(9),
        created_at timestamp,
        updated_at timestamp,
        PRIMARY KEY (id)
);

ALTER TABLE CLIENT
ADD CONSTRAINT fk_address_id
FOREIGN KEY (address_id) REFERENCES ADDRESS (id);