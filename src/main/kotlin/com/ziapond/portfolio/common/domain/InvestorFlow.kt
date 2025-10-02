package com.ziapond.portfolio.common.domain

import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * @fileoverview
 * @filename InvestorFlow.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 3.
 * @copyright 2025,
 */


data class InvestorFlow(
    val ymd: LocalDate,
    val bucketStart: OffsetDateTime,
    val marketCode: String,          // ex) "KOSPI", "KOSDAQ"
    val investorTypeCode: String,    // KIS investor type code
    val investorTypeName: String?,   // optional
    val netQty: Long?,               // 30분 구간 순매수 수량 (diff)
    val netAmt: BigDecimal?,         // 30분 구간 순매수 금액 (diff)
    val acmlNetQty: Long?,           // 종료 시점 누적 수량
    val acmlNetAmt: BigDecimal?      // 종료 시점 누적 금액
)