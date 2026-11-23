@echo off
cd /d %~dp0
javac -encoding UTF-8 -d out ..\Sarcina19\IntegritateSistem.java ..\Sarcina23\FolderEncryptor.java Main.java && java -cp out Main --interval 10
pause
