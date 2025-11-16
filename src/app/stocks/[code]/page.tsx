// app/stocks/[code]/page3.tsx
import getStockData from "@/lib/getStockData";
import StockChartSection from "@/app/components/StockChartSection";

type PageProps = { params: Promise<{ code: string }> };


export default async function Page({ params }: PageProps) {
    const { code } = await params;

    const now = new Date();
    const twoDaysAgo = new Date(now);
    twoDaysAgo.setDate(now.getDate() - 2);

    const candles = await getStockData(code, twoDaysAgo, now);

    return (
        <div className="p-6 space-y-4">
            <h1 className="text-xl font-semibold">종목: {code}</h1>
            <StockChartSection code={code} initialCandles={candles} />

        </div>
    );
}
