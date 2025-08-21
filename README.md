
```angular2html
[Browser/Next.js]
   │
   ├─ REST: GET /api/stock/:code  ──────────────▶  [src/rest/router.js] ──(Bearer access_token)──▶ KIS REST
   │
   └─ WS:
       ├─ ws://.../ws/pass  ──▶ [passThrough.js] ── connect ──▶ KIS WS (심볼/채널 프록시)
       └─ ws://.../ws/samsung/{trade|quote|expected}
                            └─▶ [samsungHub.js] ──(단일 upstream 유지)──▶ KIS WS
                                         ▲
                                         └──────── [upstream.js] (승인키·재연결)

```