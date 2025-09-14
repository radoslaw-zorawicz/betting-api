CREATE TABLE IF NOT EXISTS accounts (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id INT NOT NULL,
    account_balance DECIMAL NOT NULL DEFAULT 100,
    CONSTRAINT ck_users_account_balance_non_negative CHECK (account_balance >= 0)
);

CREATE TABLE IF NOT EXISTS bets (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    amount DECIMAL NOT NULL,
    event_id VARCHAR NOT NULL,
    driver_id INT NOT NULL,
    user_id INT NOT NULL,
    status VARCHAR NOT NULL,
    odds INT NOT NULL,
    CONSTRAINT ck_bets_amount_non_negative CHECK (amount >= 0)
);

CREATE TABLE IF NOT EXISTS event_outcomes_log (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id INT NOT NULL,
    won_driver_id INT NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE
);

-- Seed initial accounts (user_id 1..5)
INSERT INTO accounts (user_id) VALUES (1);
INSERT INTO accounts (user_id) VALUES (2);
INSERT INTO accounts (user_id) VALUES (3);
INSERT INTO accounts (user_id) VALUES (4);
INSERT INTO accounts (user_id) VALUES (5);


CREATE TABLE IF NOT EXISTS event_outcomes (
    event_id VARCHAR PRIMARY KEY,
    winning_driver_id INT NOT NULL,
    finished_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_event_outcomes_event_id ON event_outcomes (event_id);
