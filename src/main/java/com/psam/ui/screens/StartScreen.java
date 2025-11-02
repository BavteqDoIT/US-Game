package com.psam.ui.screens;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("")
public class StartScreen extends VerticalLayout {

    public StartScreen() {
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        H1 title = new H1("US-Game");

        Button startButton = new Button("Start", e -> UI.getCurrent().navigate(GameScreen.class));
        startButton.getStyle().set("font-size", "24px");
        startButton.getStyle().set("padding", "20px 40px");

        add(title, startButton);
    }
}
