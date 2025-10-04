package com.ziapond.portfolio.kis.http

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

/**
 * @fileoverview
 * @filename KisOAuthClient.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 5.
 * @copyright 2025,
 */

@Component
class KisOAuthClient(
    private val restBuilder: RestClient.Builder
) {
    @Value("\${kis.base-url}") lateinit var baseUrl: String
    @Value("\${kis.oauth.rest-token-path}") lateinit var restPath: String
    @Value("\${kis.oauth.ws-token-path}") lateinit var wsPath: String
    @Value("\${kis.oauth.grant-type}") lateinit var grantType: String
    @Value("\${kis.app-key}") lateinit var appKey: String
    @Value("\${kis.app-secret}") lateinit var appSecret: String

    fun requestToken(tokenType: Short): JsonNode? {
        val url = if (tokenType.toInt() == 2) baseUrl+wsPath else baseUrl+restPath
        val client = restBuilder.build()
        val body = mapOf(
            "grant_type" to grantType,
            "appkey" to appKey,
            "appsecret" to appSecret
        )
        return client.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .body(JsonNode::class.java)
    }
}
