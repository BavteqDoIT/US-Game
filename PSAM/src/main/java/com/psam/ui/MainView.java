package com.psam.ui;

import com.psam.game.GameService;
import com.psam.game.Project;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("")
public class MainView extends VerticalLayout {

    private final GameService game;
    private final Button[][] cells = new Button[6][6];
    private final RadioButtonGroup<Project> picker = new RadioButtonGroup<>();

    @Autowired
    public MainView(GameService game) {
        this.game = game;

        var title = new H3("City Roller — MVP");

        picker.setItems(Project.DOM, Project.LAS, Project.JEZIORO, Project.FABRYKA, Project.PLAC);
        picker.setValue(Project.DOM);

        var grid = new FlexLayout();
        grid.getStyle().set("display", "grid");
        grid.getStyle().set("grid-template-columns", "repeat(6, 56px)");
        grid.getStyle().set("gap", "4px");

        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 6; c++) {
                var b = new Button("");
                b.setWidth("56px"); b.setHeight("56px");
                final int rr = r, cc = c;
                b.addClickListener(e -> onCell(rr, cc, b));
                grid.add(b);
                cells[r][c] = b;
            }
        }

        var rollBtn = new Button("Rzuć kośćmi", e -> {
            game.roll();
            Notification.show("Wyrzucono: " + game.d1() + " i " + game.d2() + " (kolumna " + game.d2() + ")");
            highlightColumn(game.d2() - 1);
        });

        var scoreBtn = new Button("Zakończ rundę", e -> {
            if (game.d2() == 0) { Notification.show("Najpierw rzuć kośćmi."); return; }
            int pts = game.scoreRound();
            Notification.show("Punkty w tej rundzie: " + pts);
            clearHighlights();
        });

        var controls = new HorizontalLayout(rollBtn, picker, scoreBtn);
        controls.setAlignItems(Alignment.CENTER);

        add(title, controls, grid);
        setAlignItems(Alignment.CENTER);
    }

    private void onCell(int r, int c, Button b) {
        try {
            game.place(r, c, picker.getValue());
            b.setText(shortLabel(picker.getValue()));
        } catch (Exception ex) {
            Notification.show(ex.getMessage());
        }
    }

    private String shortLabel(Project p) {
        return switch (p) {
            case DOM -> "D";
            case LAS -> "L";
            case JEZIORO -> "J";
            case FABRYKA -> "F";
            case PLAC -> "P";
        };
    }

    private void highlightColumn(int col) {
        clearHighlights();
        if (col < 0) return;
        for (int r = 0; r < 6; r++) cells[r][col].getStyle().set("border", "2px solid var(--lumo-primary-color)");
    }

    private void clearHighlights() {
        for (int r = 0; r < 6; r++)
            for (int c = 0; c < 6; c++)
                cells[r][c].getStyle().remove("border");
    }
}
