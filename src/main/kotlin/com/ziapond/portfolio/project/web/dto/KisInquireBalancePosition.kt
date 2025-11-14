package com.ziapond.portfolio.project.web.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class KisInquireBalancePosition(
    /** 종목코드 (6자리) */
    @JsonProperty("pdno")
    val pdno: String,

    /** 종목명 (상품명) */
    @JsonProperty("prdt_name")
    val productName: String,

    /** 매매구분명 (현금/신용/자기융자 등) */
    @JsonProperty("trad_dvsn_name")
    val tradeDivisionName: String,

    /** 전일 매수 수량 */
    @JsonProperty("bfdy_buy_qty")
    val bfdyBuyQty: String,

    /** 전일 매도 수량 */
    @JsonProperty("bfdy_sll_qty")
    val bfdySellQty: String,

    /** 금일 매수 수량 */
    @JsonProperty("thdt_buyqty")
    val todayBuyQty: String,

    /** 금일 매도 수량 */
    @JsonProperty("thdt_sll_qty")
    val todaySellQty: String,

    /** 보유 수량 */
    @JsonProperty("hldg_qty")
    val holdingQty: String,

    /** 주문 가능 수량 */
    @JsonProperty("ord_psbl_qty")
    val orderPossibleQty: String,

    /** 매입 평균 단가 */
    @JsonProperty("pchs_avg_pric")
    val purchaseAvgPrice: String,

    /** 매입 금액 */
    @JsonProperty("pchs_amt")
    val purchaseAmount: String,

    /** 현재가 */
    @JsonProperty("prpr")
    val currentPrice: String,

    /** 평가 금액 */
    @JsonProperty("evlu_amt")
    val evaluationAmount: String,

    /** 평가 손익 금액 */
    @JsonProperty("evlu_pfls_amt")
    val evaluationProfitLossAmount: String,

    /** 평가 손익률 */
    @JsonProperty("evlu_pfls_rt")
    val evaluationProfitLossRate: String,

    /** 평가 수익률 */
    @JsonProperty("evlu_erng_rt")
    val evaluationEarningRate: String,

    /** 대출일 (YYYYMMDD) */
    @JsonProperty("loan_dt")
    val loanDate: String,

    /** 대출 금액 */
    @JsonProperty("loan_amt")
    val loanAmount: String,

    /** 대주/대출 매도 정산대금 */
    @JsonProperty("stln_slng_chgs")
    val stockLoanSellingCharges: String,

    /** 만기일 (YYYYMMDD) */
    @JsonProperty("expd_dt")
    val expiryDate: String,

    /** 등락률 */
    @JsonProperty("fltt_rt")
    val fluctuationRate: String,

    /** 전일 대비 증감금액 */
    @JsonProperty("bfdy_cprs_icdc")
    val prevDayCompareIncreaseDecrease: String,

    /** 종목별 증거금율명 */
    @JsonProperty("item_mgna_rt_name")
    val itemMarginRateName: String,

    /** 보증금율명 */
    @JsonProperty("grta_rt_name")
    val guaranteeRateName: String,

    /** 대용가격 */
    @JsonProperty("sbst_pric")
    val substitutePrice: String,

    /** 대출단가 (신용/융자 단가) */
    @JsonProperty("stck_loan_unpr")
    val stockLoanUnitPrice: String
)
