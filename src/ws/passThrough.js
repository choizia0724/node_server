// src/ws/passThrough.js
import WebSocket from "ws";

const TR_ID_MAP = {
  trade: "H0STCNT0",  // 실시간 체결가
  quote: "H0STASP0",  // 실시간 호가
  // expected: "..."   // 실시간 예상체결가: KIS 문서의 정확한 tr_id로 채워주세요(또는 ENV로 주입)
};

const buildKisMsg = ({ approval_key, tr_id, tr_key, tr_type, custtype = "P" }) => ({
  header: {
    approval_key,
    custtype,          // 개인 P / 법인 B
    tr_type,           // '1' 등록 / '2' 해제
    "content-type": "utf-8",
  },
  body: {
    input: { tr_id, tr_key },
  },
});

export const mountPassThrough = (wss, auth) => {
  const kisWsUrl = process.env.KIS_WS_URL;
  if (!kisWsUrl) throw new Error("Missing env: KIS_WS_URL");
  const custtype = process.env.KIS_CUSTTYPE || "P";

  wss.on("connection", (client, req) => {
    const path = req.url || "";
    if (!path.startsWith("/ws/pass")) return;

    let upstream = null;
    let reconnectTimer = null;

    const connectUpstream = async () => {
      // KIS WS는 핸드셰이크 헤더도 요구하는 환경이 있으므로 그대로 유지
      const approval = await auth.getApprovalKey();
      upstream = new WebSocket(kisWsUrl, undefined, {
        headers: {
          approval_key: approval,
          appkey: process.env.KIS_APP_KEY,
          secretkey: process.env.KIS_SECRET_KEY,
        },
      });

      upstream.on("open", () => {
        // 클라 → 서버: 간단포맷 or KIS원본포맷 모두 수용
        client.on("message", async (raw) => {
          // 연결 중간에 재연결될 수 있으니 매번 최신 approval 사용
          const approval_key = await auth.getApprovalKey();
          try {
            const msg = JSON.parse(raw.toString());

            // 1) 사용자가 KIS 원본 포맷을 직접 보낸 경우: approval_key만 채워서 전달
            if (msg?.header && msg?.body?.input?.tr_id && msg?.body?.input?.tr_key) {
              const out = {
                ...msg,
                header: { ...msg.header, approval_key: msg.header.approval_key || approval_key },
              };
              if (upstream.readyState === WebSocket.OPEN) upstream.send(JSON.stringify(out));
              return;
            }

            // 2) 간단 포맷: { type: 'trade'|'quote'|'expected', symbol: '005930', op?: 'subscribe'|'unsubscribe' }
            const type = String(msg.type || "").toLowerCase();
            const tr_id = TR_ID_MAP[type] || process.env.EXPECTED_TR_ID; // expected 대응
            const tr_key = msg.symbol;
            const tr_type = (String(msg.op || "subscribe").toLowerCase() === "unsubscribe") ? "2" : "1";

            if (!tr_id || !tr_key) {
              client.send(JSON.stringify({ error: "invalid payload; require {type, symbol} or KIS-format body.input" }));
              return;
            }

            const kisPayload = buildKisMsg({ approval_key, tr_id, tr_key, tr_type, custtype });
            if (upstream.readyState === WebSocket.OPEN) upstream.send(JSON.stringify(kisPayload));
          } catch {
            // JSON 아님 → 원문 그대로(고급 사용자 시나리오)
            if (upstream.readyState === WebSocket.OPEN) upstream.send(raw);
          }
        });
      });

      // 업스트림 → 클라
      upstream.on("message", (buf) => {
        if (client.readyState === WebSocket.OPEN) client.send(buf.toString());
      });

      upstream.on("close", async () => {
        // 승인키 갱신 후 재연결 (간단 백오프)
        try { await auth.forceRefreshApproval?.(); } catch {}
        if (!reconnectTimer) {
          reconnectTimer = setTimeout(() => {
            reconnectTimer = null;
            if (client.readyState === WebSocket.OPEN) connectUpstream();
          }, 1500);
        }
      });

      upstream.on("error", () => {
        try { upstream?.close(); } catch {}
      });
    };

    // 최초 연결
    connectUpstream().catch((e) => {
      console.error("[passThrough] upstream connect failed:", e?.message || e);
      client.close();
    });

    const cleanup = () => {
      try { upstream?.close(); } catch {}
      try { client.close(); } catch {}
    };
    client.on("close", cleanup);
    client.on("error", cleanup);
  });
};
