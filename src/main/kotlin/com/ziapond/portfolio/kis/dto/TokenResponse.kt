package com.ziapond.portfolio.kis.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @fileoverview
 * @filename TokenResponse.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 5.
 * @copyright 2025,
 */

data class TokenResponse(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("token_type") val tokenType: String? = "Bearer",
    @JsonProperty("expires_in") val expiresIn: Long = 86400
)