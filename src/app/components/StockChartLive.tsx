// app/components/StockChartLive.tsx
"use client";

import { useEffect, useRef, useState } from "react";
import StockChart from "./StockChart";
import type { CandleDTO } from "@/types/candle";

/** KIS 체결 틱 파서 (간단 버전)
 *  - 실시간 패킷: "0|H0STCNT0|...|<payload>"
 *  - payload: "^" 구분. 일반적으로 [0]=종목코드, [1]=HHmmss, [3]~ 체결가/등락 등.
 *  - 정확 인덱스는 TR 문서 기준으로 조정하세요.
 */
function parseKisTrade(raw: string) {
    if (!raw) return null;
    if (raw[0] === "{") return null; // 제어/성공 JSON 등은 스킵
    const segs = raw.split("|");
    if (segs.length < 4) return null;
    const trid = segs[1];
    if (trid !== "H0STCNT0") return null; // 체결 채널만 처리
    const payload = segs[3];
    const F = payload.split("^");

    const code = F[0];
    // KIS 문서 기준으로 체결가/체결수량 인덱스 확인 필요
    const price = Number(F[3] ?? NaN);  // ★ 필요시 인덱스 보정
    const volume = Number(F[9] ?? 0);   // ★ 필요시 인덱스 보정
    if (!code || !Number.isFinite(price)) return null;
    return { code, price, volume };
}

type Props = {
    code: string;
    initial: CandleDTO[];
};

export default function StockChartLive({ code, initial }: Props) {
    const [candles, setCandles] = useState<CandleDTO[]>(initial);
    const wsRef = useRef<WebSocket | null>(null);

    useEffect(() => {
        const base = process.env.NEXT_PUBLIC_WS_URL!;
        const ws = new WebSocket(`${base}/ws/pass`);
        wsRef.current = ws;

        ws.onopen = () => {
            // 네 passThrough가 지원하는 간단 포맷: { type, symbol, op }
            ws.send(JSON.stringify({ type: "trade", symbol: code, op: "subscribe" }));
        };

        ws.onmessage = (ev) => {
            const s = String(ev.data || "");
            const tick = parseKisTrade(s);
            if (!tick || tick.code !== code) return;

            // 마지막(오늘) 일봉 갱신: close/high/low(/volume)
            setCandles((prev) => {
                if (prev.length === 0) return prev;
                const next = [...prev];
                const last = next[next.length - 1];
                const p = tick.price;

                next[next.length - 1] = {
                    ...last,
                    close: p,
                    high: Math.max(last.high, p),
                    low: Math.min(last.low, p),
                    // volume은 틱마다 "증가분"인지 "누적"인지 환경마다 달라서
                    // 여기선 보수적으로 증가분으로 처리 (필요시 보정)
                    volume: last.volume + (Number.isFinite(tick.volume) ? tick.volume : 0),
                };
                return next;
            });
        };

        ws.onerror = () => {
            // 조용히 실패 허용
        };

        ws.onclose = () => {
            wsRef.current = null;
        };

        return () => {
            try {
                // 구독 해제 후 종료
                ws.send(JSON.stringify({ type: "trade", symbol: code, op: "unsubscribe" }));
                ws.close();
            } catch {}
        };
    }, [code]);

    return <StockChart candles={candles} />;
}
