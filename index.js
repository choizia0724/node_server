
import connectDB from "./src/services/connectDB.js"; // MongoDB 연결 함수


// MongoDB 연결 및 서버 시작
connectDB()
  .then(async () => {
    console.log("Database connected successfully. Starting Express server...");

    // 서버 시작 전에 getStockData를 실행하고 싶다면 여기에!
    // await getStockData();
    // console.log("Initial stock data processing completed.");

    app.listen(PORT, () => {
      console.log(`Express server listening on port ${PORT}`);
      console.log(
        `Access the app at http://localhost:${PORT} (if port forwarding is set up)`
      );
    });
  })
  .catch((dbError) => {
    console.error("Database initial connection failed:", dbError);
    // 데이터베이스 연결 실패 시에도 Express 서버는 시작할 수 있도록
    // (하지만 DB 기능은 작동하지 않을 것임)
    app.listen(PORT, () => {
      console.warn(
        `Express server listening on port ${PORT}, but database connection failed.`
      );
    });
  });
