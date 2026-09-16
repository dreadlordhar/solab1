# Timer Manager

Aplicație desktop JavaFX pentru lucrarea de laborator **"Elaborarea unui mecanism de
planificare a activității proceselor utilizînd Timer-ul"**.

Aplicația permite crearea și rularea simultană a mai multor timere, demonstrând
cele 3 moduri cerute, toate implementate cu `java.util.Timer` / `java.util.TimerTask`:

1. **Delay** — execuție unică, după un număr de secunde ales de utilizator.
2. **Specific Time** — execuție la o dată/oră exactă aleasă de utilizator.
3. **Periodic** — execuție repetată, la un interval indicat de utilizator.

## Structura proiectului

```
timer-manager/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/lab/timermanager/
    │   ├── MainApp.java                 # punctul de intrare (JavaFX Application)
    │   ├── model/
    │   │   ├── TimerModel.java          # starea unui timer (JavaFX properties)
    │   │   ├── TimerType.java           # DELAY / SPECIFIC_TIME / PERIODIC
    │   │   └── TimerStatus.java         # RUNNING / COMPLETED / STOPPED
    │   ├── service/
    │   │   └── TimerService.java        # toată logica Timer / TimerTask e AICI
    │   ├── controller/
    │   │   └── MainController.java      # leagă view-ul de TimerService
    │   └── view/
    │       ├── TimerCardView.java       # cardul vizual al unui timer
    │       └── NewTimerDialog.java      # dialogul "+ New Timer"
    └── resources/
        ├── com/lab/timermanager/view/MainView.fxml   # layout-ul general (FXML)
        └── css/style.css                              # tot design-ul (CSS)
```

Separarea este strictă: `model` nu știe nimic despre UI, `service` este singurul loc
care creează obiecte `Timer`/`TimerTask`, iar `controller`/`view` doar afișează starea
și transmit comenzi (`startTimer`, `stopTimer`, `deleteTimer`) către `service`.

## Cerințe

* JDK 17+
* Maven 3.8+ (sau IntelliJ IDEA cu Maven integrat)
* Conexiune la internet la prima compilare (Maven descarcă dependențele JavaFX
  `org.openjfx:javafx-controls` și `org.openjfx:javafx-fxml`, versiunea 21.0.2)

## Rulare

Din directorul `timer-manager/`:

```bash
mvn clean javafx:run
```

Alternativ, pentru a produce un JAR executabil (conține toate dependințele):

```bash
mvn clean package
java -jar target/timer-manager-1.0.0.jar
```

### Rulare din IntelliJ IDEA

1. `File → Open` → selectează folderul `timer-manager` (proiect Maven, se importă automat).
2. Așteaptă descărcarea dependențelor.
3. Rulează clasa `com.lab.timermanager.MainApp` (Run configuration de tip Application),
   sau folosește task-ul Maven `javafx:run` din panoul Maven.

## Utilizare

1. Apasă **"+ New Timer"**.
2. Alege un nume, tipul timerului (Delay / Specific Time / Periodic) și parametrii
   corespunzători.
3. Apasă **Create** — timerul pornește automat și apare ca un card.
4. Fiecare card arată tipul, detaliile de planificare, timpul rămas / următoarea
   execuție (actualizat live, în fiecare secundă) și statusul curent
   (`Running` / `Completed` / `Stopped`).
5. Butoanele **Start / Stop / Delete** controlează independent fiecare timer —
   poți avea oricâte timere active simultan.

## Notă despre implementare

* Fiecare timer pornit de utilizator primește propriul obiect `java.util.Timer`
  (fir de execuție separat), oprit explicit prin `cancel()` la Stop/Delete.
* Un singur `Timer` suplimentar ("ticker"), pornit cu `scheduleAtFixedRate`, rulează
  o dată pe secundă și recalculează textul de countdown pentru toate cardurile — evită
  crearea a zeci de fire doar pentru actualizarea vizuală.
* Actualizările de UI se fac prin `Platform.runLater(...)`, deoarece `TimerTask.run()`
  rulează pe firul propriu al `Timer`-ului, nu pe firul JavaFX Application Thread.
