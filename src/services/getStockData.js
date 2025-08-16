import axios from "axios";
import models from "../../models/index.js";

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

        for (const item of data) {
          const stockData = {
            symbol: item.srtnCd,
            name: item.itmsNm,
            basdt: item.basDt,
            isincd: item.isinCd,
            mrktctg: item.mrktCtg,
            crno: item.crno,
            corpnm: item.corpNm,
          };
          models.Stock.upsert(stockData)
            .then(() => {
              console.log(
                `Stock data for ${stockData.symbol} updated successfully.`
              );
            })
            .catch((error) => {
              console.error(
                `Error updating stock data for ${stockData.symbol}:`,
                error
              );
            });
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
