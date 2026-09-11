CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       full_name VARCHAR(150) NOT NULL,
                       email VARCHAR(180) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL,
                       active BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMPTZ NOT NULL,
                       updated_at TIMESTAMPTZ NOT NULL
);