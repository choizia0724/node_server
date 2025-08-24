// src/ws/server.js
import { WebSocketServer } from "ws";
import { mountPassThrough } from "./passThrough.js";
import { createSamsungHub } from "./samsungHub.js";

/**
 * 기존 http.Server 인스턴스에 WebSocket 엔드포인트를 붙입니다.
 * - 여기서는 listen을 호출하지 않습니다(바깥에서 한 번만 server.listen()).
 * - 경로:
 *   - /ws/pass
 *   - /ws/samsung/:channel  (trade | quote | expected)
 */
export const startWsServer = (auth, server) => {
  console.log(auth)
  if (!server) throw new Error("startWsServer requires an http.Server instance");

  const wssPass = new WebSocketServer({ noServer: true });
  const wssSamsung = new WebSocketServer({ noServer: true });

  // 1) 패스스루: 프론트와 업스트림을 그대로 중계
  mountPassThrough(wssPass, auth);

  // 2) 삼성 허브: 채널별 집계형 브로드캐스트
  const hub = createSamsungHub(auth);
  wssSamsung.on("connection", async (client, req) => {
    const path = req.url || "";
    const m = path.match(/^\/ws\/samsung\/(trade|quote|expected)$/);
    if (!m) return client.close();
    await hub.addSubscriber(m[1], client); // m[1] = channel
  });

  // 업그레이드 라우팅
  server.on("upgrade", (req, socket, head) => {
    const url = req.url || "";

    if (url.startsWith("/ws/pass")) {
      wssPass.handleUpgrade(req, socket, head, (ws) => {
        wssPass.emit("connection", ws, req);
      });
      return;
    }

    if (/^\/ws\/samsung\/(trade|quote|expected)$/.test(url)) {
      wssSamsung.handleUpgrade(req, socket, head, (ws) => {
        wssSamsung.emit("connection", ws, req);
      });
      return;
    }

    // 허용하지 않은 경로는 거절
    socket.destroy();
  });

  return server;
};
