@echo off
cd /d %~dp0
javac -encoding UTF-8 -d out FolderEncryptor.java && java -cp out FolderEncryptor urmarit protejat
pause
