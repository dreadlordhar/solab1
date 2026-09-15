package com.lab.timermanager.model;

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
