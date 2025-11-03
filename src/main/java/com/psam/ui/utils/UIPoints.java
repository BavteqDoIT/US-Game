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

            // Numer rundy w rogu
            Span roundIndex = new Span(String.valueOf(i + 1));
            roundIndex.getStyle().set("position", "absolute");
            roundIndex.getStyle().set("bottom", "2px");
            roundIndex.getStyle().set("right", "4px");
            roundIndex.getStyle().set("font-size", "12px");
            roundIndex.getStyle().set("color", "gray");
            roundCell.add(roundIndex);

            add(roundCell);
            roundCells[i] = roundCell;
        }
    }

//    public void setRoundScore(int round, int score) {
//        if (round < 1 || round > 9) return;
//
//        Div cell = roundCells[round - 1];
//
//        if (cell.getComponentCount() > 1) {
//            cell.remove(cell.getComponentAt(1));
//        }
//
//        Span scoreLabel = new Span(String.valueOf(score));
//        scoreLabel.getStyle().set("font-size", "16px");
//        scoreLabel.getStyle().set("font-weight", "bold");
//        cell.add(scoreLabel);
//    }
//
//    public void clearScores() {
//        for (Div cell : roundCells) {
//            if (cell.getComponentCount() > 1) {
//                cell.remove(cell.getComponentAt(1));
//            }
//        }
//    }
}
