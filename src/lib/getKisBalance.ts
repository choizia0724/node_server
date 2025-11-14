import {KisInquireBalanceRequest, KisInquireBalanceResponse} from "@/types/kisBalanceTypes";

/**
 * @fileoverview
 * @filename getKisBalance.ts
 * @author zia
 * @version 1.0.0 - 2025. 11. 15.
 * @copyright 2025,
 */
// src/api/kisBalanceApi.ts

const API_BASE =
    process.env.NEXT_PUBLIC_API_BASE?.replace(/\/+$/, "") ?? "";

/**
 * /kis/balance 호출 함수
 */
export async function getKisBalance(
    params: KisInquireBalanceRequest
): Promise<KisInquireBalanceResponse> {
    const url = `/api/kis/balance${buildQueryString(params)}`;

    const res = await fetch(url, {
        method: "GET",
        headers: {
            Accept: "application/json",
        },
    });

    if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(
            `Failed to fetch /kis/balance: ${res.status} ${res.statusText} ${text}`
        );
    }

    return (await res.json()) as KisInquireBalanceResponse;
}

function buildQueryString(params: KisInquireBalanceRequest): string {
    const search = new URLSearchParams();

    search.set("cano", params.cano);
    search.set("acntPrdtCd", params.acntPrdtCd);

    if (params.afhrFlprYn) search.set("afhrFlprYn", params.afhrFlprYn);
    if (params.oflYn) search.set("oflYn", params.oflYn);
    if (params.inqrDvsn) search.set("inqrDvsn", params.inqrDvsn);
    if (params.unprDvsn) search.set("unprDvsn", params.unprDvsn);
    if (params.fundSttlIcldYn)
        search.set("fundSttlIcldYn", params.fundSttlIcldYn);
    if (params.fncgAmtAutoRdptYn)
        search.set("fncgAmtAutoRdptYn", params.fncgAmtAutoRdptYn);
    if (params.prcsDvsn) search.set("prcsDvsn", params.prcsDvsn);
    if (params.ctxAreaFk100) search.set("ctxAreaFk100", params.ctxAreaFk100);
    if (params.ctxAreaNk100) search.set("ctxAreaNk100", params.ctxAreaNk100);

    const qs = search.toString();
    return qs ? `?${qs}` : "";
}
