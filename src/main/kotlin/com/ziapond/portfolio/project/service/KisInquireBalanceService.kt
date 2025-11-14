package com.ziapond.portfolio.project.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ziapond.portfolio.project.web.dto.KisInquireBalanceRequest
import com.ziapond.portfolio.project.web.dto.KisInquireBalanceResponse
import com.ziapond.portfolio.kis.http.KisHttp
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class KisInquireBalanceService(
    private val kisHttp: KisHttp,
    private val objectMapper: ObjectMapper,
    @Value("\${kis.inquire-balance.path}")
    private val path: String,
    @Value("\${kis.inquire-balance.tr-id}")
    private val trId: String
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun getBalance(req: KisInquireBalanceRequest): KisInquireBalanceResponse {
        val query = mapOf(
            "CANO" to req.cano,
            "ACNT_PRDT_CD" to req.acntPrdtCd,
            "AFHR_FLPR_YN" to req.afhrFlprYn,
            "OFL_YN" to req.oflYn,
            "INQR_DVSN" to req.inqrDvsn,
            "UNPR_DVSN" to req.unprDvsn,
            "FUND_STTL_ICLD_YN" to req.fundSttlIcldYn,
            "FNCG_AMT_AUTO_RDPT_YN" to req.fncgAmtAutoRdptYn,
            "PRCS_DVSN" to req.prcsDvsn,
            "CTX_AREA_FK100" to req.ctxAreaFk100,
            "CTX_AREA_NK100" to req.ctxAreaNk100,
        )

        log.info(
            "KIS 잔고조회 요청: path={}, tr_id={}, query={}",
            path, trId, query
        )

        val json = kisHttp.getJson(
            path = path,
            query = query,
            trId = trId,
            auth = true,
            custType = "P"
        ) ?: error("KIS 잔고조회 응답이 null 입니다")

        // rt_cd 체크 (0 아니면 예외)
        val rtCd = json.get("rt_cd")?.asText()
        if (rtCd != "0") {
            val msgCd = json.get("msg_cd")?.asText()
            val msg1  = json.get("msg1")?.asText()
            log.warn("KIS 잔고조회 실패: rt_cd={}, msg_cd={}, msg1={}", rtCd, msgCd, msg1)
            error("KIS 잔고조회 오류: rt_cd=$rtCd, msg_cd=$msgCd, msg1=$msg1")
        }

        // JsonNode → DTO 매핑
        return objectMapper.readValue(json.toString())
    }
}
