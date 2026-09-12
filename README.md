# Space Ship Timer

Aplicatie Java Swing cu doua moduri de timer, realizata folosind clasele
`Timer` si `TimerTask`.

## Structura aplicatiei

```text
solab1/
├── README.md
└── src/
	 ├── Main.java
	 ├── TimerModel.java
	 ├── TimerService.java
	 ├── TimerController.java
	 ├── TimerPanel.java
	 └── RoundedProgressBar.java
```

### `src/`

Folderul contine codul sursa al aplicatiei.

- `Main.java` porneste aplicatia si creeaza componentele principale.
- `TimerModel.java` pastreaza starea Timer 1: timpul total, timpul ramas,
	decrementarea, progresul si resetarea.
- `TimerService.java` gestioneaza executia cu `Timer` si `TimerTask`. Aici
	sunt programate atat countdown-ul periodic, cat si programarile la o ora
	exacta.
- `TimerController.java` leaga interfata de model si serviciu. Primeste
	actiunile butoanelor si coordoneaza rezultatul.
- `TimerPanel.java` contine interfata Swing: campuri, butoane, pagini,
	animatia dintre timere, ceasul real si istoricul programarilor.
- `RoundedProgressBar.java` este componenta grafica pentru bara de progres.

## Functionalitati

### Timer 1

Countdown cu minute si secunde. Dupa pornire, `TimerTask` se executa la
fiecare secunda si scade timpul ramas.

### Timer 2

Programare la o ora exacta (`HH:MM:SS`). Pagina afiseaza si ceasul real,
permite adaugarea mai multor programari si ofera posibilitatea de a sterge
elementele din istoric.

Butonul `TIMER 2 >>` schimba pagina cu o animatie de glisare. Pe Timer 2
butonul `PAUSE` este ascuns, deoarece programarile sunt independente.

## Cum pornesti aplicatia

1. Deschide un terminal in folderul proiectului:

	```powershell
	cd "C:\Users\Lenovo\Desktop\os\solab1"
	```

2. Compileaza toate clasele:

	```powershell
	javac -d out src\*.java
	```

3. Porneste aplicatia:

	```powershell
	java -cp out Main
	```

Sau poti executa compilarea si pornirea intr-o singura linie:

```powershell
javac -d out src\*.java; java -cp out Main
```

## Sfaturi

- Ruleaza comenzile din folderul radacina `solab1`, nu din `src`.
- Daca modifici codul, compileaza din nou inainte de a porni aplicatia.
- Pentru Timer 1, introdu un interval mai mare decat zero.
- Pentru Timer 2, alege o ora viitoare; o ora deja trecuta este respinsa.
- Pentru mai multe programari, schimba ora si apasa `START` din nou.
- Selecteaza o programare din istoric inainte sa apesi `STERGE`.
- `RESET` opreste timer-ele active si readuce valorile initiale.
