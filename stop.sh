#!/usr/bin/env bash
# 停止 PairFlow 的前端 (:4200) 與後端 (:8080)。PostgreSQL 不會被關閉。
for p in 4200 8080; do
  pids="$(lsof -ti tcp:$p 2>/dev/null || true)"
  if [ -n "$pids" ]; then kill $pids 2>/dev/null && echo "✓ 已停止 :$p"; else echo "· :$p 沒有在執行"; fi
done
echo "（PostgreSQL 仍在執行；要一起關閉請跑：brew services stop postgresql@16）"
