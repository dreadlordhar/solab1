#!/bin/sh
# Ecran virtual + VNC + noVNC (acces din browser) + aplicatia Swing

Xvfb :0 -screen 0 1024x700x24 -nolisten tcp &
until [ -e /tmp/.X11-unix/X0 ]; do sleep 0.2; done

fluxbox >/dev/null 2>&1 &
x11vnc -display :0 -forever -shared -nopw -rfbport 5900 -quiet >/dev/null 2>&1 &
websockify --web=/usr/share/novnc 6080 localhost:5900 &

# daca fereastra se inchide, aplicatia se reporneste
while true; do
  java -Xmx128m -cp /app MainFrame
  sleep 1
done
