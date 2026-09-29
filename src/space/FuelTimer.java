package space;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class FuelTimer {

    // ==========================================
    // VALORI
    // ==========================================

    private int fuel = 100;

    private int oxygen = 30;

    private final long launchDuration = 10000;


    // ==========================================
    // TIMER-E
    // ==========================================

    private Timer launchTimer;

    private Timer fuelTimer;

    private Timer oxygenTimer;

    private Timer eventTimer;


    // ==========================================
    // LISTENERE
    // ==========================================

    private Consumer<Long> countdownListener;

    private Consumer<Integer> fuelListener;

    private Consumer<Integer> oxygenListener;

    private Runnable launchEventListener;

    private Runnable satelliteEventListener;

    private Runnable oxygenEmptyListener;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public FuelTimer(
            Consumer<Long> countdownListener,
            Consumer<Integer> fuelListener,
            Consumer<Integer> oxygenListener,
            Runnable launchEventListener,
            Runnable satelliteEventListener,
            Runnable oxygenEmptyListener
    ) {

        this.countdownListener = countdownListener;

        this.fuelListener = fuelListener;

        this.oxygenListener = oxygenListener;

        this.launchEventListener = launchEventListener;

        this.satelliteEventListener = satelliteEventListener;

        this.oxygenEmptyListener = oxygenEmptyListener;
    }


    // ==========================================
    // START
    // ==========================================

    public void start() {

        startLaunchCountdown();
    }


    // ==========================================
    // COUNTDOWN LANSARE
    // ==========================================

    private void startLaunchCountdown() {

        final long startTime =
                System.currentTimeMillis();


        launchTimer = new Timer();


        TimerTask launchTask = new TimerTask() {

            @Override
            public void run() {

                long elapsed =
                        System.currentTimeMillis()
                                - startTime;


                long remaining =
                        launchDuration - elapsed;


                if (remaining < 0) {

                    remaining = 0;
                }


                // Trimitem timpul către interfață

                if (countdownListener != null) {

                    countdownListener.accept(
                            remaining
                    );
                }


                // Când ajunge la 0

                if (remaining <= 0) {

                    launchTimer.cancel();


                    // Eveniment lansare

                    if (launchEventListener != null) {

                        launchEventListener.run();
                    }


                    // Pornim combustibilul

                    startFuelTimer();


                    // Pornim oxigenul

                    startOxygenTimer();


                    // Pornim timerul pentru satelit

                    startSatelliteTimer();
                }
            }
        };


        // Actualizare la fiecare 10 ms

        launchTimer.scheduleAtFixedRate(
                launchTask,
                0,
                10
        );
    }


    // ==========================================
    // TIMER COMBUSTIBIL
    // ==========================================

    private void startFuelTimer() {

        fuel = 100;


        fuelTimer = new Timer();


        TimerTask fuelTask = new TimerTask() {

            @Override
            public void run() {

                fuel -= 5;


                if (fuel < 0) {

                    fuel = 0;
                }


                if (fuelListener != null) {

                    fuelListener.accept(
                            fuel
                    );
                }


                if (fuel <= 0) {

                    System.out.println(
                            "Fuel empty!"
                    );

                    fuelTimer.cancel();
                }
            }
        };


        fuelTimer.scheduleAtFixedRate(
                fuelTask,
                0,
                1000
        );
    }


    // ==========================================
    // TIMER OXIGEN
    // ==========================================

    private void startOxygenTimer() {

        oxygen = 30;


        oxygenTimer = new Timer();


        TimerTask oxygenTask = new TimerTask() {

            @Override
            public void run() {

                oxygen--;


                if (oxygen < 0) {

                    oxygen = 0;
                }


                if (oxygenListener != null) {

                    oxygenListener.accept(
                            oxygen
                    );
                }


                if (oxygen <= 0) {

                    oxygenTimer.cancel();


                    System.out.println(
                            "Oxygen depleted!"
                    );


                    if (oxygenEmptyListener != null) {

                        oxygenEmptyListener.run();
                    }
                }
            }
        };


        oxygenTimer.scheduleAtFixedRate(
                oxygenTask,
                1000,
                1000
        );
    }


    // ==========================================
    // TIMER SATELIT
    // ==========================================

    private void startSatelliteTimer() {

        eventTimer = new Timer();


        TimerTask satelliteTask = new TimerTask() {

            @Override
            public void run() {

                System.out.println(
                        "Satellite separation!"
                );


                if (satelliteEventListener != null) {

                    satelliteEventListener.run();
                }


                eventTimer.cancel();
            }
        };


        // Separarea satelitului
        // la 10 secunde după lansare

        eventTimer.schedule(
                satelliteTask,
                10000
        );
    }


    // ==========================================
    // STOP
    // ==========================================

    public void stop() {

        if (launchTimer != null) {

            launchTimer.cancel();
        }


        if (fuelTimer != null) {

            fuelTimer.cancel();
        }


        if (oxygenTimer != null) {

            oxygenTimer.cancel();
        }


        if (eventTimer != null) {

            eventTimer.cancel();
        }
    }
}