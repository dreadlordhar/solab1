package com.lab.timermanager.model;

import javafx.beans.property.*;

import java.util.UUID;

/**
 * Modelul unui timer creat de utilizator din interfata grafica.
 * Nu contine nicio logica de planificare (asta e in service.TimerService) -
 * este doar starea afisata pe card si legata (bind) la UI.
 * <p>
 * Aceasta versiune contine doar tipul DELAY (reactioneaza o singura data,
 * dupa un interval de timp indicat de utilizator).
 */
public class TimerModel {

    private final String id = UUID.randomUUID().toString();

    private final StringProperty name = new SimpleStringProperty();
    private final ObjectProperty<TimerType> type = new SimpleObjectProperty<>();
    private final ObjectProperty<TimerStatus> status = new SimpleObjectProperty<>(TimerStatus.STOPPED);

    // Parametru de planificare pentru DELAY
    private final IntegerProperty delaySeconds = new SimpleIntegerProperty();

    // Informatie afisata pe card: timpul ramas pana la executie
    private final StringProperty nextExecutionText = new SimpleStringProperty("-");

    public TimerModel(String name, TimerType type) {
        this.name.set(name);
        this.type.set(type);
    }

    public String getId() {
        return id;
    }

    // --- name ---
    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    // --- type ---
    public TimerType getType() {
        return type.get();
    }

    public ObjectProperty<TimerType> typeProperty() {
        return type;
    }

    // --- status ---
    public TimerStatus getStatus() {
        return status.get();
    }

    public void setStatus(TimerStatus status) {
        this.status.set(status);
    }

    public ObjectProperty<TimerStatus> statusProperty() {
        return status;
    }

    // --- delaySeconds ---
    public int getDelaySeconds() {
        return delaySeconds.get();
    }

    public void setDelaySeconds(int value) {
        delaySeconds.set(value);
    }

    public IntegerProperty delaySecondsProperty() {
        return delaySeconds;
    }

    // --- nextExecutionText ---
    public String getNextExecutionText() {
        return nextExecutionText.get();
    }

    public void setNextExecutionText(String value) {
        nextExecutionText.set(value);
    }

    public StringProperty nextExecutionTextProperty() {
        return nextExecutionText;
    }
}
