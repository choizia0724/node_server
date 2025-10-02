package com.ziapond.portfolio.project.service

data class MinuteTick(
    val tsKst: java.time.OffsetDateTime,
    val stckBsopDate: java.time.LocalDate,
    val stckOprc: java.math.BigDecimal?,
    val stckHgpr: java.math.BigDecimal?,
    val stckLwpr: java.math.BigDecimal?,
    val stckClpr: java.math.BigDecimal?,
    val acmlVol: Long?,

    val prdyVrssVolRate: java.math.BigDecimal?,
    val prdyVrss: java.math.BigDecimal?,
    val prdyVrssSign: String?,
    val prdyCtrt: java.math.BigDecimal?,
    val htsFrgnEhrt: java.math.BigDecimal?,
    val frgnNtbyQty: Long?,
    val flngClsCode: String?,
    val acmlPrttRate: java.math.BigDecimal?
)