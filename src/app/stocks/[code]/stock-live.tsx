// app/stocks/[code]/stock-live.tsx
"use client";
import { useEffect } from "react";
import StockChart from "@/app/components/StockChart";

export default function StockLive({
                                      code,
                                      initial,
                                  }: {
    code: string;
    initial: any[];
}) {
    // 기본 차트 데이터
    const candles = initial;

    useEffect(() => {
        const ws = new WebSocket(
            `${process.env.NEXT_PUBLIC_WS_BASE}/ws/pass?symbol=${code}`
        );

        ws.onmessage = (msg) => {
            try {
                const data = JSON.parse(msg.data);
                console.log("📈 live tick:", data);
                // TODO: StockChart updateLastBar 호출 연결 필요
            } catch (e) {
                console.error("WS error:", e);
            }
        };

        return () => {
            ws.close();
        };
    }, [code]);

    return <StockChart candles={candles} />;
}
