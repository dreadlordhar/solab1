#!/bin/bash
set -e

export DISPLAY=:99

Xvfb :99 -screen 0 1024x768x24 &

until xdpyinfo -display :99 >/dev/null 2>&1; do
    sleep 1
done

fluxbox &

x11vnc -display :99 -forever -shared \
    -nopw -localhost -rfbport 5900 &

websockify --web=/usr/share/novnc/ \
    6080 localhost:5900 &

exec java TimerLabGUI