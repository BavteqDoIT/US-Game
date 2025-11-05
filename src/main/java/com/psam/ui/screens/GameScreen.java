package com.psam.ui.screens;

import com.psam.game.GameService;
import com.psam.ui.utils.UIGrid;
import com.psam.ui.utils.UIPoints;
import com.psam.ui.utils.UILabelFactory;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@CssImport("./styles/styles.css")
@Route("main")
public class GameScreen extends VerticalLayout {

    @Autowired
    public GameScreen(GameService game) {
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        setSizeFull();

        UIGrid grid = new UIGrid(game);
        grid.refreshHighlights();
        UIPoints points = new UIPoints();
        game.setUIPoints(points);

        String[] ranges = {"3–4", "5–6", "7", "8–9", "10–11"};
        VerticalLayout columnLabels = UILabelFactory.createColumnLabels(ranges);

        HorizontalLayout boardLayout = new HorizontalLayout(columnLabels, grid);
        boardLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        boardLayout.setSpacing(true);

        Button rollButton = new Button("🎲 Rzuć kostkami", e -> {
            try {
                game.startRound();
                grid.clearHighlights();
                grid.highlightRoundColumns(game.isDouble(game.d1(), game.d2()));

                Notification.show("Wyniki: 🎲 " + game.d1() + " i 🎲 " + game.d2() +
                        " → aktywne kolumny: " + game.d1() + " i " + game.d2());

                if (game.isGameOver()) {
                    UI.getCurrent().navigate("end");
                }
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        add(rollButton, boardLayout, points);
    }
}
