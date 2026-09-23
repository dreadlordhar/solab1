package circlecatcher.distraction;

import javax.swing.*;

import javax.sound.sampled.*;

import java.awt.*;
import java.awt.event.*;

import java.io.InputStream;
import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Timer;

import circlecatcher.timers.Task;

public class DistractionManager {
    private final List<Timer> popupTimers = new ArrayList<>();
    private final List<JDialog> openPopups = new ArrayList<>();
    private final Random random = new Random();

    // File names for pop ups
    private final String[] memeNames = {
        "cat_laugh",
        "pterodactil",
        "nyan_cat",
        "nokia",
        "matrix",
    };

    public void start(long gameDuration) {
        if (random.nextDouble() < 0.10) return;

        int perMinute = 3 + random.nextInt(2);
        int total = (int) ((gameDuration / 1000.0) / 60.0 * perMinute);
        if (total < 1) total = 1;

        List<Long> triggers = generateTriggerTimes(gameDuration, total);

        for (long delay : triggers) {
            Timer t = new Timer();
            t.schedule(
                Task.set(() -> showPopup()),
                delay
            );
            popupTimers.add(t);
        }
    }

    public void stop() {
        for (Timer t : popupTimers) {
            t.cancel();
        }
        popupTimers.clear();

        for (JDialog p : openPopups) {
            p.dispose();
        }
        openPopups.clear();
    }

    private List<Long> generateTriggerTimes(long gameDuration, int count) {
        List<Long> times = new ArrayList<>();
        long minGap = 5000; // 5 seconds in ms

        for (int i = 0; i < count; i++) {
            int maxAttempts = 100;

            for (int attempt = 0; attempt < maxAttempts; attempt++) {
                long candidate =
                    (long) (random.nextDouble() * (gameDuration - 5000));

                if (candidate < 1000) {
                    candidate = 1000;
                }

                boolean tooClose = false;

                for (long existing : times) {
                    if (Math.abs(existing - candidate) < minGap) {
                        tooClose = true;
                        break;
                    }
                }

                if (!tooClose) {
                    times.add(candidate);
                    break;
                }
            }
        }

        Collections.sort(times);
        return times;
    }

    private void showPopup() {
        if (memeNames.length == 0) return;

        String name = memeNames[random.nextInt(memeNames.length)];

        ImageIcon icon = loadImageIcon("/resources/images/" + name + ".gif");

        if (icon == null) {
            icon = loadImageIcon("/resources/images/" + name + ".png");
        }

        if (icon == null) return;

        // popapinator 5000
        JDialog popup = new JDialog();
        popup.setUndecorated(true);
        popup.setModal(false);
        popup.setAlwaysOnTop(true);

        int popupSize = 500;
        int w = icon.getIconWidth();
        int h = icon.getIconHeight();

        if (
            w > popupSize ||
            h > popupSize
        )
            popup.setSize(popupSize, popupSize);
        else
            popup.setSize(w, h);


        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        int x = Math.max(
            0,
            random.nextInt(Math.max(1, screen.width - popupSize))
        );

        int y = Math.max(
            0,
            random.nextInt(Math.max(1, screen.height - popupSize))
        );

        popup.setLocation(x, y);

        JLabel label = new JLabel(icon);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);

        popup.add(label);

        openPopups.add(popup);

        SwingUtilities.invokeLater(() -> popup.setVisible(true));

        Clip currentClip =
            playSound("/resources/sounds/" + name + ".wav");

        int duration = 3000 + random.nextInt(2001);

        Timer autoClose = new Timer();

        autoClose.schedule(
            Task.set(() -> {
                SwingUtilities.invokeLater(() -> {
                    if (currentClip != null) {
                        currentClip.stop();
                        currentClip.close();
                    }

                    popup.dispose();
                    openPopups.remove(popup);
                });
            }),
            duration
        );

        popupTimers.add(autoClose);

        popup.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentClip != null) {
                    currentClip.stop();
                    currentClip.close();
                }

                popup.dispose();
                openPopups.remove(popup);
            }
        });

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentClip != null) {
                    currentClip.stop();
                    currentClip.close();
                }

                popup.dispose();
                openPopups.remove(popup);
            }
        });
    }

    private ImageIcon loadImageIcon(String path) {
        try {
            URL url = getClass().getResource(path);

            if (url == null) {
                return null;
            }

            return new ImageIcon(url);
        } catch (Exception e) {
            return null;
        }
    }

    private Clip playSound(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);

            if (is == null) {
                return null;
            }

            AudioInputStream audio =
                AudioSystem.getAudioInputStream(is);

            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.start();

            return clip;
        } catch (Exception e) {
            return null;
        }
    }
}
