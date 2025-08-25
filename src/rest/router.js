// src/rest/router.js
import express from "express";
import axios from "axios";

export const mountRest = (app, auth) => {
  const base = process.env.KIS_BASE_URL;
  if (!base) throw new Error("Missing env: KIS_BASE_URL");

  const r = express.Router();

  const proxyGet = async (req, res, kisPath) => {
    const url = `${base}${kisPath}`;

    const doReq = async (token) =>
      axios.get(url, {
        headers: {
          Authorization: `Bearer ${token}`,
          appkey: process.env.KIS_APP_KEY,
          secretkey: process.env.KIS_SECRET_KEY,
        },
        params: req.query,
      });

    try {
      const token = await auth.getAccessToken();
      const rsp = await doReq(token);
      res.status(rsp.status).send(rsp.data);
    } catch (e) {
      const status = e?.response?.status;
      if (status === 401) {
        try {
          const token = await auth.forceRefreshAccess();
          const retry = await doReq(token);
          return res.status(retry.status).send(retry.data);
        } catch {}
      }
      res
        .status(status || 500)
        .send(e?.response?.data || { message: "KIS error" });
    }
  };

  // 종목 리스트 (국내 상장종목)
  r.get("/stocks", (req, res) =>
      proxyGet(req, res, "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice") // or 종목 리스트 API
  );

// 특정 종목 현재가
  r.get("/stock/:code/quote", (req, res) =>
      proxyGet(req, res, `/uapi/domestic-stock/v1/quotations/inquire-price?fid_input_iscd=${req.params.code}`)
  );

// 특정 종목 당일분봉 (intraday 차트)
  r.get("/stock/:code/intraday", (req, res) =>
      proxyGet(req, res, `/uapi/domestic-stock/v1/quotations/inquire-time-itemchartprice?fid_input_iscd=${req.params.code}`)
  );

// 특정 종목 일봉 (일자별 시세)
  r.get("/stock/:code/daily", (req, res) =>
      proxyGet(req, res, `/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice?fid_input_iscd=${req.params.code}`)
  );

// 특정 종목 주봉
  r.get("/stock/:code/weekly", (req, res) =>
      proxyGet(req, res, `/uapi/domestic-stock/v1/quotations/inquire-weekly-itemchartprice?fid_input_iscd=${req.params.code}`)
  );

// 특정 종목 월봉
  r.get("/stock/:code/monthly", (req, res) =>
      proxyGet(req, res, `/uapi/domestic-stock/v1/quotations/inquire-monthly-itemchartprice?fid_input_iscd=${req.params.code}`)
  );

// 체결 내역 (틱데이터)
  r.get("/stock/:code/trades", (req, res) =>
      proxyGet(req, res, `/uapi/domestic-stock/v1/quotations/inquire-transaction?fid_input_iscd=${req.params.code}`)
  );

// 투자자별 매매동향 (개인/외국인/기관)
  r.get("/stock/:code/investor", (req, res) =>
      proxyGet(req, res, `/uapi/domestic-stock/v1/quotations/inquire-investor?fid_input_iscd=${req.params.code}`)
  );

// 종목별 프로그램매매 동향
  r.get("/stock/:code/program-trade", (req, res) =>
      proxyGet(req, res, `/uapi/domestic-stock/v1/quotations/inquire-program-trade?fid_input_iscd=${req.params.code}`)
  );

// 시장지수 (코스피/코스닥 등)
  r.get("/market/index/:marketId", (req, res) =>
      proxyGet(req, res, `/uapi/domestic-stock/v1/quotations/inquire-daily-indexchartprice?fid_input_iscd=${req.params.marketId}`)
  );

// 시장별 투자자 동향
  r.get("/market/:marketId/investor", (req, res) =>
      proxyGet(req, res, `/uapi/domestic-stock/v1/quotations/inquire-investor-index?fid_input_iscd=${req.params.marketId}`)
  );


  app.use("/api", r);
};
