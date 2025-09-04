// app/stocks/[code]/page.tsx
import StockChart from "@/app/components/StockChart";
import axios from "axios";
import {CandleDTO} from "@/types/candle";
import {dailySearchRequest} from "@/types/dailySearchRequest";
import {getDailyData, getHourlyData} from '@/app/utils/getChartData';


const hhmmss = () => {
    const d = new Date();
    return [d.getHours(), d.getMinutes(), d.getSeconds()]
        .map(n => String(n).padStart(2, "0"))
        .join("");
};


export default async function Page({params}: { params:Promise<{ code: string }> }) {
    const {code} = await params

    const dailyCandles:CandleDTO[]=[];
    const timeCandles:CandleDTO[]=[];

    // 보낼 body
    const dailyBody: dailySearchRequest = {
        hour: "130000",      // 오후 1시
        date1: "20240901",   // 시작일
        date2: "20250931",   // 종료일
        divCode: "D",        // 일봉
        adj: "1",            // 수정주가
    };
    // 국내주식기간별시세(일/주/월/년)[v1_국내주식-016]
    await getDailyData(
        `${process.env.NEXT_PUBLIC_API_BASE}/api/stock/${code}/search/`,
        dailyBody,
        dailyCandles
        );

    // 주식당일분봉조회[v1_국내주식-022]
    // await getApiData(
    //     `${process.env.NEXT_PUBLIC_API_BASE}/api/stock/${code}/search/time`,
    //     {hour: hhmmss()},
    //     timeCandles
    // )

    await getHourlyData(`${process.env.NEXT_PUBLIC_API_BASE}/api/stock/${code}/search/time`,
        {hour:hhmmss()},
        timeCandles
        )

    // 주식현재가 투자자[v1_국내주식-012]
    await axios.get(`${process.env.NEXT_PUBLIC_API_BASE}/api/stock/${code}/investor`)
        .then(res=> console.log(res.data))



    return (
        <div className="p-4">
            <h1 className="text-xl font-bold mb-2">{code} 차트</h1>
            <StockChart candles={dailyCandles} />
            <StockChart candles={timeCandles}/>
        </div>
    );
}
