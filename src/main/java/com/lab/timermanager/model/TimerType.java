package com.lab.timermanager.model;

/**
 * Tipul de planificare implementat de aceasta parte a temei (lucru in pereche):
 *  - DELAY -> reactioneaza la un anumit interval de timp (o singura data, dupa un delay)
 * <p>
 * Celelalte doua tipuri (Specific Time, Periodic) sunt implementate de coleg,
 * in cealalta jumatate a proiectului.
 */
public enum TimerType {
    DELAY("Delay");

    private final String displayName;

    TimerType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
