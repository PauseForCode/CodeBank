CREATE TABLE snippets (
    id UUID PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    code TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);
