#!/bin/sh
set -e

DOMAIN="${NGINX_DOMAIN:-www.macromv.com}"
LE_DIR="/etc/letsencrypt"
LE_LIVE_DIR="$LE_DIR/live/$DOMAIN"
TEMPLATE_DIR="/opt/letsencrypt"

mkdir -p /var/www/certbot
mkdir -p "$LE_DIR"

if [ ! -f "$LE_DIR/options-ssl-nginx.conf" ]; then
  mkdir -p "$LE_DIR"
  cp "$TEMPLATE_DIR/options-ssl-nginx.conf" "$LE_DIR/options-ssl-nginx.conf"
fi

if [ ! -f "$LE_DIR/ssl-dhparams.pem" ]; then
  cp "$TEMPLATE_DIR/ssl-dhparams.pem" "$LE_DIR/ssl-dhparams.pem"
fi

if [ ! -f "$LE_LIVE_DIR/fullchain.pem" ] || [ ! -f "$LE_LIVE_DIR/privkey.pem" ]; then
  echo "[nginx] No existing certificate for $DOMAIN. Generating temporary self-signed certificate."
  mkdir -p "$LE_LIVE_DIR"
  openssl req -x509 -nodes -newkey rsa:2048 -days 1 \
    -keyout "$LE_LIVE_DIR/privkey.pem" \
    -out "$LE_LIVE_DIR/fullchain.pem" \
    -subj "/CN=$DOMAIN" >/dev/null 2>&1
  cp "$LE_LIVE_DIR/fullchain.pem" "$LE_LIVE_DIR/chain.pem"
fi

exec "$@"
