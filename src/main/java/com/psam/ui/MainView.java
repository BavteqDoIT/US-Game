package com.psam.ui;

import com.psam.game.GameService;
import com.psam.ui.utils.*;
import com.vaadin.flow.component.dependency.CssImport;
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

        ProjectPicker picker = new ProjectPicker();
        UIGrid grid = new UIGrid(game);
        UIButtons buttons = new UIButtons(
                game,
                grid::clearHighlights,
                () -> grid.highlightColumn(game.d2() - 1)
        );

        gameLayout.add(picker, buttons, grid);

        StartMenu startMenu = new StartMenu(null);

        startMenu.setStartAction(() -> {
            startMenu.setVisible(false);
            gameLayout.setVisible(true);
        });

        add(startMenu, gameLayout.getLayout());
    }
}