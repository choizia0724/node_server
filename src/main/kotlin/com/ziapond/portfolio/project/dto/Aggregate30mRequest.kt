package com.ziapond.portfolio.project.dto

data class Aggregate30mRequest(
    val symbols: List<String>? = null,   // 지정 없으면 KOSPI 전 종목
    val endTime: String? = null          // "HH:mm" (없으면 현재 시각 기준 눈금 스냅)
)