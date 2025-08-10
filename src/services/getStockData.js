import axios from "axios";
import { connectDB } from "./connectDB";

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
        const connection = await connectDB();

        const mergeSql = `
MERGE INTO stock_table target
USING (SELECT :symbol AS symbol, :name AS name, TO_DATE(:basDt, 'YYYYMMDD') AS basDt,
              :isinCd AS isinCd, :mrktCtg AS mrktCtg, :crno AS crno, :corpNm AS corpNm
       FROM dual) source
ON (target.symbol = source.symbol AND target.basDt = source.basDt)
WHEN MATCHED THEN
  UPDATE SET 
    name = source.name,
    isinCd = source.isinCd,
    mrktCtg = source.mrktCtg,
    crno = source.crno,
    corpNm = source.corpNm
WHEN NOT MATCHED THEN
  INSERT (symbol, name, basDt, isinCd, mrktCtg, crno, corpNm)
  VALUES (source.symbol, source.name, source.basDt, source.isinCd, source.mrktCtg, source.crno, source.corpNm)
`;

        for (const item of data) {
          const stockData = {
            symbol: item.srtnCd,
            name: item.itmsNm,
            basDt: item.basDt,
            isinCd: item.isinCd,
            mrktCtg: item.mrktCtg,
            crno: item.crno,
            corpNm: item.corpNm,
          };

          await connection.execute(mergeSql, {
            symbol: stockData.symbol,
            name: stockData.name,
            basDt: stockData.basDt,
            isinCd: stockData.isinCd,
            mrktCtg: stockData.mrktCtg,
            crno: stockData.crno,
            corpNm: stockData.corpNm,
          });
        }

        await connection.commit();

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
