// app/stocks/[code]/page.tsx
import axios from "axios";
import type { CandleDTO } from "@/types/candle";
import type { dailySearchRequest } from "@/types/dailySearchRequest";
import StockChartLive from "@/app/components/StockChartLive";

// ─── 유틸 ──────────────────────────────────────────────────────────────────────
const ymdTodayKST = () => {
    const parts = new Intl.DateTimeFormat("en-CA", {
        timeZone: "Asia/Seoul",
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
    }).formatToParts(new Date());
    const map = Object.fromEntries(parts.map(p => [p.type, p.value]));
    return `${map.year}${map.month}${map.day}`; // YYYYMMDD
};

const parseYmd = (s: string) => new Date(+s.slice(0, 4), +s.slice(4, 6) - 1, +s.slice(6, 8));
const addDaysYmd = (s: string, delta: number) => {
    const d = parseYmd(s);
    d.setDate(d.getDate() + delta);
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    return `${y}${m}${day}`;
};

const toUtcSec = (yyyymmdd: string, hhmmss = "000000") => {
    const y = +yyyymmdd.slice(0, 4);
    const m = +yyyymmdd.slice(4, 6);
    const d = +yyyymmdd.slice(6, 8);
    const hh = +hhmmss.slice(0, 2);
    const mm = +hhmmss.slice(2, 4);
    const ss = +hhmmss.slice(4, 6);
    return Math.floor(Date.UTC(y, m - 1, d, hh, mm, ss) / 1000);
};

type KISDailyRow = {
    stck_bsop_date: string; // YYYYMMDD
    stck_cntg_hour?: string; // HHmmss | undefined
    stck_oprc: string;
    stck_hgpr: string;
    stck_lwpr: string;
    stck_clpr: string;
    acml_vol: string;
};

const fetchDailyChunk = async (url: string, body: dailySearchRequest) => {
    const res = await axios.post(url, body, {
        headers: {
            "Cache-Control": "no-store",
            "content-type": "application/json",
        },
        timeout: 10000,
    });
    const rows: KISDailyRow[] = res.data?.output2 ?? [];
    return rows.map((d) => ({
        time: toUtcSec(d.stck_bsop_date, d.stck_cntg_hour || "000000"),
        open: Number(d.stck_oprc),
        high: Number(d.stck_hgpr),
        low: Number(d.stck_lwpr),
        close: Number(d.stck_clpr),
        volume: Number(d.acml_vol),
    })) as CandleDTO[];
};

const fetchDailyPaged = async (
    url: string,
    baseBody: dailySearchRequest,
    options?: { chunkDays?: number; maxChunks?: number }
) => {
    const chunkDays = options?.chunkDays ?? 180; // 한 번에 6개월
    const maxChunks = options?.maxChunks ?? 4;   // 최대 4번(≈ 2년치)
    const startAll = baseBody.date1;
    let end = baseBody.date2;

    const bag: CandleDTO[] = [];

    for (let i = 0; i < maxChunks; i++) {
        const start = (() => {
            const cand = addDaysYmd(end, -chunkDays + 1); // end 포함
            return cand < startAll ? startAll : cand;
        })();

        const body: dailySearchRequest = {
            ...baseBody,
            date1: start,
            date2: end,
        };

        const chunk = await fetchDailyChunk(url, body);
        bag.push(...chunk);

        if (start === startAll) break;
        end = addDaysYmd(start, -1); // 다음 루프는 이전 구간으로
    }

    // dedup by time, then sort asc
    const dedup = Array.from(
        new Map(bag.map((c) => [c.time, c])).values()
    ).sort((a, b) => a.time - b.time);

    return dedup;
};

// ─── 페이지 컴포넌트 ───────────────────────────────────────────────────────────
export default async function Page({ params }: { params: Promise<{ code: string }> }) {
    const { code } = await params;

    // 조회 파라미터
    // 원하는 기간만 바꾸면 됨. 끝 날짜는 오늘(KST)로 자동.
    const dailyBody: dailySearchRequest = {
        hour: "130000",              // 일봉은 크게 의미 없지만 유지
        date1: "20240901",           // 시작일(원하는 만큼 과거로)
        date2: ymdTodayKST(),        // 종료일 = 오늘
        divCode: "D",                // 일봉
        adj: "1",                    // 수정주가
    };

    const base = process.env.NEXT_PUBLIC_API_BASE!;
    const dailyUrl = `${base}/proxy/stocks/search/${code}`;

    // 여러 번 호출해서 100개 이상 확보
    const dailyCandles: CandleDTO[] = await fetchDailyPaged(dailyUrl, dailyBody, {
        chunkDays: 180, // 6개월 단위로 끊어서
        maxChunks: 6,   // 최대 6번(≈ 3년치) — 필요에 따라 늘리면 됨
    });

    // 참고: 투자자 현재가(로그만)
    try {
        await axios
            .get(`${base}/proxy/stocks/investor/${code}`, { timeout: 8000 })
            .then((r) => console.log("investor:", r.data));
    } catch (e) {
        console.warn("investor fetch skipped:", (e as any)?.message);
    }

    return (
        <div className="p-4">
            <h1 className="text-xl font-bold mb-2">{code} 차트</h1>
            <StockChartLive code={code} initial={dailyCandles} />
        </div>
    );
}
