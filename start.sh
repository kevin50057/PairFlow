#!/usr/bin/env bash
# PairFlow 一鍵啟動：PostgreSQL → 後端 (:8080) → 前端 (:4200)
# 用法：  ./start.sh        結束：在本視窗按 Ctrl + C（或另開視窗跑 ./stop.sh）
set -uo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

say(){ printf "\033[1;35m▸ %s\033[0m\n" "$1"; }
ok(){  printf "\033[1;32m✓ %s\033[0m\n" "$1"; }
err(){ printf "\033[1;31m✗ %s\033[0m\n" "$1"; }

DB_NAME=pairflow; DB_USER=pairflow; DB_PASS=pairflow
DB_URL="postgresql://$DB_USER:$DB_PASS@localhost:5432/$DB_NAME"
# 避開 macOS 偶發的 "postmaster became multithreaded" 問題
export LC_ALL="${LC_ALL:-en_US.UTF-8}" LANG="${LANG:-en_US.UTF-8}"

cleanup(){
  echo; say "關閉中…"
  for p in 4200 8080; do
    pids="$(lsof -ti tcp:$p 2>/dev/null || true)"
    [ -n "$pids" ] && kill $pids 2>/dev/null || true
  done
  ok "前端／後端已停止（PostgreSQL 仍在背景，需要時用 brew services stop postgresql@16 關閉）"
  exit 0
}
trap cleanup INT TERM

wait_http(){ # url name max_seconds
  local url="$1" name="$2" max="${3:-120}" i=0
  until curl -fs -o /dev/null "$url" 2>/dev/null; do
    i=$((i+1)); [ "$i" -ge "$max" ] && { err "$name 等待逾時"; return 1; }
    sleep 1
  done; ok "$name 就緒"
}

# 1) PostgreSQL ----------------------------------------------------------------
say "檢查 PostgreSQL…"
if ! psql "$DB_URL" -c "select 1" >/dev/null 2>&1; then
  say "啟動 PostgreSQL (brew services)…"
  brew services start postgresql@16 >/dev/null 2>&1 || true
  for _ in $(seq 1 30); do pg_isready -h localhost -q && break; sleep 1; done
  # 第一次：建立資料庫與帳號（用本機超級使用者，失敗就忽略，代表已存在）
  if ! psql "$DB_URL" -c "select 1" >/dev/null 2>&1; then
    say "首次設定資料庫與帳號…"
    createdb "$DB_NAME" 2>/dev/null || true
    psql -d "$DB_NAME" -c "DO \$\$ BEGIN IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname='$DB_USER') THEN CREATE ROLE $DB_USER LOGIN PASSWORD '$DB_PASS'; END IF; END \$\$;" 2>/dev/null || true
    psql -d "$DB_NAME" -c "ALTER DATABASE $DB_NAME OWNER TO $DB_USER; ALTER SCHEMA public OWNER TO $DB_USER;" 2>/dev/null || true
  fi
fi
if psql "$DB_URL" -c "select 1" >/dev/null 2>&1; then ok "PostgreSQL 已連線"
else err "PostgreSQL 無法連線，請看 README 的「疑難排解」"; exit 1; fi

# 2) 後端 (Spring Boot) --------------------------------------------------------
if curl -fs -o /dev/null http://localhost:8080/api/health 2>/dev/null; then
  ok "後端已在執行 (:8080)"
else
  say "啟動後端 (Spring Boot)…  log → backend/.run.log"
  ( cd backend && exec mvn -q spring-boot:run >"$ROOT/backend/.run.log" 2>&1 ) &
  wait_http http://localhost:8080/api/health "後端 :8080" 180 || { err "後端啟動失敗，請看 backend/.run.log"; cleanup; }
fi

# 3) 前端 (Angular) ------------------------------------------------------------
if curl -fs -o /dev/null http://localhost:4200 2>/dev/null; then
  ok "前端已在執行 (:4200)"
else
  [ -d frontend/node_modules ] || { say "安裝前端套件 (npm install)…"; ( cd frontend && npm install ); }
  say "啟動前端 (Angular)…  log → frontend/.run.log"
  ( cd frontend && exec npm start >"$ROOT/frontend/.run.log" 2>&1 ) &
  wait_http http://localhost:4200 "前端 :4200" 180 || { err "前端啟動失敗，請看 frontend/.run.log"; cleanup; }
fi

echo
ok "PairFlow 已啟動！"
printf "  開啟：\033[1;36m http://localhost:4200\033[0m\n"
printf "  帳號： kevin@pairflow.test\n"
printf "  密碼： secret123\n\n"
say "按 Ctrl + C 結束（請保持本視窗開著）"

# 保持前景，讓 Ctrl + C 能優雅關閉
tail -f /dev/null & wait $!
