package com.ziapond.portfolio.project.domain

import java.time.LocalDate

/**
 * @fileoverview
 * @path /StockTable.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 1.
 * @copyright 2025,
 */

data class StockTable(
    val symbol: String,
    val name: String,
    val basdt: LocalDate,
    val isincd: String,
    val mrktctg: String,
    val crno: String,
    val corpnm:String,
)