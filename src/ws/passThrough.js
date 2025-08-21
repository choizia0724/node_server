// src/ws/passThrough.js
import { WebSocket } from "ws";

export const mountPassThrough = (wss, auth) => {
  const kisWsUrl = process.env.KIS_WS_URL;
  if (!kisWsUrl) throw new Error("Missing env: KIS_WS_URL");

  const handleConnection = async (client, path) => {
    if (!path.startsWith("/ws/pass")) return;

    let upstream = null;

    const connect = async () => {
      const approval = await auth.getApprovalKey();

      upstream = new WebSocket(kisWsUrl, undefined, {
        headers: {
          approval_key: approval,
          appkey: process.env.KIS_APP_KEY,
          secretkey: process.env.KIS_SECRET_KEY,
        },
      });

      upstream.on("open", () => {
        client.on("message", (msg) => {
          if (upstream?.readyState === WebSocket.OPEN) upstream.send(msg);
        });
      });

      upstream.on("message", (buf) => {
        if (client.readyState === WebSocket.OPEN) client.send(buf.toString());
      });

      upstream.on("close", async () => {
        await auth.forceRefreshApproval();
        setTimeout(connect, 1500);
      });

      upstream.on("error", () => {
        try {
          upstream?.close();
        } catch {}
      });
    };

    await connect();

    const cleanup = () => {
      try {
        upstream?.close();
      } catch {}
      try {
        client.close();
      } catch {}
    };
    client.on("close", cleanup);
    client.on("error", cleanup);
  };

  wss.on("connection", (client, req) =>
    handleConnection(client, req.url || "")
  );
};
