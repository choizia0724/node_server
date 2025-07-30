import getStockData from "./src/services/getStockData";
import connectDB from "./src/services/connectDB";

connectDB()
  .then(() => {
    getStockData(); // 데이터 가져오기 함수 호출
    console.log("Database connected successfully");
  })
  .catch((err) => {
    console.error("Database connection failed:", err);
    process.exit(1);
  });
