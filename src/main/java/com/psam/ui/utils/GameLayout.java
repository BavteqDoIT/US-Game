package com.psam.ui.utils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class GameLayout {

    private VerticalLayout gameLayout;

    public GameLayout() {
        gameLayout = new VerticalLayout();
        gameLayout.setVisible(false);
        gameLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        gameLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
    }

    public void add(Component... components) {
        gameLayout.add(components);
    }

    public VerticalLayout getLayout() {
        return gameLayout;
    }

    public void setVisible(boolean visible) {
        gameLayout.setVisible(visible);
    }
}
