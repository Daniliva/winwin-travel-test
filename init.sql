
CREATE DATABASE IF NOT EXISTS mini_test;

\connect mini_test;


CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);


CREATE TABLE IF NOT EXISTS processing_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    input_text TEXT NOT NULL CHECK (LENGTH(input_text) <= 1000),
    output_text TEXT NOT NULL CHECK (LENGTH(output_text) <= 1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);


INSERT INTO users (email, password_hash) 
VALUES ('test@a.com', '$2a$10$j8Xz3mZ6z3z3z3z3z3z3z3z3z3z3z3z3z3z3z3z3z3z3z3z3z3z3z') 
ON CONFLICT (email) DO NOTHING;