// src/ws/samsungHub.js
import { createKisUpstream } from "./upstream.js";

// KIS tr_id 매핑
const TR_ID = {
  trade: "H0STCNT0",  // 실시간 체결가
  quote: "H0STASP0",  // 실시간 호가
  // expected: "..."   // 실시간 예상체결가(문서 값으로 설정하거나 ENV EXPECTED_TR_ID 사용)
};

const buildKisMsg = ({ approval_key, tr_id, tr_key, tr_type = "1", custtype = "P" }) => ({
  header: {
    approval_key,
    custtype,
    tr_type,               // 기본 '1' (등록)
    "content-type": "utf-8",
  },
  body: { input: { tr_id, tr_key } },
});

export const createSamsungHub = (auth) => {
  const symbol = process.env.SAMSUNG_SYMBOL || "005930";
  const custtype = process.env.KIS_CUSTTYPE || "P";
  const expectedTrId = process.env.EXPECTED_TR_ID; // 문서 값으로 환경변수 주입 권장

  const subs = {
    trade: new Set(),
    quote: new Set(),
    expected: new Set(),
  };

  // 각 채널별 업스트림 생성
  const upstreams = {
    trade: createKisUpstream(auth, {
      name: "trade",
      // 서버 내부에서 approval_key를 얻어 KIS 규격으로 구독
      makeSubscribeMsg: async () => {
        const approval_key = await auth.getApprovalKey();
        return buildKisMsg({ approval_key, tr_id: TR_ID.trade, tr_key: symbol, tr_type: "1", custtype });
      },
      makeUnsubscribeMsg: async () => {
        const approval_key = await auth.getApprovalKey();
        return buildKisMsg({ approval_key, tr_id: TR_ID.trade, tr_key: symbol, tr_type: "2", custtype });
      },
    }),

    quote: createKisUpstream(auth, {
      name: "quote",
      makeSubscribeMsg: async () => {
        const approval_key = await auth.getApprovalKey();
        return buildKisMsg({ approval_key, tr_id: TR_ID.quote, tr_key: symbol, tr_type: "1", custtype });
      },
      makeUnsubscribeMsg: async () => {
        const approval_key = await auth.getApprovalKey();
        return buildKisMsg({ approval_key, tr_id: TR_ID.quote, tr_key: symbol, tr_type: "2", custtype });
      },
    }),

    expected: createKisUpstream(auth, {
      name: "expected",
      makeSubscribeMsg: async () => {
        const tr_id = expectedTrId;
        if (!tr_id) {
          console.warn("[samsungHub] EXPECTED_TR_ID is not set. 'expected' channel will be no-op.");
          return null; // upstream에서 null이면 전송 skip 하도록 가정
        }
        const approval_key = await auth.getApprovalKey();
        return buildKisMsg({ approval_key, tr_id, tr_key: symbol, tr_type: "1", custtype });
      },
      makeUnsubscribeMsg: async () => {
        const tr_id = expectedTrId;
        if (!tr_id) return null;
        const approval_key = await auth.getApprovalKey();
        return buildKisMsg({ approval_key, tr_id, tr_key: symbol, tr_type: "2", custtype });
      },
    }),
  };

  // 업스트림 수신 → 구독자 브로드캐스트
  ["trade", "quote", "expected"].forEach((ch) => {
    upstreams[ch].onMessage((payload) => {
      for (const c of subs[ch]) {
        if (c.readyState === 1) c.send(payload); // 1 = OPEN
      }
    });
  });

  const remove = (channel, ws) => {
    const set = subs[channel];
    set.delete(ws);
    if (set.size === 0) upstreams[channel]?.close();
  };

  const addSubscriber = async (channel, ws) => {
    const set = subs[channel];
    set.add(ws);
    if (set.size === 1) await upstreams[channel].connect(); // 최초 구독자 → 업스트림 연결/구독

    ws.on("close", () => remove(channel, ws));
    ws.on("error", () => {
      try { ws.close(); } catch {}
    });
  };

  return { addSubscriber };
};
