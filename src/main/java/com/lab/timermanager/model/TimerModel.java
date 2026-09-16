package com.lab.timermanager.model;

import javafx.beans.property.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modelul unui timer creat de utilizator din interfata grafica.
 * Nu contine nicio logica de planificare (asta e in service.TimerService) -
 * este doar starea afisata pe card si legata (bind) la UI.
 */
public class TimerModel {

    private final String id = UUID.randomUUID().toString();

    private final StringProperty name = new SimpleStringProperty();
    private final ObjectProperty<TimerType> type = new SimpleObjectProperty<>();
    private final ObjectProperty<TimerStatus> status = new SimpleObjectProperty<>(TimerStatus.STOPPED);

    // Parametri de planificare, in functie de tip
    private final IntegerProperty delaySeconds = new SimpleIntegerProperty();   // DELAY
    private final IntegerProperty periodSeconds = new SimpleIntegerProperty();  // PERIODIC
    private final ObjectProperty<LocalDateTime> specificDateTime = new SimpleObjectProperty<>(); // SPECIFIC_TIME

    // Informatie afisata pe card: timpul ramas / urmatoarea executie
    private final StringProperty nextExecutionText = new SimpleStringProperty("-");
    // Cate execuții a avut deja (relevant pentru PERIODIC)
    private final IntegerProperty executionCount = new SimpleIntegerProperty(0);

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

    // --- periodSeconds ---
    public int getPeriodSeconds() {
        return periodSeconds.get();
    }

    public void setPeriodSeconds(int value) {
        periodSeconds.set(value);
    }

    public IntegerProperty periodSecondsProperty() {
        return periodSeconds;
    }

    // --- specificDateTime ---
    public LocalDateTime getSpecificDateTime() {
        return specificDateTime.get();
    }

    public void setSpecificDateTime(LocalDateTime value) {
        specificDateTime.set(value);
    }

    public ObjectProperty<LocalDateTime> specificDateTimeProperty() {
        return specificDateTime;
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

    // --- executionCount ---
    public int getExecutionCount() {
        return executionCount.get();
    }

    public void setExecutionCount(int value) {
        executionCount.set(value);
    }

    public void incrementExecutionCount() {
        executionCount.set(executionCount.get() + 1);
    }

    public IntegerProperty executionCountProperty() {
        return executionCount;
    }
}
