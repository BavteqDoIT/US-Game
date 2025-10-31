package com.psam.ui.utils;

import com.psam.game.GameService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class UIButtons extends HorizontalLayout {

    private final Button rollBtn;
    private final Button scoreBtn;
    private final GameService game;
    private final Runnable clearHighlights;
    private final Runnable highlightColumn;

    public UIButtons(GameService game, Runnable clearHighlights, Runnable highlightColumn) {
        this.game = game;
        this.clearHighlights = clearHighlights;
        this.highlightColumn = highlightColumn;

        rollBtn = new Button("Rzuć kośćmi");
        scoreBtn = new Button("Zakończ rundę");

        configureRollButton();
        configureScoreButton();

        add(rollBtn, scoreBtn);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

    private void configureRollButton() {
        rollBtn.addClickListener(e -> {
            game.roll();
            Notification.show("Wyrzucono: " + game.d1() + " i " + game.d2() +
                    " (kolumna " + game.d2() + ")");
            highlightColumn.run();
            rollBtn.setEnabled(false);
            scoreBtn.setEnabled(true);
        });
    }

    private void configureScoreButton() {
        scoreBtn.setEnabled(false);
        scoreBtn.addClickListener(e -> {
            if (game.d2() == 0) {
                Notification.show("Najpierw rzuć kośćmi.");
                return;
            }
            int pts = game.scoreRound();
            Notification.show("Punkty w tej rundzie: " + pts);
            clearHighlights.run();
            scoreBtn.setEnabled(false);
            rollBtn.setEnabled(true);
        });
    }
}
