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
        // TODO
    }

    public void stop() {
        // TODO
    }

    private List<Long> generateTriggerTimes(long gameDuration, int count) {
        // TODO
        return null;
    }

    private void showPopup() {
        // TODO
    }

    private BufferedImage loadImage(String path) {
        // TODO
        return null;
    }

    private void playSound(String path) {
        // TODO
    }
}
