package com.ziapond.portfolio.project.web.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import java.time.LocalDate

/**
 * @fileoverview
 * @filename InvestorFlowSearchRequest.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 3.
 * @copyright 2025,
 */


data class InvestorFlowSearchRequest(
    val symbol: String? = null,             // 있으면 "종목단위" 조회, 없으면 "시장단위" 조회
    val marketCode: String? = null,         // KOSPI/KOSDAQ (시장단위일 때 유용)
    val investorType: String? = null,       // 투자자 유형 코드 필터(옵션)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val from: LocalDate? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val to: LocalDate? = null,
    @field:Min(1) val page: Int = 1,
    @field:Min(1) @field:Max(1000) val limit: Int = 20
)

