package com.ziapond.portfolio.kis.auth

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import java.time.Duration
import java.time.Instant
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Service
class KisTokenProviderImpl(
    builder: RestClient.Builder,
    @Value("\${kis.base-url}") private val baseUrl: String,
    @Value("\${kis.token-path:/oauth2/tokenP}") private val tokenPath: String,
    @Value("\${kis.app-key}") private val appKey: String,
    @Value("\${kis.app-secret}") private val appSecret: String,
    @Value("\${kis.refresh-skew-seconds:60}") private val refreshSkewSeconds: Long
) : KisTokenProvider {

    private val rest: RestClient = builder.baseUrl(baseUrl).build()

    // in-memory 캐시
    @Volatile private var cachedToken: String? = null
    @Volatile private var expiresAt: Instant? = null
    private val lock = ReentrantLock()

    override fun accessToken(): String {
        val now = Instant.now()
        val token = cachedToken
        val exp = expiresAt

        // 캐시가 있고 아직 충분히 유효하면 바로 반환
        if (!token.isNullOrBlank() && exp != null && now.isBefore(exp.minusSeconds(refreshSkewSeconds))) {
            return token
        }

        // 갱신 구역 (동시성 제어)
        return lock.withLock {
            val t = cachedToken
            val e = expiresAt
            val stillOk = !t.isNullOrBlank() && e != null && Instant.now().isBefore(e.minusSeconds(refreshSkewSeconds))
            if (stillOk) return t!!

            val fresh = fetchNewToken()
            cachedToken = fresh.accessToken
            // 만료시각 = now + expiresIn - 아주 작은 여유(2초)
            expiresAt = Instant.now().plusSeconds(fresh.expiresIn.coerceAtLeast(5) - 2)
            cachedToken!!
        }
    }

    override fun invalidate() {
        lock.withLock {
            cachedToken = null
            expiresAt = null
        }
    }

    private fun fetchNewToken(): TokenResponse {

        val req = TokenRequest(
            grantType = "client_credentials",
            appKey = appKey,
            appSecret = appSecret
        )
        try {
            return rest.post()
                .uri(tokenPath)
                .body(req)
                .retrieve()
                .body(TokenResponse::class.java)
                ?: throw IllegalStateException("KIS token response was null")
        } catch (e: HttpClientErrorException) {
            // 4xx 상세 로그 원하면 e.responseBodyAsString 출력
            throw IllegalStateException("Failed to fetch KIS token: ${e.statusCode}", e)
        }
    }

    // ===== DTOs =====
    private data class TokenRequest(
        @JsonProperty("grant_type") val grantType: String,
        @JsonProperty("appkey") val appKey: String,
        @JsonProperty("appsecret") val appSecret: String
    )

    data class TokenResponse(
        @JsonProperty("access_token") val accessToken: String,
        @JsonProperty("token_type") val tokenType: String? = "Bearer",
        @JsonProperty("expires_in") val expiresIn: Long = 86400
    )
}