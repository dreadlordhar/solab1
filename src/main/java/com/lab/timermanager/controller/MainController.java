package com.lab.timermanager.controller;

import com.lab.timermanager.model.TimerModel;
import com.lab.timermanager.service.TimerService;
import com.lab.timermanager.view.NewTimerDialog;
import com.lab.timermanager.view.TimerCardView;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import java.util.Optional;

/**
 * Controller-ul ferestrei principale. Nu contine logica de planificare -
 * doar creeaza TimerModel-uri (prin dialog), le cere serviciului sa le porneasca/opreasca,
 * si construieste/sterge cardurile corespunzatoare.
 */
public class MainController {

    @FXML
    private FlowPane timerContainer;

    @FXML
    private Button newTimerButton;

    @FXML
    private Label emptyStateLabel;

    private final TimerService timerService = new TimerService();

    @FXML
    public void initialize() {
        updateEmptyState();
    }

    @FXML
    private void onNewTimer() {
        NewTimerDialog dialog = new NewTimerDialog(timerContainer.getChildren().size());
        Optional<TimerModel> result = dialog.showAndReturn();
        result.ifPresent(this::addTimerCard);
    }

    private void addTimerCard(TimerModel model) {
        TimerCardView card = new TimerCardView(model, timerService, () -> {
            timerContainer.getChildren().removeIf(node -> node.getUserData() == model);
            updateEmptyState();
        });
        card.setUserData(model);
        timerContainer.getChildren().add(card);
        updateEmptyState();

        // Pornim automat timerul imediat dupa creare.
        timerService.startTimer(model);
    }

    private void updateEmptyState() {
        boolean empty = timerContainer.getChildren().isEmpty();
        emptyStateLabel.setVisible(empty);
        emptyStateLabel.setManaged(empty);
    }

    /** Apelat de MainApp la inchiderea ferestrei, pentru a opri toate firele Timer. */
    public void shutdown() {
        timerService.shutdownAll();
    }
}
