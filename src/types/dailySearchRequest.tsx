// types/dailySearchRequest.ts
export interface dailySearchRequest {
    hour: string;     // 조회 시간 (예: "130000")
    date1: string;    // 조회 시작일 (예: "20240101")
    date2: string;    // 조회 종료일 (예: "20240131")
    divCode: "D" | "W" | "M" | "Y"; // 기간 구분 (일봉/주봉/월봉/년봉)
    adj: "0" | "1";   // 수정주가 여부 (0: 수정주가, 1: 원주가)
}
