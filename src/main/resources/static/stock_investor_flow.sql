CREATE SCHEMA IF NOT EXISTS stocks;

CREATE TABLE IF NOT EXISTS stocks.investor_flow (
                                                        ymd                DATE         NOT NULL,          -- 영업일 (YYYY-MM-DD)
                                                        bucket_start       TIMESTAMPTZ  NOT NULL,          -- 30분 버킷 시작(Asia/Seoul)
                                                        market_code        VARCHAR(16)  NOT NULL,          -- 'KOSPI' / 'KOSDAQ' 등
    investor_type_code VARCHAR(32)  NOT NULL,          -- KIS 제공 코드(예: 개인/외국인/기관 등 코드)
    investor_type_name TEXT,                            -- 사람이 읽는 이름
    net_qty            BIGINT,                          -- 30분 구간 순매수 "수량" (= 누적 차분)
    net_amt            NUMERIC(20,0),                   -- 30분 구간 순매수 "금액" (= 누적 차분)
    acml_net_qty       BIGINT,                          -- 버킷 종료시점 누적 수량
    acml_net_amt       NUMERIC(20,0),                   -- 버킷 종료시점 누적 금액
    PRIMARY KEY (ymd, bucket_start, market_code, investor_type_code)
    );

CREATE INDEX IF NOT EXISTS idx_investor_flow_30m_market_time
    ON stocks.investor_flow (market_code, bucket_start DESC);
