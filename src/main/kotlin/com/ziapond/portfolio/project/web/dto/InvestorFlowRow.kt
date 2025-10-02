package com.ziapond.portfolio.project.web.dto

import java.time.LocalDate

/**
 * @fileoverview
 * @filename InvestorFlowRow.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 3.
 * @copyright 2025,
 */

data class InvestorFlowRow(
    val ymd: LocalDate,
    val bucketStart: java.time.OffsetDateTime,
    val marketCode: String? = null,
    val symbol: String? = null,
    val investorTypeCode: String,
    val investorTypeName: String?,
    val netQty: Long?,
    val netAmt: java.math.BigDecimal?,
    val acmlNetQty: Long?,
    val acmlNetAmt: java.math.BigDecimal?
)
