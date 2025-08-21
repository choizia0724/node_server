// src/ws/upstream.js
import { WebSocket } from "ws";

export const createKisUpstream = (auth, opts) => {
  const kisWsUrl = opts.kisWsUrl || process.env.KIS_WS_URL;
  if (!kisWsUrl) throw new Error("Missing env: KIS_WS_URL");

  let ws = null;
  let closedByMe = false;
  let reconnectTimer = null;
  let messageHandler = null;

  const connect = async () => {
    if (ws?.readyState === WebSocket.OPEN) return;
    closedByMe = false;

    const approval = await auth.getApprovalKey();

    ws = new WebSocket(kisWsUrl, undefined, {
      headers: {
        approval_key: approval,
        appkey: process.env.KIS_APP_KEY,
        secretkey: process.env.KIS_SECRET_KEY,
      },
    });

    ws.on("open", () => {
      const msg = opts.makeSubscribeMsg();
      ws?.send(JSON.stringify(msg));
    });

    ws.on("message", (buf) => {
      const s = buf.toString();
      messageHandler && messageHandler(s);
    });

    ws.on("close", async () => {
      if (closedByMe) return;
      await auth.forceRefreshApproval();
      scheduleReconnect();
    });

    ws.on("error", () => {
      try {
        ws?.close();
      } catch {}
    });
  };

  const scheduleReconnect = () => {
    if (reconnectTimer) return;
    reconnectTimer = setTimeout(() => {
      reconnectTimer = null;
      connect().catch(scheduleReconnect);
    }, 1500);
  };

  const close = () => {
    closedByMe = true;
    try {
      ws?.close();
    } catch {}
  };

  const send = (obj) => {
    if (ws?.readyState === WebSocket.OPEN) ws.send(JSON.stringify(obj));
  };

  const onMessage = (handler) => {
    messageHandler = handler;
  };

  return { connect, close, send, onMessage };
};
