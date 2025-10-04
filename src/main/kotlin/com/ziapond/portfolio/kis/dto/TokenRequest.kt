package com.ziapond.portfolio.kis.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @fileoverview
 * @filename TokenRequest.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 5.
 * @copyright 2025,
 */

data class TokenRequest(
    @JsonProperty("grant_type") val grantType: String,
    @JsonProperty("appkey") val appKey: String,
    @JsonProperty("appsecret") val appSecret: String
)
