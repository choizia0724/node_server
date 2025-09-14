// app/components/StockChartLive.tsx
"use client";

import { useEffect, useRef, useState } from "react";
import StockChart from "./StockChart";
import type { CandleDTO } from "@/types/candle";

/**
 * KIS 체결 틱 파서 (간단 버전)
 * 패킷: "0|H0STCNT0|...|<payload>"
 * payload는 '^' 구분: [0]=종목코드, [1]=HHmmss, (체결가/거래량 인덱스는 환경/문서에 맞게 조정)
 */
function parseKisTrade(raw: string): { code: string; price: number; volume: number } | null {
    if (!raw || raw[0] === "{") return null; // 제어/성공 JSON은 스킵
    const segs = raw.split("|");
    if (segs.length < 4) return null;
    const trid = segs[1];
    if (trid !== "H0STCNT0") return null; // 체결 채널만 처리
    const payload = segs[3];
    const F = payload.split("^");

    const code = F[0];
    // ⬇️ 필요 시 인덱스 보정 (문서 확인)
    const price = Number(F[3] ?? NaN);
    const volume = Number(F[9] ?? 0);

    if (!code || !Number.isFinite(price)) return null;
    return { code, price, volume: Number.isFinite(volume) ? volume : 0 };
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
            // passThrough가 지원하는 간단 포맷
            ws.send(JSON.stringify({ type: "trade", symbol: code, op: "subscribe" }));
        };

        ws.onmessage = (ev) => {
            const s = String(ev.data ?? "");
            const tick = parseKisTrade(s);
            if (!tick || tick.code !== code) return;

            // 마지막(오늘) 일봉 갱신: close/high/low/volume
            setCandles((prev) => {
                if (prev.length === 0) return prev;
                const next = [...prev];
                const last = next[next.length - 1];
                const p = tick.price;

                const baseVol =
                    typeof (last as CandleDTO).volume === "number" && Number.isFinite((last as CandleDTO).volume)
                        ? (last as CandleDTO).volume
                        : 0;
                const volDelta = Number.isFinite(tick.volume) ? tick.volume : 0;

                next[next.length - 1] = {
                    ...last,
                    close: p,
                    high: Math.max(last.high, p),
                    low: Math.min(last.low, p),
                    volume: baseVol + volDelta,
                } as CandleDTO;

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
                ws.send(JSON.stringify({ type: "trade", symbol: code, op: "unsubscribe" }));
                ws.close();
            } catch {
                /* noop */
            }
        };
    }, [code]);

    return <StockChart candles={candles} />;
}
