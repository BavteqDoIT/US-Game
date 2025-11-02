package com.psam.ui.screens;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class GameScreen extends VerticalLayout {

    public GameScreen() {
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        setSizeFull();
    }

    public void addComponents(Component... components) {
        add(components);
    }
}
