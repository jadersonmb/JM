#!/bin/sh
set -e

DOMAIN="${NGINX_DOMAIN:-www.macromv.com}"
LE_DIR="/etc/letsencrypt"
LE_LIVE_DIR="$LE_DIR/live/$DOMAIN"
TEMPLATE_DIR="/opt/letsencrypt"
CHECK_INTERVAL="${NGINX_CERT_CHECK_INTERVAL:-300}"

mkdir -p /var/www/certbot
mkdir -p "$LE_DIR"

if [ ! -f "$LE_DIR/options-ssl-nginx.conf" ]; then
  mkdir -p "$LE_DIR"
  cp "$TEMPLATE_DIR/options-ssl-nginx.conf" "$LE_DIR/options-ssl-nginx.conf"
fi

if [ ! -f "$LE_DIR/ssl-dhparams.pem" ]; then
  cp "$TEMPLATE_DIR/ssl-dhparams.pem" "$LE_DIR/ssl-dhparams.pem"
fi

ensure_self_signed_certificate() {
  if [ -f "$LE_LIVE_DIR/fullchain.pem" ] && [ -f "$LE_LIVE_DIR/privkey.pem" ]; then
    return
  fi

  echo "[nginx] No existing certificate for $DOMAIN. Generating temporary self-signed certificate."
  mkdir -p "$LE_LIVE_DIR"
  openssl req -x509 -nodes -newkey rsa:2048 -days 1 \
    -keyout "$LE_LIVE_DIR/privkey.pem" \
    -out "$LE_LIVE_DIR/fullchain.pem" \
    -subj "/CN=$DOMAIN" >/dev/null 2>&1
  cp "$LE_LIVE_DIR/fullchain.pem" "$LE_LIVE_DIR/chain.pem"
}

wait_for_nginx() {
  while :; do
    if command -v pidof >/dev/null 2>&1; then
      pidof nginx >/dev/null 2>&1 && break
    elif command -v pgrep >/dev/null 2>&1; then
      pgrep nginx >/dev/null 2>&1 && break
    elif [ -f /var/run/nginx.pid ]; then
      break
    fi
    sleep 1
  done
}

watch_for_certificate_updates() {
  local current_fingerprint=""

  if [ -f "$LE_LIVE_DIR/fullchain.pem" ] && [ -f "$LE_LIVE_DIR/privkey.pem" ]; then
    current_fingerprint=$(sha256sum "$LE_LIVE_DIR/fullchain.pem" "$LE_LIVE_DIR/privkey.pem" 2>/dev/null || true)
  fi

  wait_for_nginx

  while sleep "$CHECK_INTERVAL"; do
    if [ ! -f "$LE_LIVE_DIR/fullchain.pem" ] || [ ! -f "$LE_LIVE_DIR/privkey.pem" ]; then
      continue
    fi

    local new_fingerprint
    new_fingerprint=$(sha256sum "$LE_LIVE_DIR/fullchain.pem" "$LE_LIVE_DIR/privkey.pem" 2>/dev/null || true)

    if [ -n "$new_fingerprint" ] && [ "$new_fingerprint" != "$current_fingerprint" ]; then
      current_fingerprint="$new_fingerprint"
      echo "[nginx] Detected updated certificate material. Reloading Nginx."
      nginx -s reload || true
    fi
  done
}

ensure_self_signed_certificate
watch_for_certificate_updates &

exec "$@"
