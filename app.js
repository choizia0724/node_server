import express from "express";
import createError from "http-errors";
import path from "path";
import cookieParser from "cookie-parser";
import logger from "morgan";
import cron from "node-cron";

import dotenv from "dotenv";
dotenv.config();

import models from "./models/index.js"; // Import models to sync with the database

import indexRouter from "./routes/index.js"; // .js 확장자 필수
import usersRouter from "./routes/users.js"; // .js 확장자 필수
import stockRouter from "./routes/stock.js"; // .js 확장자 필수

import { fileURLToPath } from "url";

import getStockData from "./src/services/getStockData.js";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();

// view engine setup
app.set("views", path.join(__dirname, "views"));
app.set("view engine", "jade");

app.use(logger("dev"));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, "public")));

//controller
app.use("/", indexRouter);
app.use("/users", usersRouter);
app.use("/stock", stockRouter);

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
