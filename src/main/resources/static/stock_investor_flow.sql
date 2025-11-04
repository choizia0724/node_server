CREATE SCHEMA IF NOT EXISTS stocks;

CREATE TABLE stocks.investor_flow (
    id BIGSERIAL PRIMARY KEY,

    symbol varchar(8) NOT NULL,

    stck_bsop_date     VARCHAR(8)  NOT NULL,
    stck_clpr          VARCHAR(10) NOT NULL,
    prdy_vrss          VARCHAR(10) NOT NULL,
    prdy_vrss_sign     VARCHAR(1)  NOT NULL,

    prsn_ntby_qty      VARCHAR(12) NOT NULL,
    frgn_ntby_qty      VARCHAR(12) NOT NULL,
    orgn_ntby_qty      VARCHAR(18) NOT NULL,

    prsn_ntby_tr_pbmn  VARCHAR(18) NOT NULL,
    frgn_ntby_tr_pbmn  VARCHAR(18) NOT NULL,
    orgn_ntby_tr_pbmn  VARCHAR(18) NOT NULL,

    prsn_shnu_vol      VARCHAR(18) NOT NULL,
    frgn_shnu_vol      VARCHAR(18) NOT NULL,
    orgn_shnu_vol      VARCHAR(18) NOT NULL,

    prsn_shnu_tr_pbmn  VARCHAR(18) NOT NULL,
    frgn_shnu_tr_pbmn  VARCHAR(18) NOT NULL,
    orgn_shnu_tr_pbmn  VARCHAR(18) NOT NULL,

    prsn_seln_vol      VARCHAR(18) NOT NULL,
    frgn_seln_vol      VARCHAR(18) NOT NULL,
    orgn_seln_vol      VARCHAR(18) NOT NULL,

    prsn_seln_tr_pbmn  VARCHAR(18) NOT NULL,
    frgn_seln_tr_pbmn  VARCHAR(18) NOT NULL,
    orgn_seln_tr_pbmn  VARCHAR(18) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_investor_flow_30m_market_time
    ON stocks.investor_flow (market_code, bucket_start DESC);

