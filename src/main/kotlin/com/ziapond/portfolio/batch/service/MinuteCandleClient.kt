// service/MinuteCandleClient.kt
package com.ziapond.portfolio.batch.service

import com.fasterxml.jackson.databind.JsonNode
import com.ziapond.portfolio.kis.http.KisHttp
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.*

@Service
class MinuteCandleClient(
    private val http: KisHttp
) {
    data class MinuteTick(
        val tsKst: OffsetDateTime,
        val open: BigDecimal,
        val high: BigDecimal,
        val low: BigDecimal,
        val close: BigDecimal,
        val volume: Long
    )

    private val KST: ZoneId = ZoneId.of("Asia/Seoul")

    /** 당일 기준, [windowStart, windowEnd) 범위의 1분틱 수집 */
    fun fetchWindowTicks(symbol6: String, windowStart: LocalTime, windowEnd: LocalTime): List<MinuteTick> {
        // KIS 당일 분봉 엔드포인트: 끝시각 기준으로 30분치 제공되는 패턴
        val endStr = "%02d%02d%02d".format(windowEnd.hour, windowEnd.minute, 0)

        val node: JsonNode? = http.getJson(
            path = "/uapi/domestic-stock/v1/quotations/inquire-time-itemchartprice",
            query = mapOf(
                "FID_COND_MRKT_DIV_CODE" to "J",
                "FID_INPUT_ISCD" to symbol6,
                "FID_INPUT_HOUR_1" to endStr
            ),
            trId = "FHKST03010200",             // 당일 분봉
            headers = mapOf("tr_cont" to "")    // 최초 호출
        )

        // 응답 위치(output2 혹은 response.body.items.item)
        val arr = node?.path("output2")
            ?: node?.path("response")?.path("body")?.path("items")?.path("item")

        if (arr == null || arr.isMissingNode || arr.isNull) return emptyList()

        val today = LocalDate.now(KST)

        // 배열/단건 모두 처리
        val nodes = mutableListOf<JsonNode>().apply {
            if (arr.isArray) arr.forEach { add(it) } else add(arr)
        }

        return nodes.mapNotNull { n ->
            // 시간(HHmmss) 파싱
            val hhmmss = n.path("stck_cntg_hour").asText()
                .ifBlank { n.path("stck_bsop_time").asText() }
            if (hhmmss.isBlank() || hhmmss.length < 6) return@mapNotNull null

            val time = try {
                LocalTime.of(
                    hhmmss.substring(0, 2).toInt(),
                    hhmmss.substring(2, 4).toInt(),
                    hhmmss.substring(4, 6).toInt()
                )
            } catch (_: Exception) {
                return@mapNotNull null
            }

            val ts: OffsetDateTime = ZonedDateTime.of(today, time, KST).toOffsetDateTime()

            // 숫자 필드 파서 (로컬 함수는 값만 반환)
            fun bd(name: String): BigDecimal? =
                n.path(name).asText().takeIf { it.isNotBlank() }?.toBigDecimalOrNull()

            // 필수값은 람다 본문에서 라벨리턴으로 필터
            val open  = bd("stck_oprc") ?: return@mapNotNull null
            val close = bd("stck_clpr") ?: bd("stck_prpr") ?: return@mapNotNull null
            val high  = bd("stck_hgpr") ?: open
            val low   = bd("stck_lwpr") ?: open

            // 분 체결량(없으면 0)
            val volume = n.path("cntg_vol").asLong(-1).let { if (it >= 0) it else 0L }

            MinuteTick(
                tsKst = ts,
                open = open,
                high = high,
                low = low,
                close = close,
                volume = volume
            )
        }.filter { tick ->
            val t = tick.tsKst.toLocalTime()
            !t.isBefore(windowStart) && t.isBefore(windowEnd)
        }
    }
}
