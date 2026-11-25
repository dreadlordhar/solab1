# Imagine proprie: foaia de stil (CSS) a site-ului
FROM nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY style.css /usr/share/nginx/html/style.css

EXPOSE 80

HEALTHCHECK --interval=10s --timeout=3s --retries=3 \
  CMD wget -qO- http://127.0.0.1/style.css >/dev/null || exit 1
