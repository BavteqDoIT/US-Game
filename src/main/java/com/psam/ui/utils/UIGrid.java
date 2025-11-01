package com.psam.ui.utils;

import com.psam.game.GameService;
import com.psam.game.Project;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;

import java.util.Random;

public class UIGrid extends FlexLayout {

    private final int size;
    private final Div[][] cells;
    private final Random random = new Random();

    public UIGrid(GameService game) {
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

                final int rr = r, cc = c;

                cell.addClickListener(e -> {
                    try {
                        if (cell.getComponentCount() > 0) {
                            Notification.show("To pole jest już zajęte!");
                            return;
                        }

                        // losowanie 1–6
                        int roll = random.nextInt(6) + 1;
                        Project rolledProject = rollToProject(roll);

                        // umieszczamy projekt w grze
                        game.place(rr, cc, rolledProject);

                        // pokazujemy emoji
                        Span emoji = new Span(getEmoji(rolledProject));
                        emoji.getStyle().set("font-size", "56px");
                        cell.add(emoji);

                        Notification.show("Wypadło: " + roll + " → " + rolledProject);

                    } catch (Exception ex) {
                        Notification.show(ex.getMessage());
                    }
                });

                add(cell);
                cells[r][c] = cell;
            }
        }
    }

    private Project rollToProject(int roll) {
        return switch (roll) {
            case 1, 2 -> Project.DOM;
            case 3 -> Project.LAS;
            case 4 -> Project.JEZIORO;
            case 5 -> Project.FABRYKA;
            case 6 -> Project.PLAC;
            default -> throw new IllegalArgumentException("Nieprawidłowy wynik rzutu kostką: " + roll);
        };
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
