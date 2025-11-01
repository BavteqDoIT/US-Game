package com.psam.ui.utils;

import com.psam.game.GameService;
import com.psam.game.Project;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;

public class UIGrid extends FlexLayout {

    private final int size;
    private final Div[][] cells;
    private final GameService game;

    public UIGrid(GameService game) {
        this.game = game;
        this.size = game.board().getSize();
        this.cells = new Div[size][size];

        getStyle().set("display", "grid");
        getStyle().set("grid-template-columns", "repeat(6, 56px)");
        getStyle().set("gap", "4px");

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Div cell = new Div();
                cell.setWidth("56px");
                cell.setHeight("56px");
                cell.addClassName("noHighlight");
                cell.getStyle().set("display", "flex");
                cell.getStyle().set("align-items", "center");
                cell.getStyle().set("justify-content", "center");

                final int rr = r;

                cell.addClickListener(e -> handleCellClick(rr, cell));
                add(cell);
                cells[r][c] = cell;
            }
        }
    }

    private void handleCellClick(int row, Div cell) {
        try {
            if (!game.isRoundActive()) {
                Notification.show("Najpierw rzuć kostkami!");
                return;
            }

            if (cell.getComponentCount() > 0) {
                Notification.show("To pole jest już zajęte!");
                return;
            }

            var result = game.place(row);
            addEmoji(result.row(), result.col(), result.project());

            clearHighlights();

            if (!result.roundEnded() && game.getTurnStage() == 2) {
                highlightColumn(game.d2() - 1);
            }

            if (result.roundEnded()) {
                Notification.show("Runda zakończona! Możesz rzucić kostkami ponownie.");
            }

            Notification.show(result.message());

        } catch (Exception ex) {
            Notification.show(ex.getMessage());
        }
    }

    private void addEmoji(int row, int col, Project p) {
        Span emoji = new Span(getEmoji(p));
        emoji.getStyle().set("font-size", "50px");
        cells[row][col].add(emoji);
    }

    private String getEmoji(Project p) {
        return switch (p) {
            case DOM -> "\uD83C\uDFE0";      // 🏠
            case LAS -> "\uD83C\uDF33";      // 🌳
            case JEZIORO -> "\uD83C\uDF0A";  // 🌊
            case FABRYKA -> "\u2699\uFE0F";  // ⚙️
            case PLAC -> "\uD83D\uDDFD";     // 🗽
        };
    }

    public void highlightColumn(int col) {
        for (int r = 0; r < size; r++) {
            cells[r][col].removeClassName("noHighlight");
            cells[r][col].addClassName("highlighted");
        }
    }

    public void clearHighlights() {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                cells[r][c].removeClassName("highlighted");
                cells[r][c].addClassName("noHighlight");
            }
        }
    }
}
