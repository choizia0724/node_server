package com.ziapond.portfolio.project.web.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import java.time.LocalDate

/**
 * @fileoverview
 * @filename StockSearchRequest.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 1.
 * @copyright 2025,
 */


data class StockSearchRequest(

    val symbol: String? = null,

    val name: String? = null,

    val mrktctg: String? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    val from: LocalDate? = null,   // 옵션: 기준일 시작

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    val to: LocalDate? = null,     // 옵션: 기준일 끝

    @field:Min(1)
    val page: Int = 1,

    @field:Min(1) @field:Max(5000)
    val limit: Int = 20,

    val useornot: Boolean? = null
)

