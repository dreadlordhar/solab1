@echo off
cd /d %~dp0
javac -encoding UTF-8 -d out IntegritateSistem.java && java -cp out IntegritateSistem
pause
