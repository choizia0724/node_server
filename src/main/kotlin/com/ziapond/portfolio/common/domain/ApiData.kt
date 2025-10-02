package com.ziapond.portfolio.common.domain
/**
 * @fileoverview
 * @path /ApiData.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 1.
 * @copyright 2025,
 */
data class ApiData(
    val baseUrl: String,
    val provider: String,
    val method: String,
    val description: String,
    val pathTemplate: String,
    val headers: Map<String, String>,
    val pathParameters: Map<String, String>,
    val responseParams: Map<String, String>,
)
