// src/rest/router.js
import express from "express";
import axios from "axios";

export const mountRest = (app, auth) => {
  const base = process.env.KIS_BASE_URL;
  if (!base) throw new Error("Missing env: KIS_BASE_URL");

  const r = express.Router();

  // --- 유틸들 ---
  const normalizeCode6 = (raw) => (raw || "").replace(/\D/g, "").slice(-6); // "A006390" -> "006390"
  const mergeQuery = (req, defaults) => {
    // 기본값 -> 클라이언트 쿼리가 있으면 덮어쓰기
    req.query = { ...defaults, ...req.query };
  };

  const proxyGet = async (req, res, kisPath) => {
    const url = `${base}${kisPath}`;
    const doReq = async (token) =>
        axios.get(url, {
          headers: {
            'content-type':'application/json',
            authorization: `bearer ${token}`,
            appkey: process.env.KIS_APP_KEY,
            appsecret: process.env.KIS_SECRET_KEY,
            tr_id:'FHKST03010200',
            custtype:'P'
          },
          params: req.query,
          timeout: 10000,
        });

    try {
      const token = await auth.getAccessToken();
      console.log(token)
      const rsp = await doReq(token);
      return res.status(rsp.status).send(rsp.data);
    } catch (e) {
      console.error("KIS proxy error:", e?.response?.status, e?.response?.data);
      const status = e?.response?.status;
      if (status === 401) {
        try {
          const token = await auth.forceRefreshAccess();
          const retry = await doReq(token);
          return res.status(retry.status).send(retry.data);
        } catch (e2) {
          console.error("KIS retry error:", e2?.response?.status, e2?.response?.data);
        }
      }
      return res
          .status(status || 500)
          .send(e?.response?.data || { message: "KIS error" });
    }
  };

  // --- 라우트들 ---

  // 종목 리스트 (네가 실제 쓰는 "종목 목록" API로 바꿔도 됨)
  r.get("/stocks", (req, res) => {
    // 문서에 따라 필요 파라미터가 다를 수 있어 패스스루 유지
    return proxyGet(req, res, "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice");
  });

  // 현재가
  r.get("/stock/:code/quote", (req, res) => {
    const code6 = normalizeCode6(req.params.code);
    mergeQuery(req, {
      fid_cond_mrkt_div_code: "J", // 주식
      fid_input_iscd: code6,
    });
    return proxyGet(req, res, "/uapi/domestic-stock/v1/quotations/inquire-price");
  });

  // 당일 분봉 (intraday)
  r.get("/stock/:code/intraday", (req, res) => {
    const code6 = normalizeCode6(req.params.code);
    mergeQuery(req, {
      FID_COND_MRKT_DIV_CODE: "J", // 주식
      FID_INPUT_ISCD: code6,
      FID_INPUT_HOUR_1: "090000",  // 장 시작부터
      FID_PW_DATA_INCU_YN: "N",    // 수정주가 포함 여부(버전에 따라 옵션)
      //fid_org_adj_prc: "0",        // 수정주가 반영코드(문서에 따라 사용)
      FID_ETC_CLS_CODE:"J"
    });
    return proxyGet(
        req,
        res,
        "/uapi/domestic-stock/v1/quotations/inquire-time-itemchartprice"
    );
  });

  // 일봉
  r.get("/stock/:code/daily", (req, res) => {
    const code6 = normalizeCode6(req.params.code);
    mergeQuery(req, {
      fid_cond_mrkt_div_code: "J",
      fid_input_iscd: code6,
      fid_pw_data_incu_yn: "N",
      fid_org_adj_prc: "0",
      // 기간/범위 파라미터가 있는 문서 버전이면 필요 시 여기에 추가 (예: from/to)
    });
    return proxyGet(
        req,
        res,
        "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice"
    );
  });

  // 주봉
  r.get("/stock/:code/weekly", (req, res) => {
    const code6 = normalizeCode6(req.params.code);
    mergeQuery(req, {
      fid_cond_mrkt_div_code: "J",
      fid_input_iscd: code6,
      fid_pw_data_incu_yn: "N",
      fid_org_adj_prc: "0",
    });
    return proxyGet(
        req,
        res,
        "/uapi/domestic-stock/v1/quotations/inquire-weekly-itemchartprice"
    );
  });

  // 월봉
  r.get("/stock/:code/monthly", (req, res) => {
    const code6 = normalizeCode6(req.params.code);
    mergeQuery(req, {
      fid_cond_mrkt_div_code: "J",
      fid_input_iscd: code6,
      fid_pw_data_incu_yn: "N",
      fid_org_adj_prc: "0",
    });
    return proxyGet(
        req,
        res,
        "/uapi/domestic-stock/v1/quotations/inquire-monthly-itemchartprice"
    );
  });

  // 체결 내역(틱)
  r.get("/stock/:code/trades", (req, res) => {
    const code6 = normalizeCode6(req.params.code);
    mergeQuery(req, {
      fid_cond_mrkt_div_code: "J",
      fid_input_iscd: code6,
      // 필요 시 건수/시작시각 등의 파라미터를 문서에 맞춰 추가
      // ex) fid_input_hour_1: "090000"
    });
    return proxyGet(
        req,
        res,
        "/uapi/domestic-stock/v1/quotations/inquire-transaction"
    );
  });

  // 종목별 투자자 동향
  r.get("/stock/:code/investor", (req, res) => {
    const code6 = normalizeCode6(req.params.code);
    mergeQuery(req, {
      fid_cond_mrkt_div_code: "J",
      fid_input_iscd: code6,
      // 기간/집계 단위 등은 문서에 따라 추가
    });
    return proxyGet(
        req,
        res,
        "/uapi/domestic-stock/v1/quotations/inquire-investor"
    );
  });

  // 종목별 프로그램매매 동향
  r.get("/stock/:code/program-trade", (req, res) => {
    const code6 = normalizeCode6(req.params.code);
    mergeQuery(req, {
      fid_cond_mrkt_div_code: "J",
      fid_input_iscd: code6,
    });
    return proxyGet(
        req,
        res,
        "/uapi/domestic-stock/v1/quotations/inquire-program-trade"
    );
  });

  // 시장지수(코스피/코스닥 등) 일봉
  r.get("/market/index/:marketId", (req, res) => {
    // 지수 코드는 문서 규격을 따르되, 여기서는 그대로 전달
    mergeQuery(req, {
      fid_input_iscd: req.params.marketId,
      // 필요 시 지수용 분류 코드/수정주가 옵션 추가
      fid_pw_data_incu_yn: "N",
      fid_org_adj_prc: "0",
    });
    return proxyGet(
        req,
        res,
        "/uapi/domestic-stock/v1/quotations/inquire-daily-indexchartprice"
    );
  });

  // 시장별 투자자 동향
  r.get("/market/:marketId/investor", (req, res) => {
    mergeQuery(req, {
      fid_input_iscd: req.params.marketId,
      // 집계 단위/기간 등은 문서에 맞춰 추가 가능
    });
    return proxyGet(
        req,
        res,
        "/uapi/domestic-stock/v1/quotations/inquire-investor-index"
    );
  });

  // 헬스체크(테스트)
  r.get("/__ping", (_req, res) => res.send("rest-ok"));

  app.use("/api", r);
};
