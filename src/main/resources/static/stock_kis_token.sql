CREATE SCHEMA IF NOT EXISTS stocks;

CREATE TABLE IF NOT EXISTS stocks.kis_token (
                                              id           BIGSERIAL PRIMARY KEY,
                                              token_type   SMALLINT    NOT NULL,         -- 1: REST, 2: WS
                                              date_key     DATE        NOT NULL,         -- KST 기준 '당일'
                                              access_token TEXT        NOT NULL,
                                              issued_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at   TIMESTAMPTZ NOT NULL,
    meta         JSONB,
    CONSTRAINT uk_kis_token UNIQUE(token_type, date_key)
    );

CREATE INDEX IF NOT EXISTS idx_kis_token_valid
    ON stocks.kis_token(token_type, date_key, expires_at DESC);