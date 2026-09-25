package space;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class FuelTimer {

    private int fuel = 100;

    private Timer fuelTimer;
    private Timer eventTimer;

    private Consumer<Integer> fuelListener;
    private Runnable eventListener;

    public FuelTimer(
            Consumer<Integer> fuelListener,
            Runnable eventListener
    ) {
        this.fuelListener = fuelListener;
        this.eventListener = eventListener;
    }

    public void start() {

        fuelTimer = new Timer();

        TimerTask fuelTask = new TimerTask() {

            @Override
            public void run() {

                fuel -= 5;

                if (fuelListener != null) {
                    fuelListener.accept(fuel);
                }

                if (fuel <= 0) {
                    System.out.println("Fuel empty!");
                    fuelTimer.cancel();
                }
            }
        };

        fuelTimer.scheduleAtFixedRate(
                fuelTask,
                0,
                1000
        );


        // Space event timer
        eventTimer = new Timer();

        Date eventTime =
                new Date(System.currentTimeMillis() + 10000);

        TimerTask spaceEventTask = new TimerTask() {

            @Override
            public void run() {

                System.out.println(
                        "Satellite separation!"
                );

                if (eventListener != null) {
                    eventListener.run();
                }

                eventTimer.cancel();
            }
        };

        eventTimer.schedule(
                spaceEventTask,
                eventTime
        );
    }

    public void stop() {

        if (fuelTimer != null) {
            fuelTimer.cancel();
        }

        if (eventTimer != null) {
            eventTimer.cancel();
        }
    }
}