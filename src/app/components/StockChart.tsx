// components/StockChart.tsx
"use client";
import { useEffect, useRef } from "react";
import {
    CandlestickSeries,
    ColorType,
    CrosshairMode,
    HistogramSeries,
    createChart,
    type ISeriesApi,
    type UTCTimestamp,
} from "lightweight-charts";
import {CandleDTO} from "@/types/candle";


export default function StockChart({ candles }: { candles: CandleDTO[] }) {
    const containerRef = useRef<HTMLDivElement | null>(null);
    const candleSeriesRef = useRef<ISeriesApi<"Candlestick"> | null>(null);
    const volumeSeriesRef = useRef<ISeriesApi<"Histogram"> | null>(null);
    const chartRef = useRef<ReturnType<typeof createChart> | null>(null);

    useEffect(() => {
        if (!containerRef.current) return;

        const chart = createChart(containerRef.current, {
            width: containerRef.current.clientWidth,   // 최초 width 지정
            height: 420,
            layout: { background: { type: ColorType.Solid, color: "white" }, textColor: "#333" },
            rightPriceScale: { borderVisible: false },
            timeScale: { borderVisible: false },
            crosshair: { mode: CrosshairMode.Normal }, // 1 대신 enum
        });
        chartRef.current = chart;

        // v5: addSeries 사용
        const candle = chart.addSeries(CandlestickSeries, {});
        const volume = chart.addSeries(HistogramSeries, {
            priceScaleId: "",                     // 서브 스케일
            priceFormat: { type: "volume" },      // 볼륨 포맷
        });

        // 서브 스케일 마진
        chart.priceScale("").applyOptions({ scaleMargins: { top: 0.8, bottom: 0 } });

        candleSeriesRef.current = candle;
        volumeSeriesRef.current = volume;

        // 초기 데이터
        candle.setData(
            candles.map(({ time, open, high, low, close }) => ({ time, open, high, low, close }))
        );
        volume.setData(
            candles.map(({ time, volume = 0, open, close }) => ({
                time,
                value: volume,
                color: close >= open ? "#26a69a" : "#ef5350",
            }))
        );

        const handleResize = () =>
            chart.applyOptions({ width: containerRef.current!.clientWidth });
        window.addEventListener("resize", handleResize);
        // 초기 한 번 반영
        handleResize();

        return () => {
            window.removeEventListener("resize", handleResize);
            chart.remove();
        };
    }, [candles]);

    // 실시간 업데이트(원하면 props로 빼서 사용)
    const updateLastBar = (bar: CandleDTO) => {
        candleSeriesRef.current?.update(bar);
        volumeSeriesRef.current?.update({
            time: bar.time,
            value: bar.volume ?? 0,
            color: bar.close >= bar.open ? "#26a69a" : "#ef5350",
        });
    };

    return <div ref={containerRef} className="w-full" />;
}
