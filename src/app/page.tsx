"use client";

import {Fragment, useEffect, useState} from "react";
import { getKisBalance } from "@/lib/getKisBalance";
import type {
    KisInquireBalanceResponse,
    KisInquireBalancePosition,
} from "@/types/kisBalanceTypes";
import getStockData from "@/lib/getStockData";
import {CandleDTO} from "@/types/candle";
import StockChart from "@/app/components/StockChart";

type AccountPreset = {
    title: string;
    cano: string;
    acntPrdtCd: string;
};

const ACCOUNT_PRESETS: AccountPreset[] = [
    {
        title: "일반계좌",
        cano: "73449068",
        acntPrdtCd: "01",
    },
    // {
    //     title: "ISA계좌",
    //     cano: "43486792",
    //     acntPrdtCd: "01"
    // }
];

export default function AccountBalancePage() {
    const [selectedIndex, setSelectedIndex] = useState(0);
    const [currentAccount, setCurrentAccount] = useState<AccountPreset | null>(
        ACCOUNT_PRESETS[0] ?? null
    );
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [data, setData] = useState<KisInquireBalanceResponse | null>(null);

    // 탭 선택 시 호출
    const loadAccount = async (index: number) => {
        const account = ACCOUNT_PRESETS[index];
        setSelectedIndex(index);
        setCurrentAccount(account);
        setError(null);
        setData(null);
        setLoading(true);

        try {
            const res = await getKisBalance({
                cano: account.cano,
                acntPrdtCd: account.acntPrdtCd,
                // 나머지 파라미터는 서버 기본값 사용
            });
            setData(res);
        } catch (e: any) {
            setError(e?.message ?? "잔고 조회 중 오류가 발생했습니다.");
        } finally {
            setLoading(false);
        }
    };

    // 첫 로딩 시 첫 번째 계좌 자동 조회
    useEffect(() => {
        if (ACCOUNT_PRESETS.length > 0) {
            loadAccount(0);
        }
    }, []);

    return (
        <main className="max-w-5xl mx-auto px-4 py-8 space-y-8">
            <header className="space-y-1">
                <h1 className="text-2xl font-semibold">계좌 잔고</h1>
                <p className="text-sm text-gray-500">
                    미리 등록한 계좌 탭을 눌러 잔고와 보유 종목을 조회합니다.
                </p>
            </header>

            {/* 계좌 탭 영역 */}
            <section className="bg-white border rounded-xl p-4 shadow-sm space-y-4">
                <div className="flex flex-wrap gap-2 border-b pb-2">
                    {ACCOUNT_PRESETS.map((acc, idx) => {
                        const isActive = idx === selectedIndex;
                        return (
                            <button
                                key={`${acc.cano}-${acc.acntPrdtCd}`}
                                type="button"
                                onClick={() => loadAccount(idx)}
                                className={[
                                    "px-3 py-1.5 text-sm rounded-t-md border-b-2",
                                    "transition-colors",
                                    isActive
                                        ? "border-blue-500 text-blue-600 bg-blue-50"
                                        : "border-transparent text-gray-600 hover:bg-gray-50",
                                ].join(" ")}
                            >
                                {acc.title}
                            </button>
                        );
                    })}
                </div>

                {/* 현재 선택된 계좌 정보 & 상태 */}
                {currentAccount && (
                    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 text-xs text-gray-500">
                        <div>
              <span className="font-medium text-gray-700">
                {currentAccount.title}
              </span>{" "}
                            · CANO: {currentAccount.cano} / ACNT_PRDT_CD:{" "}
                            {currentAccount.acntPrdtCd}
                        </div>
                        {data && (
                            <div>
                                rt_cd: {data.rt_cd} / msg_cd: {data.msg_cd} ·{" "}
                                {data.msg1 || "조회 결과"}
                            </div>
                        )}
                    </div>
                )}

                {loading && (
                    <p className="text-sm text-gray-600 mt-2">조회 중입니다...</p>
                )}
                {error && (
                    <p className="text-sm text-red-500 mt-2 whitespace-pre-line">
                        {error}
                    </p>
                )}
            </section>

            {/* 결과 영역 */}
            {data && !loading && (
                <>
                    <AccountSummary response={data} />
                    <PositionsTable positions={data.output1} />
                </>
            )}
        </main>
    );
}

// ======================
// 계좌 요약 컴포넌트
// ======================

function AccountSummary({ response }: { response: KisInquireBalanceResponse }) {
    const summary = response.output2[0];
    if (!summary) return null;

    return (
        <section className="bg-white border rounded-xl p-4 shadow-sm space-y-3">
            <h2 className="text-lg font-medium">계좌 요약</h2>
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-sm">
                <SummaryItem label="총 평가금액" value={summary.tot_evlu_amt} />
                <SummaryItem label="순자산" value={summary.nass_amt} />
                <SummaryItem label="매입금액 합계" value={summary.pchs_amt_smtl_amt} />
                <SummaryItem
                    label="평가손익 합계"
                    value={summary.evlu_pfls_smtl_amt}
                />
                <SummaryItem label="자산 증감액" value={summary.asst_icdc_amt} />
                <SummaryItem
                    label="자산 증감 수익률"
                    value={summary.asst_icdc_erng_rt + " %"}
                />
            </div>
        </section>
    );
}

function SummaryItem({ label, value }: { label: string; value: string }) {
    return (
        <div className="space-y-1">
            <div className="text-xs text-gray-500">{label}</div>
            <div className="font-semibold">{formatNumber(value)}</div>
        </div>
    );
}

// ======================
// 종목별 테이블 컴포넌트
// ======================

function PositionsTable({
                            positions,
                        }: {
    positions: KisInquireBalancePosition[];
}) {
    const [expandedCode, setExpandedCode] = useState<string | null>(null);
    const [chartData, setChartData] = useState<Record<string, CandleDTO[]>>({});
    const [loadingCode, setLoadingCode] = useState<string | null>(null);
    const [errorCode, setErrorCode] = useState<string | null>(null);

    if (!positions.length) {
        return (
            <section className="bg-white border rounded-xl p-4 shadow-sm">
                <h2 className="text-lg font-medium mb-2">보유 종목</h2>
                <p className="text-sm text-gray-500">보유 중인 종목이 없습니다.</p>
            </section>
        );
    }

    const handleToggleRow = async (code: string) => {
        // 이미 펼쳐져 있으면 닫기
        if (expandedCode === code) {
            setExpandedCode(null);
            setErrorCode(null);
            return;
        }

        // 새로 펼치기
        setExpandedCode(code);
        setErrorCode(null);

        // 이미 데이터가 있으면 다시 fetch 안 하고 그대로 사용
        if (chartData[code]) return;

        try {
            setLoadingCode(code);

            // 최근 1주일 범위 계산
            const now = new Date();
            const from = new Date(now);
            from.setDate(now.getDate() - 7);

            const candles = await getStockData(code, from, now);
            setChartData((prev) => ({ ...prev, [code]: candles }));
        } catch (e: any) {
            console.error("차트 데이터 조회 실패:", e);
            setErrorCode(code);
        } finally {
            setLoadingCode(null);
        }
    };

    return (
        <section className="bg-white border rounded-xl p-4 shadow-sm overflow-x-auto">
            <h2 className="text-lg font-medium mb-3">보유 종목</h2>
            <table className="min-w-full text-sm border-t">
                <thead className="bg-gray-50">
                <tr>
                    <Th>종목코드</Th>
                    <Th>종목명</Th>
                    <Th className="text-right">수량</Th>
                    <Th className="text-right">매입가</Th>
                    <Th className="text-right">현재가</Th>
                    <Th className="text-right">평가손익</Th>
                    <Th className="text-right">수익률</Th>
                </tr>
                </thead>
                <tbody>
                {positions.map((p) => {
                    const isExpanded = expandedCode === p.pdno;
                    const candles = chartData[p.pdno];

                    return (
                        <Fragment key={p.pdno}>
                            {/* 기본 행 */}
                            <tr className="border-t hover:bg-gray-50">
                                <Td>{p.pdno}</Td>
                                <Td>
                                    <button
                                        type="button"
                                        onClick={() => handleToggleRow(p.pdno)}
                                        className="text-left w-full hover:underline text-blue-600"
                                    >
                                        {p.prdt_name}
                                    </button>
                                </Td>
                                <Td align="right">{formatNumber(p.hldg_qty)}</Td>
                                <Td align="right">{formatNumber(p.pchs_avg_pric)}</Td>
                                <Td align="right">{formatNumber(p.prpr)}</Td>
                                <Td
                                    align="right"
                                    className={numberClass(p.evlu_pfls_amt)}
                                >
                                    {formatNumber(p.evlu_pfls_amt)}
                                </Td>
                                <Td
                                    align="right"
                                    className={numberClass(p.evlu_pfls_rt)}
                                >
                                    {p.evlu_pfls_rt} %
                                </Td>
                            </tr>

                            {/* 확장 행 (차트 영역) */}
                            {isExpanded && (
                                <tr className="border-t bg-gray-50">
                                    <td colSpan={7} className="p-3">
                                        {loadingCode === p.pdno && (
                                            <div className="text-xs text-gray-500">
                                                차트 데이터를 불러오는 중입니다...
                                            </div>
                                        )}

                                        {errorCode === p.pdno && (
                                            <div className="text-xs text-red-500 mb-2">
                                                차트 데이터를 가져오는 중 오류가 발생했습니다.
                                            </div>
                                        )}

                                        {candles && candles.length > 0 && (
                                            <div className="mt-2">
                                                <StockChart
                                                    candles={candles}
                                                    height={360}
                                                    theme="light"
                                                />
                                            </div>
                                        )}

                                        {candles && candles.length === 0 && (
                                            <div className="text-xs text-gray-500">
                                                최근 1주일 간 차트 데이터가 없습니다.
                                            </div>
                                        )}
                                    </td>
                                </tr>
                            )}
                        </Fragment>
                    );
                })}
                </tbody>
            </table>
        </section>
    );
}


function Th({
                children,
                className,
            }: {
    children: React.ReactNode;
    className?: string;
}) {
    return (
        <th
            className={`px-3 py-2 text-left font-medium text-gray-600 whitespace-nowrap ${
                className ?? ""
            }`}
        >
            {children}
        </th>
    );
}

function Td({
                children,
                align,
                className,
            }: {
    children: React.ReactNode;
    align?: "left" | "right" | "center";
    className?: string;
}) {
    return (
        <td
            className={`px-3 py-2 whitespace-nowrap ${
                align === "right"
                    ? "text-right"
                    : align === "center"
                        ? "text-center"
                        : "text-left"
            } ${className ?? ""}`}
        >
            {children}
        </td>
    );
}

// ======================
// 숫자/색상 헬퍼
// ======================

function formatNumber(value: string | number | null | undefined): string {
    if (value === null || value === undefined) return "-";
    const n = Number(value);
    if (Number.isNaN(n)) return String(value);
    return n.toLocaleString("ko-KR");
}

function numberClass(value: string | number | null | undefined): string {
    const n = Number(value);
    if (Number.isNaN(n)) return "";
    if (n > 0) return "text-red-600";
    if (n < 0) return "text-blue-600";
    return "";
}
