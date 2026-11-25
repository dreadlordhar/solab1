# Imagine proprie: lab 1 (Java Swing, Timere) rulat in container si afisat in browser (noVNC)

# --- etapa 1: compilare
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /src
COPY src/ .
RUN javac -encoding UTF-8 -d /out *.java

# --- etapa 2: rulare (JRE + ecran virtual + noVNC)
FROM eclipse-temurin:17-jre-jammy
ENV DEBIAN_FRONTEND=noninteractive \
    DISPLAY=:0
RUN apt-get update && apt-get install -y --no-install-recommends \
      xvfb x11vnc fluxbox novnc websockify wget \
      fontconfig fonts-dejavu-core libfreetype6 \
      libxtst6 libxi6 libxrender1 libxext6 libx11-6 \
    && ln -s /usr/share/novnc/vnc.html /usr/share/novnc/index.html \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /out /app
COPY start.sh /start.sh
RUN chmod +x /start.sh

EXPOSE 6080
HEALTHCHECK --interval=15s --timeout=5s --start-period=25s --retries=3 \
  CMD wget -qO- http://127.0.0.1:6080/ >/dev/null || exit 1

CMD ["/start.sh"]
