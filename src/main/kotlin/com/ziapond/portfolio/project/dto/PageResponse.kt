package com.ziapond.portfolio.project.dto

/**
 * @fileOverview 페이지네이션에 대한 DTO
 * @filename StockSearchRequest.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 2.
 * @copyright 2025,
 */


data class PageResponse<T>(
    val data: List<T>,
    val pagination: Pagination
)


