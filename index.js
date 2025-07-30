// /app/index.js (또는 app.js)

import express, { json } from "express";
import connectDB from "./src/services/connectDB"; // MongoDB 연결 함수

const app = express();
const PORT = process.env.PORT || 3000; // 환경 변수 PORT가 없으면 3000 사용

// --- Express 미들웨어 설정 (필요한 경우) ---
app.use(json()); // JSON 요청 본문 파싱
// 다른 미들웨어들도 여기에 추가할 수 있습니다.
// ------------------------------------------

// --- 예시 라우트 ---
app.get("/", (req, res) => {
  res.send("Node.js app is running and connected to MongoDB (hopefully)!");
});

app.get("/status", (req, res) => {
  // 간단한 상태 체크 라우트
  const dbStatus =
    mongoose.connection.readyState === 1 ? "Connected" : "Disconnected";
  res.status(200).json({
    app: "Running",
    database: dbStatus,
    timestamp: new Date().toISOString(),
  });
});
// --------------------

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

// --- 추가: Node.js 앱 종료 시그널 처리 ---
// Kubernetes가 Pod를 종료할 때, SIGTERM 시그널을 보냅니다.
// 이때 깔끔하게 앱을 종료하고 DB 연결을 끊도록 합니다.
process.on("SIGTERM", () => {
  console.log("SIGTERM signal received. Closing HTTP server.");
  app.close(() => {
    console.log("HTTP server closed.");
    mongoose.connection.close(false, () => {
      // false: process.exit() 호출 안 함
      console.log("MongoDB connection closed.");
      process.exit(0); // 정상 종료
    });
  });
});

process.on("SIGINT", () => {
  // Ctrl+C (개발 환경에서 유용)
  console.log("SIGINT signal received. Closing HTTP server.");
  app.close(() => {
    console.log("HTTP server closed.");
    mongoose.connection.close(false, () => {
      console.log("MongoDB connection closed.");
      process.exit(0);
    });
  });
});
