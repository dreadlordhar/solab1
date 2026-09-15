package com.lab.timermanager.view;

import com.lab.timermanager.model.TimerModel;
import com.lab.timermanager.model.TimerType;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Objects;
import java.util.Optional;

/**
 * Dialog modal pentru crearea unui timer nou de tip DELAY: utilizatorul
 * alege doar numele si dupa cate secunde sa se execute.
 */
public class NewTimerDialog extends Dialog<TimerModel> {

    public NewTimerDialog(int existingCount) {
        setTitle("New Timer");
        setHeaderText("Configureaza un timer nou (Delay)");
        getDialogPane().getStyleClass().add("new-timer-dialog");
        getDialogPane().getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());

        setResizable(true);

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField("Timer " + (existingCount + 1));

        Spinner<Integer> delaySpinner = new Spinner<>(1, 24 * 3600, 10);
        delaySpinner.setEditable(true);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 10, 10, 10));

        grid.add(new Label("Nume"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Executa dupa (secunde)"), 0, 1);
        grid.add(delaySpinner, 1, 1);

        getDialogPane().setContent(grid);
        getDialogPane().setPrefWidth(420);

        setResultConverter(buttonType -> {
            if (buttonType != createButtonType) {
                return null;
            }
            String name = nameField.getText().isBlank()
                    ? "Timer " + (existingCount + 1)
                    : nameField.getText().trim();
            TimerModel model = new TimerModel(name, TimerType.DELAY);
            model.setDelaySeconds(delaySpinner.getValue());
            return model;
        });
    }

    public Optional<TimerModel> showAndReturn() {
        return showAndWait();
    }
}
