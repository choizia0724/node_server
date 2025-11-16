package com.ziapond.portfolio.kis.http

import com.fasterxml.jackson.databind.JsonNode
import com.ziapond.portfolio.kis.auth.KisTokenProvider
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClient
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
    @Value("\${kis.retry.max-delay-ms:3000}") private val maxDelayMs: Long,
    private val tokenProvider: KisTokenProvider
) {

    companion object {
        private val log = LoggerFactory.getLogger(KisHttp::class.java)

        private fun mask(value: String?, visible: Int = 4): String {
            if (value.isNullOrBlank()) return "null"
            if (value.length <= visible) return "*".repeat(value.length)
            val tail = value.takeLast(visible)
            return "*".repeat(value.length - visible) + tail
        }

        private fun headersSummary(headers: HttpHeaders): Map<String, Any?> =
            headers.mapValues { (k, v) ->
                when (k.lowercase()) {
                    "authorization" -> "Bearer ..."            // 전체 토큰 X
                    "appsecret"     -> mask(v.firstOrNull())
                    else            -> v
                }
            }
    }

    private val rest: RestClient = builder.baseUrl(baseUrl).build()

    fun getJson(
        path: String,
        query: Map<String, Any?> = emptyMap(),
        reqHeaders: HttpHeaders = HttpHeaders(),
        trId: String? = null,
        auth: Boolean = true,
        custType: String = "P"
    ): JsonNode? = executeWithRetries {
        val token = if (auth) tokenProvider.getRestToken() else null

        val headers = HttpHeaders().apply {
            set("appkey", appKey)
            set("appsecret", appSecret)
            set("custtype", custType)
            set("content-type", "application/json; charset=utf-8")
            if (auth) {
                require(!token.isNullOrBlank()) { "KIS access token is blank" }
                set("authorization", "Bearer $token")
            }
            trId?.takeIf { it.isNotBlank() }?.let { set("tr_id", it) }
            addAll(reqHeaders)
        }

        // ✅ 요청 로그
        log.info(
            "KIS GET 요청: path={}, query={}, tr_id={}, auth={}, headers={}",
            path,
            query.filterValues { it != null },
            trId,
            auth,
            headersSummary(headers)
        )

        rest.get()
            .uri { b ->
                b.path(path)
                query.forEach { (k, v) -> if (v != null) b.queryParam(k, v) }
                b.build()
            }
            .headers { it.addAll(headers) }
            .retrieve()
    }

    fun postJson(
        path: String,
        body: Any? = null,
        query: Map<String, Any?> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        trId: String? = null,
        auth: Boolean = true
    ): JsonNode? = executeWithRetries {
        val token = if (auth) provider.accessToken() else null

        val httpHeaders = HttpHeaders().apply {
            add("appkey", appKey)
            add("appsecret", appSecret)
            add("content-type", "application/json")
            if (auth) {
                require(!token.isNullOrBlank()) { "KIS access token is blank" }
                add("authorization", "Bearer $token")
            }
            if (!trId.isNullOrBlank()) add("tr_id", trId)
            headers.forEach { (k, v) -> add(k, v) }
        }

        // ✅ 요청 로그
        log.info(
            "KIS POST 요청: path={}, query={}, tr_id={}, auth={}, bodyType={}, headers={}",
            path,
            query.filterValues { it != null },
            trId,
            auth,
            body?.javaClass?.simpleName,
            headersSummary(httpHeaders)
        )

        val spec = rest.post()
            .uri { b ->
                b.path(path)
                query.forEach { (k, v) -> if (v != null) b.queryParam(k, v) }
                b.build()
            }
            .headers { it.addAll(httpHeaders) }

        if (body != null) spec.body(body)
        spec.retrieve()
    }

    private inline fun executeWithRetries(
        build: () -> RestClient.ResponseSpec
    ): JsonNode? {
        var attempt = 1
        while (true) {
            try {
                val resp: ResponseEntity<JsonNode> = build().toEntity(JsonNode::class.java)
                val sc = resp.statusCode
                val body = resp.body

                // ✅ 응답 로그 (성공/에러 둘 다)
                log.info(
                    "KIS 응답: status={}, attempt={}, rt_cd={}, msg_cd={}, msg={}",
                    sc.value(),
                    attempt,
                    body?.get("rt_cd")?.asText(),
                    body?.get("msg_cd")?.asText(),
                    body?.get("msg1")?.asText()
                )

                if (sc.is2xxSuccessful) return body

                if (sc == HttpStatus.UNAUTHORIZED && attempt <= maxAttempts) {
                    log.warn("KIS 401 응답 → 토큰 무효화 후 재시도 (attempt={})", attempt)
                    provider.invalidate()
                    attempt++
                    continue
                }

                if ((sc == HttpStatus.TOO_MANY_REQUESTS || sc.is5xxServerError) && attempt < maxAttempts) {
                    val retryAfterHeader = resp.headers["Retry-After"]?.firstOrNull()
                    log.warn(
                        "KIS {} 응답 → 백오프 후 재시도 (attempt={}, retryAfter={})",
                        sc.value(), attempt, retryAfterHeader
                    )
                    sleepBackoff(attempt, retryAfterHeader)
                    attempt++
                    continue
                }

                log.error("KIS 비정상 응답: status={}, body={}", sc.value(), body)
                throw IllegalStateException("HTTP ${sc.value()}")
            } catch (e: HttpClientErrorException.Unauthorized) {
                log.warn("KIS 예외: 401 Unauthorized (attempt={}) body={}", attempt, e.responseBodyAsString)
                if (attempt <= maxAttempts) {
                    provider.invalidate()
                    attempt++
                    continue
                } else throw e
            } catch (e: HttpClientErrorException.TooManyRequests) {
                log.warn("KIS 예외: 429 TooManyRequests (attempt={}) body={}", attempt, e.responseBodyAsString)
                if (attempt < maxAttempts) {
                    val ra = e.responseHeaders?.getFirst("Retry-After")
                    sleepBackoff(attempt, ra)
                    attempt++
                    continue
                } else throw e
            } catch (e: HttpClientErrorException) {
                log.error(
                    "KIS 4xx 예외 발생: status={}, attempt={}, body={}",
                    e.statusCode.value(), attempt, e.responseBodyAsString, e
                )
                throw e
            } catch (e: HttpServerErrorException) {
                log.error(
                    "KIS 5xx 예외 발생: status={}, attempt={}, body={}",
                    e.statusCode.value(), attempt, e.responseBodyAsString, e
                )
                if (attempt < maxAttempts) {
                    sleepBackoff(attempt, null)
                    attempt++
                    continue
                } else throw e
            }
        }
    }

    private fun sleepBackoff(attempt: Int, retryAfterHeader: String?) {
        val fromHeader = retryAfterHeader?.toLongOrNull()?.let { it * 1000 }
        val backoff = fromHeader ?: run {
            val expo = baseDelayMs * (1L shl (attempt - 1))
            val jitter = Random.nextLong(0, baseDelayMs)
            min(expo + jitter, maxDelayMs)
        }
        log.debug("KIS 백오프: attempt={}, sleep={}ms", attempt, backoff)
        Thread.sleep(backoff)
    }
}
