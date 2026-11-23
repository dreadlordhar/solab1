@echo off
cd /d %~dp0
javac -encoding UTF-8 IntegritateSistem.java FolderEncryptor.java SistemProtejat.java && java SistemProtejat --interval 10
pause
