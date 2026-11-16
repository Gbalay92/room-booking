CREATE TABLE users (
   id            UUID         PRIMARY KEY,
   email         VARCHAR(254) NOT NULL,
   password_hash VARCHAR(255) NOT NULL,
   role          VARCHAR(20)  NOT NULL,
   created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
   CONSTRAINT users_role_check CHECK (role IN ('USER', 'ADMIN'))
);

CREATE UNIQUE INDEX users_email_unique ON users (lower(email));