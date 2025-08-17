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

/* GET stock by symbol */
router.get("/:symbol", async function (req, res, next) {
  const symbol = req.params.symbol;
  models.Stock.findOne({ where: { symbol } })
    .then((stock) => {
      if (stock) {
        res.status(200).json(stock);
      } else {
        res.status(404).send("Stock not found.");
      }
    })
    .catch((error) => {
      console.error("Error fetching stock data:", error);
      res.status(500).send("Error fetching stock data.");
    });
});

/* Get stock by param */
router.post("/search", async function (req, res, next) {
  const { symbol, name, mrktctg } = req.body;
  const whereClause = {};

  if (symbol) {
    whereClause.symbol = symbol;
  }
  if (name) {
    whereClause.name = { [models.Sequelize.Op.like]: `%${name}%` };
  }
  if (mrktctg) {
    whereClause.mrktctg = mrktctg;
  }

  // If no search parameters are provided, return all stocks
  if (Object.keys(whereClause).length === 0) {
    return res.status(400).send("No search parameters provided.");
  }

  models.Stock.findAll({ where: whereClause })
    .then((stocks) => {
      res.status(200).json(stocks);
    })
    .catch((error) => {
      console.error("Error searching stock data:", error);
      res.status(500).send("Error searching stock data.");
    });
});

/* Fetch and update stock data */
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
