CREATE TABLE articles (
    "id" uuid DEFAULT uuid_generate_v4 () primary key,
    user_id uuid NOT NULL REFERENCES users ("id") ON DELETE CASCADE,
    original_lang varchar(20),
    original_content text,
    content text,
    status smallint DEFAULT 1 NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);