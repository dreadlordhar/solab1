# Lucrare de laborator 7 — Turcanu Damian

Soluție pentru opțiunea dificilă: un CV cu o singură pagină web, imaginile și foaia CSS sunt construite în trei imagini și rulează în trei containere separate. Containerul web expune pagina pe http://localhost:8097 și transmite cererile /assets/images/ și /assets/styles/ către serviciile dedicate prin rețeaua Compose.

Este inclusă și aplicația Java cu Timer și TimerTask cerută de laboratorul 1. Aceasta rulează ca un serviciu suplimentar, izolat de rețea; când sarcina este terminată, containerul se oprește cu codul 0.

## Structura

~~~text
compose.yaml
web/       Dockerfile, nginx.conf, default.conf, index.html, app.js
images/    Dockerfile, nginx.conf, default.conf, avatar.svg, stack.svg
styles/    Dockerfile, nginx.conf, default.conf, style.css
timer/     Dockerfile, TimerApp.java
~~~

~~~mermaid
flowchart LR
  B[Browser localhost:8097] --> W[web container<br/>HTML and reverse proxy]
  W -->|/assets/images/| I[images container<br/>SVG files]
  W -->|/assets/styles/| C[styles container<br/>CSS file]
  T[timer-lab1 container<br/>Java Timer and TimerTask] -. isolated with network none .- W
~~~

## Pornire și verificare

Docker Desktop trebuie să fie pornit. Din acest director:

~~~powershell
docker compose --profile lab1 up --build -d
docker compose ps -a
docker compose logs --no-log-prefix timer-lab1
Invoke-WebRequest http://localhost:8097/api/health | Select-Object -ExpandProperty Content
(Invoke-WebRequest -Method Head http://localhost:8097/assets/styles/style.css).Headers['X-Asset-Source']
(Invoke-WebRequest -Method Head http://localhost:8097/assets/images/avatar.svg).Headers['X-Asset-Source']
~~~

Pagina poate fi accesată la http://localhost:8097. În lista containerelor, web, images și styles trebuie să fie healthy; timer-lab1 apare ca Exited (0) după finalizarea celor trei planificări.

Pentru a opri containerele proiectului:

~~~powershell
docker compose down
~~~

Imaginile sunt păstrate pentru o pornire ulterioară. Portul poate fi schimbat prin variabila LAB7_PORT, de exemplu: $env:LAB7_PORT = '8098'.

## Arhitectură și securitate

- Serviciile comunică prin DNS-ul rețelei Docker folosind numele images și styles; nu se fixează adrese IP.
- Numai containerul web publică un port, legat la interfața locală 127.0.0.1.
- Serverele Nginx rulează cu UID 101, pe portul neprivilegiat 8080, cu sistem de fișiere read-only și capabilități eliminate.
- Aplicația Timer rulează ca UID 10001, fără rețea, într-o imagine Java runtime construită în mai multe etape.
- Resursele sunt limitate la 0,25 CPU și 64 MiB pentru fiecare serviciu Nginx, respectiv 128 MiB pentru sarcina Java.
- Fișierele statice sunt incluse în imaginile lor. Nu este necesar un volum persistent, deoarece site-ul nu modifică date la execuție.

## Surse tehnice

- Docker Docs, [Networking in Compose](https://docs.docker.com/compose/how-tos/networking/).
- Docker Docs, [Control startup and shutdown order in Compose](https://docs.docker.com/compose/how-tos/startup-order/).
- Docker Docs, [Building best practices](https://docs.docker.com/build/building/best-practices/).
