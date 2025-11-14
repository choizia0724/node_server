package com.ziapond.portfolio.project.web.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank

/**
 * [TTTC8434R] 주식잔고조회 Request (QueryString용 DTO)
 *
 * 실제 KIS 요청 시엔 이 DTO를 쿼리 파라미터로 변환해서 사용.
 */
data class KisInquireBalanceRequest(
    /** 종합계좌번호 (앞 8자리) */
    @field:NotBlank
    @JsonProperty("CANO")
    val cano: String,

    /** 계좌상품코드 (뒤 2자리, 보통 "01") */
    @field:NotBlank
    @JsonProperty("ACNT_PRDT_CD")
    val acntPrdtCd: String,

    /** 시간외단일가여부 (N:기본값, Y:시간외단일가) */
    @JsonProperty("AFHR_FLPR_YN")
    val afhrFlprYn: String = "N",

    /** 오프라인여부 (공란 기본) */
    @JsonProperty("OFL_YN")
    val oflYn: String = "",

    /**
     * 조회구분
     * 00: 전체, 01: 대출일별, 02: 종목별
     */
    @JsonProperty("INQR_DVSN")
    val inqrDvsn: String = "02",

    /**
     * 단가구분
     * 01: 기본값
     */
    @JsonProperty("UNPR_DVSN")
    val unprDvsn: String = "01",

    /** 펀드결제분포함여부 (N:포함 안함, Y:포함) */
    @JsonProperty("FUND_STTL_ICLD_YN")
    val fundSttlIcldYn: String = "N",

    /** 융자금액자동상환여부 (N:기본값) */
    @JsonProperty("FNCG_AMT_AUTO_RDPT_YN")
    val fncgAmtAutoRdptYn: String = "N",

    /**
     * 처리구분
     * 00: 전일매매포함, 01: 전일매매미포함
     */
    @JsonProperty("PRCS_DVSN")
    val prcsDvsn: String = "01",

    /** 연속조회검색조건100 (첫 조회 시 공란) */
    @JsonProperty("CTX_AREA_FK100")
    val ctxAreaFk100: String = "",

    /** 연속조회키100 (첫 조회 시 공란) */
    @JsonProperty("CTX_AREA_NK100")
    val ctxAreaNk100: String = ""
)
