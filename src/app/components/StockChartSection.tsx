// app/components/StockChartSection.tsx
"use client";

import { useState } from "react";
import StockChart from "@/app/components/StockChart";
import type { CandleDTO } from "@/types/candle";
import getStockData from "@/lib/getStockData";

/**
 * @fileoverview
 * @filename StockChartSection.tsx
 * @author zia
 * @version 1.0.0 - 2025. 11. 16.
 * @copyright 2025,
 */

type Props = {
    code: string;
    initialCandles: CandleDTO[];
};

export default function StockChartSection({ code, initialCandles }: Props) {
    const [candles, setCandles] = useState<CandleDTO[]>(initialCandles);

    const handleRangeChange = async ({
                                         from,
                                         to,
                                     }: {
        from: number;
        to: number;
    }) => {
        console.log("from(ms):", from, "=>", new Date(from));
        console.log("to(ms):", to, "=>", new Date(to));

        try {
            // 여기서 getStockData 사용 (from/to는 ms라 그냥 넘겨도 됨)
            const next = await getStockData(code, from, to);
            setCandles(next);
        } catch (e) {
            console.error("캔들 재조회 실패:", e);
        }
    };

    return (
        <StockChart
            candles={candles}
            onRangeChange={handleRangeChange}
            theme="light"
            height={540}
        />
    );
}
