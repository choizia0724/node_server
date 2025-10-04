CREATE SCHEMA IF NOT EXISTS stocks;

CREATE TABLE IF NOT EXISTS stocks.stock_data (
                                                 symbol        VARCHAR(20)  NOT NULL,
                                                 bucket_start  TIMESTAMPTZ  NOT NULL,      -- 30분 버킷 시작(Asia/Seoul)
                                                 "open"        NUMERIC(16,4),
                                                 high          NUMERIC(16,4),
                                                 low           NUMERIC(16,4),
                                                 "close"       NUMERIC(16,4),
                                                 volume        BIGINT,
                                                 PRIMARY KEY (symbol, bucket_start)
);

CREATE INDEX IF NOT EXISTS idx_stock_data_symbol_time
    ON stocks.stock_data(symbol, bucket_start DESC);