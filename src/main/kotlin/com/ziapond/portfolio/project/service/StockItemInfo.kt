package com.ziapond.portfolio.project.service

import com.fasterxml.jackson.databind.JsonNode
import com.ziapond.portfolio.project.domain.StockTable
import com.ziapond.portfolio.project.mappers.StockListMapper
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
    private val builder: RestClient.Builder,
    private val stockMapper: StockListMapper,
    @Value("\${api.key}") private val api_key: String
) {
    // 주입 완료 후 접근되도록 lazy
    private val rest: RestClient by lazy {
        builder.baseUrl("https://apis.data.go.kr").build()
    }

    private val yyyymmdd = DateTimeFormatter.ofPattern("yyyyMMdd")

    /** api 서버에서 가져오기*/
    fun getStockItemName(beginBasDt: LocalDate, numOfRows: Int = 3000): List<StockTable> {
        val node: JsonNode = rest.get()
            .uri { b ->
                b.path("/1160100/service/GetKrxListedInfoService/getItemInfo")
                    .queryParam("serviceKey", api_key)
                    .queryParam("beginBasDt", beginBasDt.format(yyyymmdd))
                    .queryParam("resultType", "json")
                    .queryParam("numOfRows", numOfRows)
                    .build()
            }
            .retrieve()
            .body(JsonNode::class.java) ?: return emptyList()

        val items = node.path("response").path("body").path("items").path("item")
        return when {
            items.isArray -> items.map { it.toStockTable() }
            items.isMissingNode || items.isNull -> emptyList()
            else -> listOf(items.toStockTable())
        }
    }

    fun fetchAndUpsert(beginBasDt: LocalDate, numOfRows: Int = 3000) {
        val rows = getStockItemName(beginBasDt, numOfRows)
        if (rows.isNotEmpty()) stockMapper.upsertStocks(rows)
    }

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