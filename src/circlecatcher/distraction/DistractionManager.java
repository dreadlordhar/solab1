package circlecatcher.distraction;

import javax.swing.*;

import javax.sound.sampled.*;
import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import java.io.InputStream;
import java.util.*;
import circlecatcher.timers.Task;

public class DistractionManager {
    private final List<Timer> popupTimers = new ArrayList<>();
    private final List<JDialog> openPopups = new ArrayList<>();
    private final Random random = new Random();

    // File names for pop ups
    private final String[] memeNames = {
        // "troll",
        // "bruh",
        // "sus"
    };

    public void start(long gameDuration) {
        if (random.nextDouble() < 0.10) return; // if activated, no pop ups

        int perMinute = 3 + random.nextInt(2); // 3 or 4 pop ups per minute
        int total = (int) ((gameDuration / 1000.0) / 60.0 * perMinute);
        if (total < 1) total = 1;

        // generate triggers
        List<Long> triggers = generateTriggerTimes(gameDuration, total);

        // timer scheduling
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
        // TODO
    }

    private List<Long> generateTriggerTimes(long gameDuration, int count) {
        List<Long> times = new ArrayList<>();
        long minGap = 5000; // 5 seconds in ms

        for (int i = 0; i < count; i++) {
            int maxAttempts = 100;
            for (int attempt = 0; attempt < maxAttempts; attempt++) {
                long candidate = (long) (random.nextDouble() * (gameDuration - 5000));
                if (candidate < 1000) candidate = 1000;

                boolean tooClose = false;
                for (long existing : times) {
                    if (Math.abs(existing - candidate) < minGap) {
                        tooClose = true;
                        break;
                    }
                }

                if (!tooClose) {
                    times.add(candidate);
                    break; // move to next distraction
                }
            }
        }

        Collections.sort(times);
        return times;
    }

    private void showPopup() {
        String name = memeNames[random.nextInt(memeNames.length)];
        BufferedImage img = loadImage("/resources/images/" + name + ".png");
        if (img == null) return;

        // popapinator 5000
        JDialog popup = new JDialog();
        popup.setUndecorated(true);  // no title bar
        popup.setModal(false);       // user can still click circles behind it
        popup.setAlwaysOnTop(true);  // stays on top of the game
        popup.setSize(500, 500);

        // position randomiser
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = random.nextInt(screen.width - 500);
        int y = random.nextInt(screen.height - 500);
        popup.setLocation(x, y);

        // display the image
        JLabel label = new JLabel(new ImageIcon(img));
        popup.add(label);

        // track it
        openPopups.add(popup);

        // show it
        SwingUtilities.invokeLater(() -> popup.setVisible(true));

        // auto-close after 3-5 seconds
        int duration = 3000 + random.nextInt(2001);
        Timer autoClose = new Timer();
        autoClose.schedule(Task.set(() -> {
            SwingUtilities.invokeLater(() -> {
                popup.dispose();
                openPopups.remove(popup);
            });
        }), duration);
        popupTimers.add(autoClose);

        // click to close + low chance meme sound
        popup.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (random.nextDouble() < 0.15) {
                    playSound("/resources/sounds/" + name + ".wav");
                }
                popup.dispose();
                openPopups.remove(popup);
            }
        });
    }

    private BufferedImage loadImage(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) return null;
            return ImageIO.read(is);
        } catch (Exception e) {
            return null;
        }
    }

    private void playSound(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) return;
            AudioInputStream audio = AudioSystem.getAudioInputStream(is);
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.start();
        } catch (Exception e) {
            // silently fail if sound missing
        }
    }
}
