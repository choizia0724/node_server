import express from "express";
import createError from "http-errors";
import path from "path";
import cookieParser from "cookie-parser";
import logger from "morgan";
import cron from "node-cron";
import dotenv from "dotenv";
import cors from "cors";

dotenv.config();

import models from "./models/index.js"; // Import models to sync with the database

import indexRouter from "./routes/index.js"; // .js 확장자 필수
import usersRouter from "./routes/users.js"; // .js 확장자 필수
import stockRouter from "./src/rest/routes/stock.js"; // .js 확장자 필수

import { fileURLToPath } from "url";

import getStockData from "./src/services/getStockData.js";


import { mountRest } from "./src/rest/router.js";
import kisAuth from "./src/auth/kisAuth.js";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const frontendUrl = process.env.FRONTEND_URL || "http://localhost:3000"; // 기본값 설정

const app = express();

// view engine setup
app.set("views", path.join(__dirname, "views"));
app.set("view engine", "jade");

app.use(logger("dev"));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(
  cors({
    origin: [
      /localhost:\d+$/, // 개발용
      new RegExp(frontendUrl.replace(/\./g, "\\.")), // 환경변수 기반 프론트엔드 도메인
    ],
  })
);

app.use(cookieParser());
app.use(express.static(path.join(__dirname, "public")));

//controller
app.use("/", indexRouter);
app.use("/users", usersRouter);
app.use("/stock", stockRouter);


// REST (선택)
mountRest(app, kisAuth);

// catch 404 and forward to error handler
app.use(function (req, res, next) {
  next(createError(404));
});

// error handler
app.use(function (err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get("env") === "development" ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render("error");
});

models.sequelize
  .sync()
  .then(() => {
    console.log("Stock table synced successfully.");
  })
  .catch((error) => {
    console.error("Error syncing Stock table:", error);
  });
// Cron job to fetch stock data everyday at 00:00
cron.schedule("0 0 * * *", async () => {
  try {
    await getStockData();
  } catch (error) {
    console.error("Error fetching stock data:", error);
  }
});

export default app;
