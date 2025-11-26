package com.psam.ui.utils;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

public class UIPoints extends Div {

    private final Div[] roundCells = new Div[9];

    public UIPoints() {
        getStyle().set("display", "grid");
        getStyle().set("grid-template-columns", "repeat(9, 56px)");
        getStyle().set("gap", "4px");

        for (int i = 0; i < 9; i++) {
            Div roundCell = new Div();
            roundCell.setWidth("56px");
            roundCell.setHeight("56px");
            roundCell.getStyle().set("border", "1px solid #aaa");
            roundCell.getStyle().set("border-radius", "4px");
            roundCell.getStyle().set("position", "relative");
            roundCell.getStyle().set("display", "flex");
            roundCell.getStyle().set("align-items", "center");
            roundCell.getStyle().set("justify-content", "center");

            Span roundIndex = new Span(String.valueOf(i + 1));
            roundIndex.getStyle().set("position", "absolute");
            roundIndex.getStyle().set("bottom", "2px");
            roundIndex.getStyle().set("right", "4px");
            roundIndex.getStyle().set("font-size", "12px");
            roundIndex.getStyle().set("color", "gray");
            roundCell.add(roundIndex);

            if (i == 2 || i == 5 || i == 8) {
                Span star = new Span("⭐");
                star.getStyle().set("position", "absolute");
                star.getStyle().set("top", "2px");
                star.getStyle().set("right", "4px");
                star.getStyle().set("font-size", "14px");
                star.getStyle().set("color", "gold");
                star.getElement().setAttribute("data-type", "star");
                roundCell.add(star);
            }

            roundCells[i] = roundCell;
            add(roundCell);
        }
    }

    public void setRoundScore(int round, int score) {
        if (round < 1 || round > 9) return;

        Div cell = roundCells[round - 1];

        cell.getChildren()
                .filter(c -> "score".equals(c.getElement().getAttribute("data-type")))
                .forEach(cell::remove);

        Span scoreLabel = new Span(String.valueOf(score));
        scoreLabel.getStyle().set("font-size", "16px");
        scoreLabel.getStyle().set("font-weight", "bold");
        scoreLabel.getElement().setAttribute("data-type", "score");
        cell.add(scoreLabel);
    }
}
