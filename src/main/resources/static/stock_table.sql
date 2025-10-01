CREATE TABLE stock_table (
                             symbol    VARCHAR(20)  NOT NULL,          -- 종목코드 (예: 005930)
                             name      VARCHAR(200) NOT NULL,          -- 종목명 (국문)
                             basdt     DATE         NOT NULL,          -- 기준일자 (yyyy-MM-dd)
                             isincd    VARCHAR(12),                    -- ISIN 코드
                             mrktctg   VARCHAR(16),                    -- 시장구분 (KOSPI/KOSDAQ/ETF 등)
                             crno      VARCHAR(20),                    -- 법인등록번호/사업자번호 등
                             corpnm    VARCHAR(200),                   -- 발행사/법인명
                             PRIMARY KEY (symbol)
);

-- 조회 성능 보조 인덱스 (선택)
CREATE INDEX idx_stock_table_isincd   ON stock_table (isincd);
CREATE INDEX idx_stock_table_mrktctg  ON stock_table (mrktctg);