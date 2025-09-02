// app/stocks/[code]/page.tsx
import StockChart from "@/app/components/StockChart";
import axios from "axios";
import {CandleDTO} from "@/types/candle";

function toUtcTs(time: string | number) {
    return Math.floor(new Date(time).getTime() / 1000) as any;
}

export default async function Page({params}: { params:Promise<{ code: string }> }) {
    const candles: CandleDTO[] = [];
    const {code} = await params

    // 초기 분봉 데이터 가져오기
    await axios.get(
        `${process.env.NEXT_PUBLIC_API_BASE}/api/stock/${code}/intraday`,
        { headers: { "Cache-Control": "no-store" } }
    ).then((data)=>{
        console.log(data)

        for(let d in data){
            candles.push(d)
        }

    });

    return (
        <div className="p-4">
            <h1 className="text-xl font-bold mb-2">{code} 차트</h1>
            <StockChart candles={candles} />
        </div>
    );
}
