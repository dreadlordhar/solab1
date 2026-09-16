package com.lab.timermanager.view;

import com.lab.timermanager.model.TimerModel;
import com.lab.timermanager.model.TimerType;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Dialog modal pentru crearea unui timer nou. Utilizatorul alege tipul
 * (Delay / Specific Time / Periodic), iar formularul afiseaza doar campurile
 * relevante pentru tipul respectiv.
 */
public class NewTimerDialog extends Dialog<TimerModel> {

    public NewTimerDialog(int existingCount) {
        setTitle("New Timer");
        setHeaderText("Configureaza un timer nou");
        getDialogPane().getStyleClass().add("new-timer-dialog");
        getDialogPane().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm());

        // Fix pentru bug: fereastra dialogului nu se redimensiona cind formularul
        // devenea mai inalt (ex. tipul Specific Time), asa ca butonul "Create" iesea
        // in afara zonei vizibile. O facem redimensionabila manual...
        setResizable(true);

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField("Timer " + (existingCount + 1));

        ComboBox<TimerType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(TimerType.values());
        typeCombo.setValue(TimerType.DELAY);
        typeCombo.setMaxWidth(Double.MAX_VALUE);

        // Campuri specifice tipului DELAY
        Spinner<Integer> delaySpinner = new Spinner<>(1, 24 * 3600, 10);
        delaySpinner.setEditable(true);

        // Campuri specifice tipului PERIODIC
        Spinner<Integer> periodSpinner = new Spinner<>(1, 24 * 3600, 5);
        periodSpinner.setEditable(true);

        // Campuri specifice tipului SPECIFIC_TIME
        DatePicker datePicker = new DatePicker(LocalDate.now());
        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, LocalTime.now().getHour());
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, (LocalTime.now().getMinute() + 1) % 60);
        hourSpinner.setEditable(true);
        minuteSpinner.setEditable(true);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 10, 10, 10));

        grid.add(new Label("Nume"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Tip"), 0, 1);
        grid.add(typeCombo, 1, 1);

        VBox dynamicFields = new VBox(10);
        grid.add(dynamicFields, 0, 2, 2, 1);

        Runnable refreshFields = () -> {
            dynamicFields.getChildren().clear();
            switch (typeCombo.getValue()) {
                case DELAY -> {
                    dynamicFields.getChildren().add(new Label("Executa dupa (secunde)"));
                    dynamicFields.getChildren().add(delaySpinner);
                }
                case PERIODIC -> {
                    dynamicFields.getChildren().add(new Label("Repeta la fiecare (secunde)"));
                    dynamicFields.getChildren().add(periodSpinner);
                }
                case SPECIFIC_TIME -> {
                    dynamicFields.getChildren().add(new Label("Data"));
                    dynamicFields.getChildren().add(datePicker);
                    dynamicFields.getChildren().add(new Label("Ora : Minut"));
                    dynamicFields.getChildren().add(new javafx.scene.layout.HBox(8, hourSpinner, minuteSpinner));
                }
            }
        };
        // ...si, mai important, punem formularul intr-un ScrollPane cu inaltime
        // limitata: chiar daca un tip de timer are mai multe cimpuri, continutul
        // devine scrollabil in loc sa impinga butonul "Create" in afara ferestrei.
        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(260);
        scrollPane.setStyle("-fx-background-color: transparent;");
        GridPane.setHgrow(grid, Priority.ALWAYS);

        Runnable resizeToContent = () -> Platform.runLater(() -> {
            Window window = getDialogPane().getScene() != null ? getDialogPane().getScene().getWindow() : null;
            if (window != null) {
                window.sizeToScene();
            }
        });

        typeCombo.valueProperty().addListener((obs, oldV, newV) -> {
            refreshFields.run();
            resizeToContent.run();
        });
        refreshFields.run();

        getDialogPane().setContent(scrollPane);
        getDialogPane().setPrefWidth(420);
        resizeToContent.run();

        setResultConverter(buttonType -> {
            if (buttonType != createButtonType) {
                return null;
            }
            String name = nameField.getText().isBlank()
                    ? "Timer " + (existingCount + 1)
                    : nameField.getText().trim();
            TimerType type = typeCombo.getValue();
            TimerModel model = new TimerModel(name, type);

            switch (type) {
                case DELAY -> model.setDelaySeconds(delaySpinner.getValue());
                case PERIODIC -> model.setPeriodSeconds(periodSpinner.getValue());
                case SPECIFIC_TIME -> model.setSpecificDateTime(
                        LocalDateTime.of(datePicker.getValue(),
                                LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue())));
            }
            return model;
        });
    }

    public Optional<TimerModel> showAndReturn() {
        return showAndWait();
    }
}
