package com.ziapond.portfolio.project.web.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class KisInquireBalanceResponse(
    @JsonProperty("ctx_area_fk100")
    val ctxAreaFk100: String,

    @JsonProperty("ctx_area_nk100")
    val ctxAreaNk100: String,

    /** 종목별 잔고 리스트 */
    @JsonProperty("output1")
    val positions: List<KisInquireBalancePosition> = emptyList(),

    /** 계좌 전체 요약 (보통 1건) */
    @JsonProperty("output2")
    val summaryList: List<KisInquireBalanceSummary> = emptyList(),

    @JsonProperty("rt_cd")
    val rtCd: String,

    @JsonProperty("msg_cd")
    val msgCd: String,

    @JsonProperty("msg1")
    val msg1: String
) {
    /** 보통 output2는 1건이라서 편의 프로퍼티 하나 추가 */
    val summary: KisInquireBalanceSummary?
        get() = summaryList.firstOrNull()
}
