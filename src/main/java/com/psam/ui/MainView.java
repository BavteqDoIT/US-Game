package com.psam.ui;

import com.psam.game.GameService;
import com.psam.ui.utils.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@CssImport("./styles/styles.css")
@Route("")
public class MainView extends VerticalLayout {

    @Autowired
    public MainView(GameService game) {
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        GameLayout gameLayout = new GameLayout();
        UIGrid grid = new UIGrid(game);

        // przycisk do rzutu kostkami
        Button rollButton = new Button("🎲 Rzuć kostkami", e -> {
            try {
                game.startRound(); // nowa metoda, która losuje kostki i resetuje etap
                grid.clearHighlights();
                grid.highlightColumn(game.d1() - 1);
                Notification.show("Wyniki: 🎲 " + game.d1() + " i 🎲 " + game.d2() +
                        " → najpierw kolumna " + game.d1() + ", potem " + game.d2());
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        gameLayout.add(rollButton, grid);

        StartMenu startMenu = new StartMenu(null);
        startMenu.setStartAction(() -> {
            startMenu.setVisible(false);
            gameLayout.setVisible(true);
        });

        add(startMenu, gameLayout.getLayout());
    }
}
