package com.ziapond.portfolio.kis.auth

import com.ziapond.portfolio.common.domain.KisToken
import com.ziapond.portfolio.common.mappers.KisTokenMapper
import com.ziapond.portfolio.kis.dto.TokenRequest
import com.ziapond.portfolio.kis.dto.TokenResponse
import com.ziapond.portfolio.kis.http.KisOAuthClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


@Service
class KisTokenProviderImpl(
    builder: RestClient.Builder,
    @Value("\${kis.base-url}") private val baseUrl: String,
    @Value("\${kis.oauth.rest-token-path}") private val tokenPath: String,
    @Value("\${kis.app-key}") private val appKey: String,
    @Value("\${kis.app-secret}") private val appSecret: String,
    @Value("\${kis.refresh-skew-seconds}") private val refreshSkewSeconds: Long,
    private val mapper: KisTokenMapper,
    private val oauth: KisOAuthClient,
    @Value("\${kis.zone:Asia/Seoul}") private val zoneId: String
) : KisTokenProvider {

    private val zone = ZoneId.of(zoneId)
    private val log = LoggerFactory.getLogger(javaClass)
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

            throw IllegalStateException("Failed to fetch KIS token: ${e.statusCode}", e)
        }
    }

    override fun getRestToken(): String = getToken(1)
    override fun getWsToken(): String   = getToken(2)

    @Transactional
    override fun refreshAndGetRest(): String = refreshAndGet(1)

    @Transactional
    override fun refreshAndGetWs(): String = refreshAndGet(2)

    private fun todayKst() = LocalDate.now(zone)
    private fun nowKst()   = OffsetDateTime.now(zone)

    @Transactional(readOnly = true)
    fun getToken(tokenType: Short): String {
        val today = todayKst()
        val row = mapper.selectByTypeAndDate(tokenType, today)
        if (row != null && row.expiresAt.isAfter(nowKst().plusMinutes(1))) {
            return row.accessToken
        }
        // 없으면 즉시 발급
        return refreshAndGet(tokenType)
    }

    @Transactional
    fun refreshAndGet(tokenType: Short): String {
        val res = oauth.requestToken(tokenType) ?: error("KIS OAuth null")
        val accessToken = res.get("access_token")?.asText()
            ?: res.get("accessToken")?.asText()
            ?: error("no access_token field in response: $res")
        val issuedAt = nowKst()
        val expiresInSec = res.get("expires_in")?.asLong()
        val expiresAt = if (expiresInSec != null && expiresInSec > 0)
            issuedAt.plusSeconds(expiresInSec)
        else
            issuedAt.plusHours(23) // 보수적 기본값

        val row = KisToken(
            tokenType = tokenType,
            dateKey = todayKst(),
            accessToken = accessToken,
            issuedAt = issuedAt,
            expiresAt = expiresAt,
            meta = res.toString()
        )
        mapper.upsert(row)
        log.info("KIS token stored: type=$tokenType date=${row.dateKey} exp=$expiresAt")
        return accessToken
    }

    @Transactional
    fun cleanupExpired() {
        val n = mapper.deleteExpired(nowKst().minusDays(1))
        if (n > 0) log.info("KIS tokens cleaned: $n")
    }
}