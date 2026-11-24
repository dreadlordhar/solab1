package com.lab.timermanager.model;

/** Statusul curent al unui timer, afisat pe card. */
public enum TimerStatus {
    RUNNING("Running"),
    COMPLETED("Completed"),
    STOPPED("Stopped");

    private final String displayName;

    TimerStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
