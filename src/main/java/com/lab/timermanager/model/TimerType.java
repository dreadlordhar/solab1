package com.lab.timermanager.model;

/**
 * Cele 3 moduri de planificare cerute in lucrarea de laborator:
 *  - DELAY         -> reactioneaza la un anumit interval de timp (o singura data, dupa un delay)
 *  - SPECIFIC_TIME -> reactioneaza la un anumit timp (data/ora exacta)
 *  - PERIODIC      -> reactioneaza cu o perioada indicata (executie repetata)
 */
public enum TimerType {
    DELAY("Delay"),
    SPECIFIC_TIME("Specific Time"),
    PERIODIC("Periodic");

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
