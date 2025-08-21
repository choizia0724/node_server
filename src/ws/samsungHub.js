// src/ws/samsungHub.js
import { createKisUpstream } from "./upstream.js";

export const createSamsungHub = (auth) => {
  const symbol = process.env.SAMSUNG_SYMBOL || "005930";

  const subs = {
    trade: new Set(),
    quote: new Set(),
    expected: new Set(),
  };

  const upstreams = {
    trade: createKisUpstream(auth, {
      name: "trade",
      makeSubscribeMsg: () => ({
        action: "subscribe",
        channel: "trade",
        symbols: [symbol],
      }),
    }),
    quote: createKisUpstream(auth, {
      name: "quote",
      makeSubscribeMsg: () => ({
        action: "subscribe",
        channel: "quote",
        symbols: [symbol],
      }),
    }),
    expected: createKisUpstream(auth, {
      name: "expected",
      makeSubscribeMsg: () => ({
        action: "subscribe",
        channel: "expected",
        symbols: [symbol],
      }),
    }),
  };

  ["trade", "quote", "expected"].forEach((ch) => {
    upstreams[ch].onMessage((s) => {
      for (const c of subs[ch]) if (c.readyState === 1) c.send(s); // 1 = OPEN
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
    if (set.size === 1) await upstreams[channel].connect();

    ws.on("close", () => remove(channel, ws));
    ws.on("error", () => {
      try {
        ws.close();
      } catch {}
    });
  };

  return { addSubscriber };
};
