CREATE TABLE article_sentences (
    user_id uuid NOT NULL REFERENCES users ("id") ON DELETE CASCADE,
    article_id uuid NOT NULL REFERENCES articles ("id") ON DELETE CASCADE,
    serial_number int,
    original_lang varchar(20),
    original_content text,
    content text,
    status smallint DEFAULT 1 NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("user_id", "article_id", "serial_number")
);