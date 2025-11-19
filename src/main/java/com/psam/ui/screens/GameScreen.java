package com.psam.ui.screens;

import com.psam.game.GameService;
import com.psam.ui.utils.MessageService;
import com.psam.ui.utils.UIGrid;
import com.psam.ui.utils.UIPoints;
import com.psam.ui.utils.UILabelFactory;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
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
        setSpacing(true);

        MessageService messageService = new MessageService();
        UIGrid grid = new UIGrid(game, messageService);
        grid.refreshHighlights();

        UIPoints points = new UIPoints();
        game.setUIPoints(points);

        String[] ranges = {"3–4", "5–6", "7", "8–9", "10–11"};
        VerticalLayout rowLabels = UILabelFactory.createColumnLabels(ranges);

        HorizontalLayout gridWithLabels = new HorizontalLayout(rowLabels, grid);
        gridWithLabels.setAlignItems(FlexComponent.Alignment.START);
        gridWithLabels.setSpacing(true);

        messageService.log("W fazie początkowej musisz postawić 2 dowolne budynki w dowolnym miejscu na planszy. Następnie rzuć kostkami");

        Button rollButton = new Button("🎲 Rzuć kostkami", e -> {
            try {
                game.startRound();
                grid.clearHighlights();
                grid.highlightRoundColumns(game.isDouble(game.d1(), game.d2()));
                if(game.isDouble(game.d1(), game.d2())) {
                    messageService.log("Wyrzuciłeś podwójnie 🎲 " + game.d1() +" → "+
                            "początkowo postaw " + game.rollToProject(game.d1()).name() + " w podświetlonej kolumnie, a następnie Plac w dowolnym dostępnym miejscu"
                    );
                } else if (game.isSame(game.rollToProject(game.d1()), game.rollToProject(game.d2()))) {
                    messageService.log("Wyrzuciłeś: 🎲 " + game.d1() + " i 🎲 " + game.d2() +" → "+
                            "początkowo postaw " + game.rollToProject(game.d1()).name() + " w jednej z podświetlonych kolumn, a następnie Fabrykę w drugiej"
                    );
                } else {
                    messageService.log("Wyrzuciłeś: 🎲 " + game.d1() + " i 🎲 " + game.d2() +
                            " → " +
                            "możesz postawić " + game.rollToProject(game.d1()).name() + " w kolumnie " + game.d2() +
                            " oraz " + game.rollToProject(game.d2()).name() + " w kolumnie " + game.d1());
                }

                if (game.isGameOver()) {
                    UI.getCurrent().navigate("end");
                }
            } catch (Exception ex) {
                messageService.log("Błąd: " + ex.getMessage());
            }
        });

        add(rollButton);
        add(gridWithLabels);
        add(points);
        add(messageService.getMessagePanel());

        setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, rollButton);
        setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, gridWithLabels);
        setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, points);
        setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, messageService.getMessagePanel());
    }
}
