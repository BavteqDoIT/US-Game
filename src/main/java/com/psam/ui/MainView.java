package com.psam.ui;

import com.psam.game.GameService;
import com.psam.ui.screens.GameScreen;
import com.psam.ui.utils.UIGrid;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@CssImport("./styles/styles.css")
@Route("main")
public class MainView extends GameScreen {

    @Autowired
    public MainView(GameService game) {
        UIGrid grid = new UIGrid(game);

        Button rollButton = new Button("🎲 Rzuć kostkami", e -> {
            try {
                game.startRound();
                grid.clearHighlights();
                grid.highlightColumn(game.d1() - 1);

                Notification.show("Wyniki: 🎲 " + game.d1() + " i 🎲 " + game.d2() +
                        " → najpierw kolumna " + game.d1() + ", potem " + game.d2());

                if (game.isGameOver()) {
                    UI.getCurrent().navigate("end");
                }

            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        addComponents(rollButton, grid);
    }
}
