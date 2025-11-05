-- 업데이트 (DB쪽 접두 'A' 제거 + 대소문자 무시)
UPDATE stocks.stock_table s
SET use_or_not = TRUE
FROM stocks.symbol_whitelist t
WHERE UPPER(regexp_replace(s.symbol, '^A','')) = UPPER(t.symbol);