// src/ws/server.js
import http from "http";
import { WebSocketServer } from "ws";
import { mountPassThrough } from "./passThrough.js";
import { createSamsungHub } from "./samsungHub.js";

export const startWsServer = (
  auth,
  port = Number(process.env.WS_PORT || 8081)
) => {
  const server = http.createServer();
  const wss = new WebSocketServer({ server });

  // 패스스루
  mountPassThrough(wss, auth);

  // 삼성 집계형
  const hub = createSamsungHub(auth);
  wss.on("connection", async (client, req) => {
    const path = req.url || "";
    const m = path.match(/^\/ws\/samsung\/(trade|quote|expected)$/);
    if (!m) return;
    const channel = m[1]; // 'trade' | 'quote' | 'expected'
    await hub.addSubscriber(channel, client);
  });

  server.listen(port, () =>
    console.log(`WS server on :${port} (/ws/pass, /ws/samsung/:channel)`)
  );
};
