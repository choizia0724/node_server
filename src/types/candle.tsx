import { type UTCTimestamp} from "lightweight-charts"

export interface CandleDTO{
    time: object;
    open: number;
    high: number;
    low: number;
    close: number;
    volume?: number;
}