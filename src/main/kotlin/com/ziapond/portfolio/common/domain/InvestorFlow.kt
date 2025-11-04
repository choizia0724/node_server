package com.ziapond.portfolio.common.domain

import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * @fileoverview
 * @filename InvestorFlow.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 3.
 * @copyright 2025,
 */


data class InvestorFlow(
    val symbol: String,

    val stck_bsop_date: String,
    val stck_clpr: String,
    val prdy_vrss: String,
    val prdy_vrss_sign: String,

    val prsn_ntby_qty: String,
    val frgn_ntby_qty: String,
    val orgn_ntby_qty: String,

    val prsn_ntby_tr_pbmn: String,
    val frgn_ntby_tr_pbmn: String,
    val orgn_ntby_tr_pbmn: String,

    val prsn_shnu_vol: String,
    val frgn_shnu_vol: String,
    val orgn_shnu_vol: String,

    val prsn_shnu_tr_pbmn: String,
    val frgn_shnu_tr_pbmn: String,
    val orgn_shnu_tr_pbmn: String,

    val prsn_seln_vol: String,
    val frgn_seln_vol: String,
    val orgn_seln_vol: String,

    val prsn_seln_tr_pbmn: String,
    val frgn_seln_tr_pbmn: String,
    val orgn_seln_tr_pbmn: String
)