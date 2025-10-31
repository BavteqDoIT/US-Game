package com.psam.ui.utils;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class StartMenu extends VerticalLayout {

    private Button startButton;

    public StartMenu(Runnable ignored) {
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        H3 title = new H3("City Roller — MVP");

        startButton = new Button("Start");
        startButton.getStyle().set("font-size", "24px");
        startButton.getStyle().set("padding", "20px 40px");

        add(title, startButton);
    }

    public void setStartAction(Runnable action) {
        startButton.addClickListener(e -> action.run());
    }
}
