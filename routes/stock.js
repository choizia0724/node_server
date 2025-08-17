import { Router } from "express";
import getStockData from "../src/services/getStockData.js";
import models from "../models/index.js";

var router = Router();

/* GET stock listing. */
router.get("/", async function (req, res, next) {
  const page = parseInt(req.query.page) || 1;
  const limit = parseInt(req.query.limit) || 20;
  const offset = (page - 1) * limit;

  try {
    const { count, rows } = await models.Stock.findAndCountAll({
      offset,
      limit,
      order: [["symbol", "asc"]],
    });

    res.status(200).json({
      data: rows,
      pagination: {
        totalItems: count,
        currentPage: page,
        totalPages: Math.ceil(count / limit),
        limit,
      },
    });
  } catch (error) {
    console.error("Error fetching paginated stock data:", error);
    res.status(500).send("Error fetching stock data.");
  }
});

/* Get stock by param */
router.post("/search", async function (req, res, next) {
  const { symbol, name, mrktctg, page = 1, limit = 20 } = req.body;

  const whereClause = {};
  const offset = (page - 1) * limit;

  if (symbol) {
    whereClause.symbol = symbol;
  }
  if (name) {
    whereClause.name = { [models.Sequelize.Op.like]: `%${name}%` };
  }
  if (mrktctg) {
    whereClause.mrktctg = mrktctg;
  }
  try {
    const { count, rows } = await models.Stock.findAndCountAll({
      where: whereClause,
      offset,
      limit,
      order: [["basdt", "DESC"]],
    });

    res.status(200).json({
      data: rows,
      pagination: {
        totalItems: count,
        currentPage: page,
        totalPages: Math.ceil(count / limit),
        limit,
      },
    });
  } catch (error) {
    console.error("Error searching stock data:", error);
    res.status(500).send("Error searching stock data.");
  }
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
