package com.ziapond.portfolio.project.web.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class KisInquireBalanceSummary(
    /** 예수금총액 */
    @JsonProperty("dnca_tot_amt")
    val depositTotalAmount: String,

    /** 익일정산예정금액 */
    @JsonProperty("nxdy_excc_amt")
    val nextDayExpectedSettlementAmount: String,

    /** 이전일잔고정산금액 */
    @JsonProperty("prvs_rcdl_excc_amt")
    val previousRecordSettlementAmount: String,

    /** CMA 평가금액 */
    @JsonProperty("cma_evlu_amt")
    val cmaEvaluationAmount: String,

    /** 전일 매수 금액 */
    @JsonProperty("bfdy_buy_amt")
    val prevDayBuyAmount: String,

    /** 금일 매수 금액 */
    @JsonProperty("thdt_buy_amt")
    val todayBuyAmount: String,

    /** 익일 자동상환 금액 */
    @JsonProperty("nxdy_auto_rdpt_amt")
    val nextDayAutoRepaymentAmount: String,

    /** 전일 매도 금액 */
    @JsonProperty("bfdy_sll_amt")
    val prevDaySellAmount: String,

    /** 금일 매도 금액 */
    @JsonProperty("thdt_sll_amt")
    val todaySellAmount: String,

    /** D+2 자동상환 금액 */
    @JsonProperty("d2_auto_rdpt_amt")
    val d2AutoRepaymentAmount: String,

    /** 전일 제비용 금액 */
    @JsonProperty("bfdy_tlex_amt")
    val prevDayTaxFeeAmount: String,

    /** 금일 제비용 금액 */
    @JsonProperty("thdt_tlex_amt")
    val todayTaxFeeAmount: String,

    /** 총 대출 금액 */
    @JsonProperty("tot_loan_amt")
    val totalLoanAmount: String,

    /** 주식 평가 금액 */
    @JsonProperty("scts_evlu_amt")
    val stockEvaluationAmount: String,

    /** 총 평가 금액 */
    @JsonProperty("tot_evlu_amt")
    val totalEvaluationAmount: String,

    /** 순자산 금액 */
    @JsonProperty("nass_amt")
    val netAssetAmount: String,

    /** 금융상품자동상환여부 */
    @JsonProperty("fncg_gld_auto_rdpt_yn")
    val financialAutoRepaymentYn: String,

    /** 매입금액합계 */
    @JsonProperty("pchs_amt_smtl_amt")
    val purchaseAmountSum: String,

    /** 평가금액합계 */
    @JsonProperty("evlu_amt_smtl_amt")
    val evaluationAmountSum: String,

    /** 평가손익합계금액 */
    @JsonProperty("evlu_pfls_smtl_amt")
    val evaluationProfitLossSumAmount: String,

    /** 총 대주/대출 매도 정산대금 */
    @JsonProperty("tot_stln_slng_chgs")
    val totalStockLoanSellingCharges: String,

    /** 전일 총 자산 평가 금액 */
    @JsonProperty("bfdy_tot_asst_evlu_amt")
    val prevDayTotalAssetEvaluationAmount: String,

    /** 자산 증감 금액 */
    @JsonProperty("asst_icdc_amt")
    val assetIncreaseDecreaseAmount: String,

    /** 자산 증감 수익률 */
    @JsonProperty("asst_icdc_erng_rt")
    val assetIncreaseDecreaseEarningRate: String
)
