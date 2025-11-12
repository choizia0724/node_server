package com.ziapond.portfolio.project.web.dto

import java.util.*

data class StockDataRequest(
    val symbol: String,
    val from: Date,
    val to: Date,
)
