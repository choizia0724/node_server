// src/auth/kisAuth.js
import axios from "axios";

const KIS_BASE_URL = process.env.KIS_BASE_URL;   // e.g. https://openapivts.koreainvestment.com:29443
const APP_KEY = process.env.KIS_APP_KEY;
const SECRET_KEY = process.env.KIS_SECRET_KEY;

if (!KIS_BASE_URL || !APP_KEY || !SECRET_KEY) {
  throw new Error("Missing env: KIS_BASE_URL / KIS_APP_KEY / KIS_SECRET_KEY");
}

// 모의투자: /oauth2/tokenP, /oauth2/Approval
const TOKEN_PATH = "/oauth2/tokenP";
const APPROVAL_PATH = "/oauth2/Approval";

// --- 내부 캐시 ---
let accessToken = null;
let accessTokenExp = 0;      // epoch(ms)
let approvalKey = null;
let approvalExp = 0;

// 동시 갱신 방지용 Promise 락
let tokenRefreshLock = null;
let approvalRefreshLock = null;

// 만료 60초 전이면 갱신
const isExpiring = (expMs, skewMs = 60_000) => Date.now() + skewMs >= expMs;

// 공통 요청 헬퍼
const kis = axios.create({
  baseURL: KIS_BASE_URL,
  timeout: 10_000,
  headers: { "content-type": "application/json; charset=UTF-8" },
});

// --- 액세스 토큰 발급 ---
const requestAccessToken = async () => {
  const body = {
    grant_type: "client_credentials",
    appkey: APP_KEY,
    appsecret: SECRET_KEY,
  };
  const { data } = await kis.post(TOKEN_PATH, body);
  // KIS 응답 형태 예시: { access_token: "...", expires_in: 86400, ... }
  if (!data?.access_token) {
    throw new Error("KIS token response invalid");
  }
  accessToken = data.access_token;
  // expires_in(초) 기준 만료시각 계산 (여유 30초)
  const ttlSec = Number(data.expires_in ?? 24 * 60 * 60);
  accessTokenExp = Date.now() + (ttlSec * 1000);
  return accessToken;
};

// --- 승인키 발급(웹소켓 Approval) ---
const requestApprovalKey = async () => {
  const body = {
    grant_type: "client_credentials",
    appkey: APP_KEY,
    secretkey: SECRET_KEY,
  };
  const { data } = await kis.post(APPROVAL_PATH, body);
  // 보통 { approval_key: "...", ... }
  const key = data?.approval_key || data?.approvalKey || null;
  if (!key) throw new Error("KIS approval response invalid");
  approvalKey = key;
  // 문서상 24시간 유효 → 여유 5분
  approvalExp = Date.now() + (24 * 60 * 60 * 1000);
  return approvalKey;
};

// --- 퍼블릭 API ---
export const createKisAuth = () => {
  // 액세스 토큰 가져오기(캐시 사용)
  const getAccessToken = async () => {
    // 캐시 유효
    if (accessToken && !isExpiring(accessTokenExp)) return accessToken;

    // 이미 다른 요청이 갱신 중이면 그걸 기다림
    if (tokenRefreshLock) return tokenRefreshLock;

    // 새로 갱신
    tokenRefreshLock = (async () => {
      try {
        return await requestAccessToken();
      } finally {
        tokenRefreshLock = null;
      }
    })();

    return tokenRefreshLock;
  };

  // 강제 재발급 (401 등)
  const forceRefreshAccess = async () => {
    if (tokenRefreshLock) return tokenRefreshLock; // 이미 갱신 중이면 그거 기다리기
    tokenRefreshLock = (async () => {
      try {
        return await requestAccessToken();
      } finally {
        tokenRefreshLock = null;
      }
    })();
    return tokenRefreshLock;
  };

  // 승인키 가져오기(캐시 사용)
  const getApprovalKey = async () => {
    if (approvalKey && !isExpiring(approvalExp)) return approvalKey;
    if (approvalRefreshLock) return approvalRefreshLock;

    approvalRefreshLock = (async () => {
      try {
        return await requestApprovalKey();
      } finally {
        approvalRefreshLock = null;
      }
    })();

    return approvalRefreshLock;
  };

  // 강제 재발급
  const forceRefreshApproval = async () => {
    if (approvalRefreshLock) return approvalRefreshLock;
    approvalRefreshLock = (async () => {
      try {
        return await requestApprovalKey();
      } finally {
        approvalRefreshLock = null;
      }
    })();
    return approvalRefreshLock;
  };

  // 디버그용(운영 비권장): 현재 상태 확인 콜백
  const _debugSnapshot = () => ({
    accessTokenPresent: !!accessToken,
    accessTokenExp,
    approvalKeyPresent: !!approvalKey,
    approvalExp,
    now: Date.now(),
  });

  return {
    getAccessToken,
    forceRefreshAccess,
    getApprovalKey,
    forceRefreshApproval,
    _debugSnapshot,
  };
};

const kisAuthSingleton = createKisAuth();
export default kisAuthSingleton;
