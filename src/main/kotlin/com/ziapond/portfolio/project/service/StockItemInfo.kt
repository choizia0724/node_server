package com.ziapond.portfolio.project.service


import com.fasterxml.jackson.databind.JsonNode
import com.ziapond.portfolio.project.domain.StockTable
import com.ziapond.portfolio.project.repository.StockMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * @fileoverview KRX 상장종목 기본정보 수집 서비스
 * @filename StockItemInfo.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 1.
 * @copyright 2025,
 */

@Service
class StockItemInfo(
    builder: RestClient.Builder,                // RestClient.Builder를 주입받아 baseUrl 지정
    private val stockMapper: StockMapper,       // ← 생성자 주입으로 해결
    @Value("\${API_KEY}") private val apiKey: String
) {
    private val rest: RestClient = builder
        .baseUrl("https://apis.data.go.kr")
        .build()

    private val yyyymmdd: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    /** KRX 상장종목 정보 조회 → 도메인 리스트 반환 */
    fun getStockItemName(beginBasDt: LocalDate, numOfRows: Int = 3000): List<StockTable> {
        val node: JsonNode = rest.get()
            .uri { b ->
                b.path("/1160100/service/GetKrxListedInfoService/getItemInfo")
                    .queryParam("serviceKey", apiKey)
                    .queryParam("beginBasDt", beginBasDt.format(yyyymmdd))
                    .queryParam("resultType", "json")
                    .queryParam("numOfRows", numOfRows)
                    .build()
            }
            .retrieve()
            .body(JsonNode::class.java) ?: return emptyList()

        val items = node.path("response").path("body").path("items").path("item")

        // 배열/단건/없음 모두 대응
        return when {
            items.isArray -> items.map { it.toStockTable() }
            items.isMissingNode || items.isNull -> emptyList()
            else -> listOf(items.toStockTable())
        }
    }

    /** 조회 + DB 업서트까지 한 번에 */
    fun fetchAndUpsert(beginBasDt: LocalDate, numOfRows: Int = 3000) {
        val rows = getStockItemName(beginBasDt, numOfRows)
        if (rows.isNotEmpty()) {
            stockMapper.upsertStocks(rows)  // MyBatis 배치 업서트
        }
    }

    /** JsonNode → StockTable 매핑 보조 */
    private fun JsonNode.toStockTable(): StockTable =
        StockTable(
            symbol  = this.path("srtnCd").asText(),
            name    = this.path("itmsNm").asText(),
            basdt   = LocalDate.parse(this.path("basDt").asText(), yyyymmdd),
            isincd  = this.path("isinCd").asText(null),
            mrktctg = this.path("mrktCtg").asText(null),
            crno    = this.path("crno").asText(null),
            corpnm  = this.path("corpNm").asText(null)
        )
}