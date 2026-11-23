@echo off
cd /d %~dp0
javac -encoding UTF-8 FolderEncryptor.java && java FolderEncryptor urmarit protejat
pause
