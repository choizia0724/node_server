// lib/getStockData.ts
import "server-only";
import {CandleDTO} from "@/types/candle";
import {stockSearchRequest} from "@/types/stockSearchRequest";
/**
 * 코드별 일봉 캔들 데이터를 받아옵니다.
 * API 응답이 CandlesDTO[] 그대로라면 그대로 반환하고,
 * { items: CandlesDTO[] } / { data: CandlesDTO[] } 형태도 허용합니다.
 */
export default async function getStockData(code: string): Promise<CandleDTO[]> {
    const url = process.env.NEXT_PUBLIC_API_BASE + "/api/stocks/search/"+code

    function yyyymmdd(d = new Date()) {
        const yyyy = d.getFullYear();
        const mm = String(d.getMonth() + 1).padStart(2, "0");
        const dd = String(d.getDate()).padStart(2, "0");
        return `${yyyy}-${mm}-${dd} 00:00:00`;
    }

    const now = new Date();
    const today = yyyymmdd(now);

    const yesterdayDate = new Date(now);
    yesterdayDate.setDate(now.getDate() - 2);
    const yesterday = yyyymmdd(yesterdayDate);
    //console.log("yesterday: "+yesterday+", today: "+today)

    const body: stockSearchRequest = {
        symbol: code,
        name: "",
        mrktctg: "",
        from: yesterday,
        to: today,
        page: 1,
        useornot: true
    };
    console.log(body)

    const res = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
        // 일봉은 수분 캐시 권장(원하면 조정)
        next: { revalidate: 300 },
    });

    console.log(res)
    if (!res.ok) {
        const msg = await res.text().catch(() => "");
        throw new Error(`getStockData failed: ${res.status} ${res.statusText} ${msg}`);
    }

    const data = await res.json();
    console.log(data)
    const rows: unknown =
        Array.isArray(data) ? data : data?.tick;

    return rows as CandleDTO[];
}
