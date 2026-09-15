package com.lab.timermanager.view;

import com.lab.timermanager.model.TimerModel;
import com.lab.timermanager.model.TimerStatus;
import com.lab.timermanager.service.TimerService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Card vizual pentru un singur timer. Toata logica de start/stop/delete este
 * delegata catre TimerService - cardul doar afiseaza starea (bind la proprietatile modelului)
 * si reactioneaza la click-uri pe butoane.
 * <p>
 * Aceasta versiune afiseaza doar timere de tip DELAY.
 */
public class TimerCardView extends VBox {

    private final Label statusBadge = new Label();

    public TimerCardView(TimerModel model, TimerService service, Runnable onDeleted) {
        getStyleClass().add("timer-card");
        setPrefWidth(300);
        setSpacing(10);

        // --- Header: nume + tip ---
        Label nameLabel = new Label();
        nameLabel.textProperty().bind(model.nameProperty());
        nameLabel.getStyleClass().add("card-title");

        Label typeLabel = new Label(model.getType().getDisplayName());
        typeLabel.getStyleClass().addAll("type-badge", "badge-delay");

        HBox header = new HBox(10, nameLabel, spacer(), typeLabel);
        header.setAlignment(Pos.CENTER_LEFT);

        // --- Detalii de planificare (fixe, setate la creare) ---
        Label detailsLabel = new Label(
                "Se executa o singura data, dupa " + model.getDelaySeconds() + " secunde.");
        detailsLabel.getStyleClass().add("card-details");
        detailsLabel.setWrapText(true);

        // --- Timp ramas (actualizat live de TimerService) ---
        Label nextExecCaption = new Label("Timp ramas");
        nextExecCaption.getStyleClass().add("field-caption");

        Label nextExecValue = new Label();
        nextExecValue.textProperty().bind(model.nextExecutionTextProperty());
        nextExecValue.getStyleClass().add("countdown-value");

        VBox countdownBox = new VBox(2, nextExecCaption, nextExecValue);

        // --- Status ---
        statusBadge.getStyleClass().add("status-badge");
        updateStatusStyle(model.getStatus());
        model.statusProperty().addListener((obs, oldVal, newVal) -> updateStatusStyle(newVal));

        // --- Butoane ---
        Button startButton = new Button("Start");
        startButton.getStyleClass().addAll("action-button", "start-button");
        startButton.setOnAction(e -> service.startTimer(model));

        Button stopButton = new Button("Stop");
        stopButton.getStyleClass().addAll("action-button", "stop-button");
        stopButton.setOnAction(e -> service.stopTimer(model));

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().addAll("action-button", "delete-button");
        deleteButton.setOnAction(e -> {
            service.deleteTimer(model);
            if (onDeleted != null) {
                onDeleted.run();
            }
        });

        // Disable Start when already running
        startButton.disableProperty().bind(model.statusProperty().isEqualTo(TimerStatus.RUNNING));
        stopButton.disableProperty().bind(model.statusProperty().isNotEqualTo(TimerStatus.RUNNING));

        HBox buttonRow = new HBox(8, startButton, stopButton, deleteButton);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        HBox statusRow = new HBox(statusBadge);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        setPadding(new Insets(18));
        getChildren().addAll(header, detailsLabel, countdownBox, statusRow, buttonRow);
    }

    private Region spacer() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    private void updateStatusStyle(TimerStatus status) {
        statusBadge.getStyleClass().removeAll("status-running", "status-completed", "status-stopped");
        statusBadge.setText(status.getDisplayName());
        switch (status) {
            case RUNNING -> statusBadge.getStyleClass().add("status-running");
            case COMPLETED -> statusBadge.getStyleClass().add("status-completed");
            case STOPPED -> statusBadge.getStyleClass().add("status-stopped");
        }
    }
}
