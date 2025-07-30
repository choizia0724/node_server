// src/models/Stock.js
import mongoose from "mongoose";

const stockSchema = new mongoose.Schema(
  {
    // 기본 식별자 필드
    symbol: { type: String, unique: true, required: true }, // srtnCd (단축코드)
    name: { type: String, required: true }, // itmsNm (종목 한글명)

    // 추가된 MST 파일 정보 (선택 사항 - 필요에 따라 추가/삭제)
    basDt: { type: String }, // 기준일자 (예: "20250729")
    isinCd: { type: String, unique: true }, // ISIN 코드 (고유한 값일 수 있으므로 unique)
    mrktCtg: { type: String }, // 시장 구분 (KOSPI, KOSDAQ)
    crno: { type: String }, // 법인등록번호
    corpNm: { type: String }, // 법인명

    // 실시간 주가 등 변동성 데이터
    price: { type: Number, default: 0 }, // 현재 주가 (실시간 API로 업데이트될 필드)
    lastUpdated: { type: Date, default: Date.now }, // 마지막 업데이트 일자
  },
  { timestamps: true }
); // createdAt, updatedAt 자동 추가

export default mongoose.model("Stock", stockSchema);
