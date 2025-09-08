// src/rest/router.js
import express from "express";
import axios from "axios";

export const mountRest = (app, auth) => {
  const base = process.env.KIS_BASE_URL;
  if (!base) throw new Error("Missing env: KIS_BASE_URL");

  const TRID_DAILY    = "FHKST03010100"; // v1_국내주식-016
  const TRID_INTRADAY = "FHKST03010200"; // v1_국내주식-022
  const TRID_INVESTOR = "FHKST01010900"; // v1_국내주식-012

  const r = express.Router();

  // --- 유틸들 ---

  const mergeQuery = (req, defaults) => {
    // 기본값 -> 클라이언트 쿼리가 있으면 덮어쓰기
    req.query = { ...defaults, ...req.query };
  };

  // 기존 proxyGet 교체: trId를 선택적으로 받도록
  const proxyGet = async (req, res, kisPath, trId = TRID_DAILY) => {
    const url = `${base}${kisPath}`;
    const doReq = async (token) =>
        axios.get(url, {
          headers: {
            "content-type": "application/json",
            authorization: `bearer ${token}`,
            appkey: process.env.KIS_APP_KEY,
            appsecret: process.env.KIS_SECRET_KEY,
            tr_id: trId,     // ★ 라우트별 tr_id 주입
            custtype: "P",
          },
          params: req.query,
          timeout: 10000,
        });

    try {
      const token = await auth.getAccessToken();
      const rsp = await doReq(token);
      return res.status(rsp.status).send(rsp.data);
    } catch (e) {
      const status = e?.response?.status;
      console.error("KIS proxy error:", status, e?.response?.data);
      if (status === 401) {
        try {
          const token = await auth.forceRefreshAccess();
          const retry = await doReq(token);
          return res.status(retry.status).send(retry.data);
        } catch (e2) {
          console.error("KIS retry error:", e2?.response?.status, e2?.response?.data);
        }
      }
      return res.status(status || 500).send(e?.response?.data || { message: "KIS error" });
    }
  };


  // --- 라우트들 ---

  // 국내주식기간별시세(일/주/월/년)[v1_국내주식-016]
  r.post("/stock/search/:code", (req, res) => {
    mergeQuery(req, {
      FID_COND_MRKT_DIV_CODE: "J",
      FID_INPUT_ISCD: req.params.code,
      FID_INPUT_HOUR_1: req.body.hour,     // 일반적으론 일봉에 불필요, 그래도 패스
      FID_INPUT_DATE_1: req.body.date1,    // YYYYMMDD
      FID_INPUT_DATE_2: req.body.date2,    // YYYYMMDD
      FID_PERIOD_DIV_CODE: req.body.divCode || "D", // D/W/M/Y
      FID_ORG_ADJ_PRC: req.body.adj ?? "0",        // 0:수정주가 1:원주가
    });
    return proxyGet(
        req,
        res,
        "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice",
        TRID_DAILY
    );
  });

  // 주식당일분봉조회[v1_국내주식-022]
  r.post("/stock/search/time/:code", (req, res) => {
    mergeQuery(req, {
      FID_COND_MRKT_DIV_CODE: "J",
      FID_INPUT_ISCD: req.params.code,
      FID_INPUT_HOUR_1: req.body.hour,
      FID_PW_DATA_INCU_YN: "Y",
      FID_ETC_CLS_CODE:"J"
    });
    return proxyGet(
        req,
        res,
        "/uapi/domestic-stock/v1/quotations/inquire-time-itemchartprice",
        TRID_INTRADAY
    );
  });

  // 주식현재가 투자자[v1_국내주식-012]
  r.get("/stock/investor/:code", (req, res) => {
    mergeQuery(req, {
      fid_cond_mrkt_div_code: "J", // 주식
      fid_input_iscd: req.params.code,
    });
    return proxyGet(
        req,
        res,
        "/uapi/domestic-stock/v1/quotations/inquire-price",
        TRID_INVESTOR
    );
  });


  // 헬스체크(테스트)
  r.get("/__ping", (_req, res) => res.send("rest-ok"));

  app.use("/proxy", r);
};
