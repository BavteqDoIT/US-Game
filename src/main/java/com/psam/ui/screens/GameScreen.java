package com.psam.ui.screens;

import com.psam.game.GameService;
import com.psam.ui.utils.UIGrid;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
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

        VerticalLayout columnLabels = new VerticalLayout();
        columnLabels.setSpacing(true);
        columnLabels.setPadding(false);
        columnLabels.setAlignItems(FlexComponent.Alignment.END);

        String[] ranges = {"3–4", "5–6", "7", "8–9", "10–11"};
        for (String range : ranges) {
            Div label = new Div();
            label.add(new Span("🎲 " + range));
            label.getStyle().set("font-size", "16px");
            label.getStyle().set("font-weight", "bold");
            label.getStyle().set("height", "50px");
            label.getStyle().set("display", "flex");
            label.getStyle().set("align-items", "center");
            label.getStyle().set("justify-content", "flex-end");
            label.getStyle().set("padding-right", "8px");
            columnLabels.add(label);
        }

        HorizontalLayout boardLayout = new HorizontalLayout(columnLabels, grid);
        boardLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        boardLayout.setSpacing(true);

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

        add(rollButton, boardLayout);
    }
}
