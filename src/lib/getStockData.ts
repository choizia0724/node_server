// lib/getStockData.ts
import { CandleDTO } from "@/types/candle";
import { stockSearchRequest } from "@/types/stockSearchRequest";

/**
 * Date | number(ms) | string 을 "yyyy-MM-dd HH:mm:ss" 형태로 변환.
 */
function toDateTimeString(input: Date | number | string): string {
    if (typeof input === "string") return input;

    const d =
        input instanceof Date
            ? input
            : new Date(input); // number(ms) → Date

    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, "0");
    const dd = String(d.getDate()).padStart(2, "0");
    const hh = String(d.getHours()).padStart(2, "0");
    const mi = String(d.getMinutes()).padStart(2, "0");
    const ss = String(d.getSeconds()).padStart(2, "0");

    return `${yyyy}-${mm}-${dd} ${hh}:${mi}:${ss}`;
}

/**
 * 코드별 캔들 데이터를 받아옵니다.
 *
 * @param code 종목 코드
 * @param from 조회 시작 시각 (Date | 타임스탬프(ms) | "yyyy-MM-dd HH:mm:ss")
 * @param to   조회 종료 시각 (Date | 타임스탬프(ms) | "yyyy-MM-dd HH:mm:ss")
 */
export default async function getStockData(
    code: string,
    from: Date | number | string,
    to: Date | number | string
): Promise<CandleDTO[]> {
    const base = process.env.NEXT_PUBLIC_API_BASE;
    if (!base) {
        throw new Error("NEXT_PUBLIC_API_BASE 환경변수가 설정되어 있지 않습니다.");
    }

    const url = `/api/stocks/search/${code}`;

    const fromStr = toDateTimeString(from);
    const toStr = toDateTimeString(to);

    const body: stockSearchRequest = {
        symbol: code,
        name: "",
        mrktctg: "",
        from: fromStr,
        to: toStr,
        page: 1,
        useornot: true,
    };

    console.log("[getStockData] body:", body);

    // 서버/클라 둘 다에서 쓰고 싶기 때문에
    // next: { revalidate } 옵션은 서버에서만 넣도록 분기
    const isServer = typeof window === "undefined";

    const fetchInit: RequestInit & {
        next?: { revalidate: number };
    } = {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
    };

    if (isServer) {
        fetchInit.next = { revalidate: 300 }; // 서버에서만 의미 있음
    }

    const res = await fetch(url, fetchInit as any);

    if (!res.ok) {
        const msg = await res.text().catch(() => "");
        throw new Error(
            `getStockData failed: ${res.status} ${res.statusText} ${msg}`
        );
    }

    const data = await res.json();
    console.log("[getStockData] response:", data);

    const rows: unknown = Array.isArray(data) ? data : data?.tick;
    return rows as CandleDTO[];
}
