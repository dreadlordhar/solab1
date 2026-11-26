FROM eclipse-temurin:21-jdk-jammy

RUN apt-get update && apt-get install -y \
    xvfb \
    x11vnc \
    novnc \
    websockify \
    fluxbox \
    x11-utils \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY *.java ./

RUN javac -encoding UTF-8 *.java

COPY start.sh /app/start.sh

RUN sed -i 's/\r$//' /app/start.sh \
    && chmod +x /app/start.sh

ENV DISPLAY=:99

EXPOSE 6080

CMD ["/bin/bash", "/app/start.sh"]