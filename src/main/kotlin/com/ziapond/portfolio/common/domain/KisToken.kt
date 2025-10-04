package com.ziapond.portfolio.common.domain

import java.time.OffsetDateTime

/**
 * @fileoverview
 * @filename KisToken.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 5.
 * @copyright 2025,
 */

data class KisToken(
    val id: Long? = null,
    val tokenType: Short,         // 1: REST, 2: WS
    val dateKey: java.time.LocalDate,
    val accessToken: String,
    val issuedAt: OffsetDateTime,
    val expiresAt: OffsetDateTime,
    val meta: String? = null
)
