// app/stocks/[code]/page.tsx
import getStockData from "@/lib/getStockData";
import StockChart from "@/app/components/StockChart";

type PageProps = { params: Promise<{ code: string }> };


export default async function Page({ params }: PageProps) {
    const { code } = await params;
    const candles = await getStockData(code);

    return (
        <div className="p-6 space-y-4">
            <h1 className="text-xl font-semibold">종목: {code}</h1>
            <StockChart candles={candles} theme="light" height={540} />
        </div>
    );
}
