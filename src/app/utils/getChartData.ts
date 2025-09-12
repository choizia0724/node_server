import {CandleDTO} from "@/types/candle";
import axios from "axios";
import { type UTCTimestamp} from "lightweight-charts"

const toUtcSec = (ymd: string, hms: string): UTCTimestamp => {
    const y = +ymd.slice(0,4);
    const m = +ymd.slice(4,6);
    const d = +ymd.slice(6,8);
    const hh = +hms.slice(0,2);
    const mm = +hms.slice(2,4);
    const ss = +hms.slice(4,6);

    return Math.floor(Date.UTC(y, m - 1, d, hh, mm, ss) / 1000) as UTCTimestamp;
};


const getDailyData = async (url:string, body:any, array:CandleDTO[]) => {
    console.log(url)
    await axios.post(
        url,
        body,
        {
            headers: {
                "Cache-Control": "no-store",
                "content-type": "application/json"
            }
        }
    ).then((res:any)=>{
        console.log(res)
        const sorted = res.data.output2.sort((a, b) => {
            return a.stck_bsop_date - b.stck_bsop_date;
        });
        sorted.map((d: any) => (
            array.push({
                time: toUtcSec(d.stck_bsop_date, d.stck_cntg_hour||"000000"),
                open: Number(d.stck_oprc),
                high: Number(d.stck_hgpr),
                low: Number(d.stck_lwpr),
                close: Number(d.stck_clpr),
                volume: Number(d.acml_vol),
            })
        ));
    });
}

const getHourlyData = async (url:string, body:any, array:CandleDTO[]) => {
    await axios.post(
        url,
        body,
        {
            headers: {
                "Cache-Control": "no-store",
                "content-type": "application/json"
            }
        }
    ).then((res:any)=>{
        console.log(res)
        const sorted = res.data.output2.sort((a, b) => {
            return a.stck_cntg_hour - b.stck_cntg_hour;
        });
        sorted.map((d: any) => (
            array.push({
                time: toUtcSec(d.stck_bsop_date, d.stck_cntg_hour||"000000"),
                open: Number(d.stck_oprc),
                high: Number(d.stck_hgpr),
                low: Number(d.stck_lwpr),
                close: Number(d.stck_prpr),
                volume: Number(d.cntg_vol),
            })
        ));
    });
}

export {getDailyData, getHourlyData}