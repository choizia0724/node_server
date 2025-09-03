// app/stocks/[code]/page.tsx
import StockChart from "@/app/components/StockChart";
import axios from "axios";
import {CandleDTO} from "@/types/candle";
import {StockSearchRequest} from "@/types/StockSearchRequest";

function toUtcTs(time: string | number) {
    return Math.floor(new Date(time).getTime() / 1000) as any;
}

export default async function Page({params}: { params:Promise<{ code: string }> }) {
    const {code} = await params

    // 보낼 body
    const body: StockSearchRequest = {
        hour: "130000",      // 오후 1시
        date1: "20240901",   // 시작일
        date2: "20250931",   // 종료일
        divCode: "D",        // 일봉
        adj: "1",            // 수정주가
    };
    const candles:CandleDTO[]=[];

    // 초기 데이터 가져오기
    //const {data} =
        await axios.post(
        `${process.env.NEXT_PUBLIC_API_BASE}/api/stock/${code}/search`,
        body,
        {
            headers: {
                "Cache-Control": "no-store",
                "content-type": "application/json"
            }
        }
    ).then((res:any)=>{

        const sorted = res.data.output2.sort((a, b) => {
            return a.stck_bsop_date - b.stck_bsop_date;
        });
            sorted.map((d: any) => (
                    candles.push({
                        time: {
                            year: Number(d.stck_bsop_date.slice(0,4)),
                            month: Number(d.stck_bsop_date.slice(4,6)),
                            day: Number(d.stck_bsop_date.slice(6,8)),
                        },
                    open: Number(d.stck_oprc),
                    high: Number(d.stck_hgpr),
                    low: Number(d.stck_lwpr),
                    close: Number(d.stck_clpr),
                    volume: Number(d.acml_vol),
                })
            ));

        });



    return (
        <div className="p-4">
            <h1 className="text-xl font-bold mb-2">{code} 차트</h1>
            <StockChart candles={candles} />
        </div>
    );
}
