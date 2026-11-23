Lab5_SO - Sisteme de Operare, lucrarea 5

Sarcina19/  IntegritateSistem.java  verificare periodica a integritatii, restaurare din backup, jurnal
Sarcina23/  FolderEncryptor.java    urmarirea unui folder si criptarea fisierelor noi (AES-256-GCM)
Main/       Main.java               un singur meniu cu comenzile ambelor sarcini, legate intre ele

Dependente (23 -> 19):
  - directorul protejat al sarcinii 23 este folderul monitorizat de sarcina 19
  - dupa fiecare criptare, .enc si manifest.csv intra in backup-ul sarcinii 19 (cu SHA-256)
  - un lacat comun (BLOCARE) opreste verificarea cat timp se scrie un fisier criptat
  - un singur jurnal comun: jurnal.log
  - decriptarea se face doar dupa verificarea cu restaurare

Pornire: dublu-click pe Main\pornire.bat (compileaza in "out" si porneste meniul).
Manual, din folderul Main:
  javac -encoding UTF-8 -d out ../Sarcina19/IntegritateSistem.java ../Sarcina23/FolderEncryptor.java Main.java
  java -cp out Main

Folder de lucru: %USERPROFILE%\SO_Lab5_Main  (urmarit, monitorizat = protejat, backup, cheie, decriptat, jurnal.log)
