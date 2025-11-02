package com.psam.ui.screens;

import com.psam.game.GameService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("end")
public class EndScreen extends VerticalLayout {


    @Autowired
    public EndScreen(GameService gameService) {

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        H1 title = new H1("US-Game");

        Button restart = new Button("Zagraj ponownie", e -> {
            gameService.resetGame();
            UI.getCurrent().navigate(StartScreen.class);
        });

        add(title, restart);
    }
}
