package com.ziapond.portfolio.kis.http

import com.fasterxml.jackson.databind.JsonNode
import com.ziapond.portfolio.kis.auth.KisTokenProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClient
import java.time.Duration
import kotlin.math.min
import kotlin.random.Random

@Component
class KisHttp(
    private val provider: KisTokenProvider,
    builder: RestClient.Builder,
    @Value("\${kis.base-url}") private val baseUrl: String,
    @Value("\${kis.app-key}") private val appKey: String,
    @Value("\${kis.app-secret}") private val appSecret: String,
    @Value("\${kis.retry.max-attempts:5}") private val maxAttempts: Int,
    @Value("\${kis.retry.base-delay-ms:250}") private val baseDelayMs: Long,
    @Value("\${kis.retry.max-delay-ms:3000}") private val maxDelayMs: Long
) {
    private val rest: RestClient = builder.baseUrl(baseUrl).build()

    fun getJson(
        path: String,
        query: Map<String, Any?> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        trId: String? = null,
        auth: Boolean = true
    ): JsonNode? = executeWithRetries(
        build = {
            rest.get()
                .uri { b ->
                    b.path(path); query.forEach { (k, v) -> if (v != null) b.queryParam(k, v) }; b.build()
                }
                .headers { h ->
                    h.add("appkey", appKey); h.add("appsecret", appSecret); h.add("content-type", "application/json")
                    if (auth) h.add("authorization", "Bearer ${provider.accessToken()}")
                    if (!trId.isNullOrBlank()) h.add("tr_id", trId)
                    headers.forEach { (k, v) -> h.add(k, v) }
                }
                .retrieve()
        }
    )

    fun postJson(
        path: String,
        body: Any? = null,
        query: Map<String, Any?> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        trId: String? = null,
        auth: Boolean = true
    ): JsonNode? = executeWithRetries(
        build = {
            val spec = rest.post()
                .uri { b ->
                    b.path(path); query.forEach { (k, v) -> if (v != null) b.queryParam(k, v) }; b.build()
                }
                .headers { h ->
                    h.add("appkey", appKey); h.add("appsecret", appSecret); h.add("content-type", "application/json")
                    if (auth) h.add("authorization", "Bearer ${provider.accessToken()}")
                    if (!trId.isNullOrBlank()) h.add("tr_id", trId)
                    headers.forEach { (k, v) -> h.add(k, v) }
                }
            if (body != null) spec.body(body)
            spec.retrieve()
        }
    )

    private inline fun executeWithRetries(
        build: () -> RestClient.ResponseSpec
    ): JsonNode? {
        var attempt = 1
        while (true) {
            try {
                val resp: ResponseEntity<JsonNode> = build().toEntity(JsonNode::class.java)
                val sc = resp.statusCode

                // 성공
                if (sc.is2xxSuccessful) return resp.body

                // 401 → 토큰 무효화 후 즉시 한 번 더
                if (sc == HttpStatus.UNAUTHORIZED && attempt <= maxAttempts) {
                    provider.invalidate()
                    attempt++
                    continue
                }

                // 429/5xx → 백오프 후 재시도
                if ((sc == HttpStatus.TOO_MANY_REQUESTS || sc.is5xxServerError) && attempt < maxAttempts) {
                    val retryAfterHeader = resp.headers["Retry-After"]?.firstOrNull()
                    sleepBackoff(attempt, retryAfterHeader)
                    attempt++
                    continue
                }

                // 그 외 상태코드는 예외로 처리
                throw IllegalStateException("HTTP ${sc.value()}")
            } catch (e: HttpClientErrorException.Unauthorized) {
                if (attempt <= maxAttempts) {
                    provider.invalidate(); attempt++; continue
                } else throw e
            } catch (e: HttpClientErrorException.TooManyRequests) {
                if (attempt < maxAttempts) {
                    val ra = e.responseHeaders?.getFirst("Retry-After")
                    sleepBackoff(attempt, ra); attempt++; continue
                } else throw e
            } catch (e: HttpClientErrorException) {
                // 4xx 그 외
                throw e
            } catch (e: HttpServerErrorException) {
                if (attempt < maxAttempts) {
                    sleepBackoff(attempt, null); attempt++; continue
                } else throw e
            }
        }
    }

    private fun sleepBackoff(attempt: Int, retryAfterHeader: String?) {
        val fromHeader = retryAfterHeader?.toLongOrNull()?.let { it * 1000 }
        val backoff = fromHeader ?: run {
            val expo = baseDelayMs * (1L shl (attempt - 1))    // 지수 증가
            val jitter = Random.nextLong(0, baseDelayMs)       // 지터
            min(expo + jitter, maxDelayMs)
        }
        Thread.sleep(backoff)
    }
}
