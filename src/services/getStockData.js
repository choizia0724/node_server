import axios from "axios";
import dotenv from "dotenv";
dotenv.config(); // .env 파일의 환경변수를 process.env에 등록

// Get last week's Monday and format as 'YYYYMMDD'
function getLastWeekMonday() {
  const today = new Date();
  // Get current day of week (0=Sunday, 1=Monday, ..., 6=Saturday)
  const day = today.getDay();
  // Calculate days since last Monday (if today is Monday, go back 7 days)
  const diff = day === 1 ? 7 : ((day + 6) % 7) + 1;
  const lastMonday = new Date(today);
  lastMonday.setDate(today.getDate() - diff);

  // Format as 'YYYYMMDD'
  const yyyy = lastMonday.getFullYear();
  const mm = String(lastMonday.getMonth() + 1).padStart(2, "0");
  const dd = String(lastMonday.getDate()).padStart(2, "0");
  return `${yyyy}${mm}${dd}`;
}

const getStockData = async () => {
  const apiUrl =
    "https://apis.data.go.kr/1160100/service/GetKrxListedInfoService/getItemInfo?";
  const apiKey = process.env.API_KEY; // .env 파일에서 API 키를 가져옵니다.
  const beginBasDt = getLastWeekMonday(); // 시작 날짜 (YYYYMMDD 형식
  const resultType = "json"; // 결과 형식 (json 또는 xml)
  const numOfRows = 3000; // 요청할 데이터의 행 수
  // API 요청 URL 생성

  const url = `${apiUrl}serviceKey=${apiKey}&beginBasDt=${beginBasDt}&resultType=${resultType}&numOfRows=${numOfRows}`;

  axios
    .get(url)
    .then(async (response) => {
      if (response.status === 200) {
        const data = response.data.response.body.items.item;

        const Stock = (await import("../models/stock.js")).default;
        // Stock 모델 가져오기
        for (const item of data) {
          // 데이터베이스에 저장할 객체 생성
          const stockData = {
            symbol: item.srtnCd, // srtnCd (단축코드)
            name: item.itmsNm, // itmsNm (종목 한글명)
            basDt: item.basDt, // 기준일자
            isinCd: item.isinCd, // ISIN 코드
            mrktCtg: item.mrktCtg, // 시장 구분
            crno: item.crno, // 법인등록번호
            corpNm: item.corpNm, // 법인명
          };

          // 존재하면 업데이트, 없으면 생성 (upsert)
          await Stock.findOneAndUpdate(
            { symbol: stockData.symbol, basDt: stockData.basDt },
            // 기준 필드
            stockData,
            { upsert: true, new: true }
          );
        }
        console.log("Stock data successfully saved to the database.");
      } else {
        console.error("Error fetching stock data:", response.statusText);
      }
    })
    .catch((error) => {
      console.error("Error during API request:", error);
    });
};

export default getStockData;
