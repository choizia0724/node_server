// components/StockChart.tsx
"use client";

import dynamic from "next/dynamic";
import { useMemo } from "react";
import type { ApexOptions } from "apexcharts";
import {CandleDTO} from "@/types/candle";

const ReactApexChart = dynamic(() => import("react-apexcharts"), { ssr: false });

type Props = {
    candles: CandleDTO[];
    height?: number;
    theme?: "light" | "dark";
    className?: string;
    onRangeChange?: (range: { from: number; to: number }) => void;
};



function toMs(t: unknown) {
    if (t instanceof Date) return t.getTime();
    if (typeof t === "number") return t < 1e12 ? t * 1000 : t; // sec→ms
    const ms = Date.parse(String(t));
    return Number.isNaN(ms) ? Date.now() : ms;
}

export default function StockChart({
                                       candles,
                                       height = 520,
                                       theme = "light",
                                       className,
                                   }: Props) {
    const { ohlc, vol, yMin, yMax } = useMemo(() => {
        console.log(candles)
        const o = candles.map((c) => ({
            x: toMs((c as any).tsKst),
            y: [(c as any).open, (c as any).high, (c as any).low, (c as any).close],
        }));
        const v = candles.map((c) => ({
            x: toMs((c as any).tsKst),
            y: (c as any).volume ?? 0,
            fillColor:
                (c as any).close >= (c as any).open ? "#26a69a" : "#ef5350",
        }));
        const lows = candles.map((c) => (c as any).low);
        const highs = candles.map((c) => (c as any).high);
        const lo = lows.length ? Math.min(...lows) : 0;
        const hi = highs.length ? Math.max(...highs) : 1;
        const pad = (hi - lo) * 0.02;
        return { ohlc: o, vol: v, yMin: lo - pad, yMax: hi + pad };
    }, [candles]);

    const shared: ApexOptions = useMemo(
        () => ({
            chart: {
                id: "stocks-shared",
                group: "stocks",
                animations: { enabled: false },
                toolbar: {
                    show: true,
                    tools: {
                        download: true,
                        selection: true,
                        zoom: true,
                        pan: true,
                        reset: true,
                    },
                },
                zoom: { enabled: true, type: "x", autoScaleYaxis: true },
            },
            theme: { mode: theme },
            xaxis: {
                type: "datetime",
                tooltip: { enabled: false },
                labels: {

                    datetimeUTC: false as any,
                    formatter: (value: string) => {

                        const ts = Number(value);
                        if (!Number.isFinite(ts)) return value;

                        const d = new Date(ts); // 브라우저 로컬 타임(KST) 기준
                        return d.toLocaleString("ko-KR", {
                            month: "2-digit",
                            day: "2-digit",
                            hour: "2-digit",
                            minute: "2-digit",
                            hour12: false,
                        });
                    },
                },
            },
            grid: { strokeDashArray: 3 },
            tooltip: { shared: false, followCursor: true },
        }),
        [theme]
    );


    const candleOptions: ApexOptions = useMemo(
        () => ({
            ...shared,
            chart: { ...shared.chart!, type: "candlestick", height: Math.round(height * 0.7) },
            yaxis: { tooltip: { enabled: true }, min: yMin, max: yMax },
            plotOptions: {
                candlestick: {
                    colors: { upward: "#26a69a", downward: "#ef5350" },
                    wick: { useFillColor: true },
                },
            },
        }),
        [shared, height, yMin, yMax]
    );

    const volumeOptions: ApexOptions = useMemo(
        () => ({
            ...shared,
            chart: { ...shared.chart!, type: "bar", height: Math.round(height * 0.3), toolbar: { show: false } },
            yaxis: {
                decimalsInFloat: 0,
                labels: {
                    formatter: (v: number) =>
                        v >= 1_000_000 ? `${Math.round(v / 1_000_000)}M` :
                            v >= 1_000 ? `${Math.round(v / 1_000)}K` : `${v}`,
                },
            },
            plotOptions: { bar: { columnWidth: "75%" } },
            dataLabels: { enabled: false },
            stroke: { show: false },
            tooltip: { y: { formatter: (v: number) => `${v?.toLocaleString?.() ?? v}` } },
        }),
        [shared, height]
    );

    if (!candles?.length) {
        return (
            <div className={`w-full rounded-xl border p-6 text-sm text-gray-500 ${className ?? ""}`}>
                표시할 캔들 데이터가 없습니다.
            </div>
        );
    }

    return (
        <div className={`w-full ${className ?? ""}`}>
            <ReactApexChart
                options={candleOptions}
                series={[{ name: "OHLC", type: "candlestick", data: ohlc }]}
                type="candlestick"
                height={Math.round(height * 0.7)}
            />
            <div className="-mt-2" />
            <ReactApexChart
                options={volumeOptions}
                series={[{ name: "Volume", type: "bar", data: vol }]}
                type="bar"
                height={Math.round(height * 0.3)}
            />
        </div>
    );
}
