package com.psam.ui.utils;

import com.psam.game.GameService;
import com.psam.game.Project;
import com.psam.ui.screens.EndScreen;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;

import java.util.HashSet;
import java.util.Set;

public class UIGrid extends FlexLayout {

    private final int rows = 5;
    private final int cols = 6;
    private final Div[][] cells = new Div[rows][cols];
    private final GameService game;
    private final Set<Integer> activeColumns = new HashSet<>();
    private boolean waitingForSecondPlacement = false;
    private int initialPlacements;

    public UIGrid(GameService game) {
        this.game = game;
        initialPlacements = 0;

        getStyle().set("display", "grid");
        getStyle().set("grid-template-columns", "repeat(" + cols + ", 56px)");
        getStyle().set("gap", "4px");

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Div cell = new Div();
                cell.setWidth("56px");
                cell.setHeight("56px");
                cell.addClassName("noHighlight");
                cell.getStyle().set("display", "flex");
                cell.getStyle().set("align-items", "center");
                cell.getStyle().set("justify-content", "center");
                cell.getStyle().set("position", "relative");

                int cellPoints = game.board().getPoints(row, col);
                if (cellPoints > 0) {
                    Span pointLabel = new Span(String.valueOf(cellPoints));
                    pointLabel.getStyle().set("position", "absolute");
                    pointLabel.getStyle().set("bottom", "2px");
                    pointLabel.getStyle().set("right", "4px");
                    pointLabel.getStyle().set("font-size", "12px");
                    pointLabel.getStyle().set("color", "gray");
                    cell.add(pointLabel);
                }

                final int lambdaRow = row;
                final int lambdaCol = col;
                cell.addClickListener(e -> handleCellClick(lambdaRow, lambdaCol, cell));
                add(cell);
                cells[row][col] = cell;
            }
        }

        if (game.shouldHighlightAllColumns()) {
            highlightAll();
            Notification.show("Faza początkowa — wybierz 2 dowolne miejsca na budynki.");
        } else {
            Notification.show("Najpierw rzuć kostkami!");
        }
    }

    private void handleCellClick(int row, int col, Div cell) {
        try {
            if (game.getRoundCount() >= 9) {
                UI.getCurrent().navigate(EndScreen.class);
                return;
            }

            if (game.isSetupPhase()){
                if (cell.getComponentCount() > 1) {
                    Notification.show("To pole jest już zajęte!");
                    return;
                }

                showProjectSelectionDialog(row, col);
                return;
            }

            if (!game.isRoundActive() && game.getRoundCount() != 0) {
                Notification.show("Najpierw rzuć kostkami!");
                return;
            }

            if (!activeColumns.contains(col)) {
                Notification.show("Wybierz pole w jednej z podświetlonych kolumn!");
                return;
            }

            if (cell.getComponentCount() > 1) {
                Notification.show("To pole jest już zajęte!");
                return;
            }

            var result = game.place(row, col);
            addEmoji(result.row(), result.col(), result.project());
            Notification.show(result.message());

            if (game.shouldHighlightAllColumns()) {
                highlightAll();
                waitingForSecondPlacement = true;
                return;
            }

            if (waitingForSecondPlacement) {
                waitingForSecondPlacement = false;
                clearHighlights();
                Notification.show("Runda zakończona po dublu! Możesz rzucić kostkami ponownie.");
                if (game.getRoundCount() >= 9) {
                    UI.getCurrent().navigate(EndScreen.class);
                }
                return;
            }

            deactivateColumn(col);

            if (result.roundEnded()) {
                clearHighlights();
                Notification.show("Runda zakończona! Możesz rzucić kostkami ponownie.");
                if (game.getRoundCount() >= 9) {
                    UI.getCurrent().navigate(EndScreen.class);
                }
            }

        } catch (Exception ex) {
            Notification.show(ex.getMessage());
        }
    }

    private void showProjectSelectionDialog(int row, int col) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Wybierz projekt:");

        Button domBtn = new Button("\uD83C\uDFE0 Dom", e -> selectProject(dialog, row, col, Project.DOM));
        Button lasBtn = new Button("\uD83C\uDF33 Las", e -> selectProject(dialog, row, col, Project.LAS));
        Button fabrykaBtn = new Button("\uD83C\uDF0A Jezioro", e -> selectProject(dialog, row, col, Project.FABRYKA));

        FlexLayout layout = new FlexLayout(domBtn, lasBtn, fabrykaBtn);
        layout.getStyle().set("gap", "10px");
        layout.getStyle().set("justify-content", "center");

        dialog.add(layout);
        dialog.open();
    }

    private void selectProject(Dialog dialog, int row, int col, Project project) {
        dialog.close();
        var result = game.place(row, col, project);
        addEmoji(result.row(), result.col(), project);
        Notification.show("Postawiono: " + project.name());
        initialPlacements++;

        if (initialPlacements >= 2) {
            clearHighlights();
            Notification.show("Faza początkowa zakończona! Możesz rzucić kostkami.");
            game.endInitialPhase();
        }
    }

    private void addEmoji(int row, int col, Project project) {
        Span emoji = new Span(getEmoji(project));
        emoji.getStyle().set("font-size", "40px");
        cells[row][col].add(emoji);
    }

    private String getEmoji(Project project) {
        return switch (project) {
            case DOM -> "\uD83C\uDFE0";      // 🏠
            case LAS -> "\uD83C\uDF33";      // 🌳
            case JEZIORO -> "\uD83C\uDF0A";  // 🌊
            case FABRYKA -> "⚙️";
            case PLAC -> "\uD83D\uDDFD";     // 🗽
        };
    }

    public void highlightRoundColumns(boolean isDouble) {
        clearHighlights();
        activeColumns.clear();
        if (!isDouble) {
            int col1 = game.d1() - 1;
            int col2 = game.d2() - 1;
            highlightColumn(col1);
            highlightColumn(col2);
        } else {
            int col1 = game.d1() - 1;
            highlightColumn(col1);
        }
    }

    private void highlightColumn(int col) {
        activeColumns.add(col);
        for (int row = 0; row < rows; row++) {
            cells[row][col].removeClassName("noHighlight");
            cells[row][col].addClassName("highlighted");
        }
    }

    public void highlightAll() {
        for (int col = 0; col < cols; col++) {
            highlightColumn(col);
        }
    }

    private void deactivateColumn(int col) {
        activeColumns.remove(col);
        for (int row = 0; row < rows; row++) {
            cells[row][col].removeClassName("highlighted");
            cells[row][col].addClassName("noHighlight");
        }
    }

    public void clearHighlights() {
        activeColumns.clear();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                cells[row][col].removeClassName("highlighted");
                cells[row][col].addClassName("noHighlight");
            }
        }
    }

    public void refreshHighlights() {
        clearHighlights();
        if (game.isSetupPhase()) {
            highlightAll(); // jeśli początkowa faza
        } else if (game.isRoundActive()) {
            highlightRoundColumns(game.isDouble(game.d1(), game.d2()));
        }
    }

}
