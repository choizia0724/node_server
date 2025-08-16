import { Router } from "express";
import getStockData from "../src/services/getStockData.js";
import models from "../models/index.js";

var router = Router();

/* GET stock listing. */
router.get("/", async function (req, res, next) {
  models.Stock.findAll()
    .then((stocks) => {
      res.status(200).json(stocks);
    })
    .catch((error) => {
      console.error("Error fetching stock data:", error);
      res.status(500).send("Error fetching stock data.");
    });
});

router.get("/getStockData", async function (req, res, next) {
  try {
    await getStockData();
    res.status(200).send("Stock data fetched successfully.");
  } catch (error) {
    console.error("Error fetching stock data:", error);
    res.status(500).send("Error fetching stock data.");
  }
});

export default router;
