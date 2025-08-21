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

  // 아직 쓸 route가 없음....
  r.get("/stocks", (req, res) =>
    proxyGet(req, res, "/uapi/domestic/stock/list")
  );
  r.get("/stock/:code", (req, res) =>
    proxyGet(req, res, `/uapi/domestic/stock/${req.params.code}`)
  );

  app.use("/api", r);
};
