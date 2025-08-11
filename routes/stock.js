import { Router } from "express";
import connectDB from "../src/services/connectDB.js";
import getStockData from "../src/services/getStockData.js";

var router = Router();

/* GET stock listing. */
router.get("/", async function (req, res, next) {
  const connection = await connectDB();

  if (!connection) {
    return res.status(500).send("Database connection failed.");
  }
  try {
    connection.execute("SELECT * FROM stocks", (err, results) => {
      if (err) {
        console.error("Error fetching stock data:", err);
        return res.status(500).send("Error fetching stock data.");
      }
      res.json(results);
    });
  } catch (error) {
    console.error("Unexpected error:", error);
    res.status(500).send("Unexpected error occurred.");
  } finally {
    await connection.close();
  }
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
