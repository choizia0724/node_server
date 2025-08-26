// src/auth/kisAuth.js
import axios from "axios";

export const createKisAuth = () => {
  const base = process.env.KIS_BASE_URL;
  const appkey = process.env.KIS_APP_KEY;
  const secret = process.env.KIS_SECRET_KEY;

  if (!base || !appkey || !secret) {
    throw new Error("Missing env: KIS_BASE_URL / KIS_APP_KEY / KIS_SECRET_KEY");
  }

  const SAFETY_MS = 60_000;

  let accessToken = null;
  let accessExpAt = 0;
  let approvalKey = null;
  let approvalExpAt = 0;

  const now = () => Date.now();
  const alive = (expAt) => now() + SAFETY_MS < expAt;

  const issueAccessToken = async () => {
    const url = `${base}/oauth2/tokenP`;
    const { data } = await axios.post(url, {
      grant_type: "client_credentials",
      appkey,
      appsecret: secret,
    });
    accessToken = data.access_token;
    accessExpAt = now() + (data.expires_in ?? 24 * 3600) * 1000;
    return accessToken;
  };

  const issueApprovalKey = async () => {
    const url = `${base}/oauth2/Approval`;
    const { data } = await axios.post(url, {
      grant_type: "client_credentials",
      appkey,
      secretkey: secret,
    });
    approvalKey = data.approval_key;
    approvalExpAt = now() + (data.expires_in ?? 24 * 3600) * 1000;
    return approvalKey;
  };

  const getAccessToken = async () =>
    accessToken && alive(accessExpAt) ? accessToken : issueAccessToken();

  const getApprovalKey = async () =>
    approvalKey && alive(approvalExpAt) ? approvalKey : issueApprovalKey();

  const forceRefreshAccess = async () => issueAccessToken();
  const forceRefreshApproval = async () => issueApprovalKey();

  return {
    getAccessToken,
    getApprovalKey,
    forceRefreshAccess,
    forceRefreshApproval,
  };
};
