package com.psam.ui;

import com.psam.game.GameService;
import com.psam.game.Project;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@CssImport("./styles/styles.css")
@Route("")
public class MainView extends VerticalLayout {

    private final GameService game;
    private final int size;
    private final Div[][] cells;
    private final RadioButtonGroup<Project> picker = new RadioButtonGroup<>();

    @Autowired
    public MainView(GameService game) {
        this.game = game;
        this.size = game.board().getSize();
        this.cells = new Div[size][size];

        var title = new H3("City Roller — MVP");
        picker.setItems(Project.DOM, Project.LAS, Project.JEZIORO, Project.FABRYKA, Project.PLAC);

        var grid = new FlexLayout();
        grid.getStyle().set("display", "grid");
        grid.getStyle().set("grid-template-columns", "repeat(6, 56px)");
        grid.getStyle().set("gap", "4px");

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
                cell.addClickListener(e -> onCell(rr, cc, cell));
                grid.add(cell);
                cells[r][c] = cell;
            }
        }

        Button scoreBtn = new Button("Zakończ rundę");
        scoreBtn.setEnabled(false);

        Button rollBtn = new Button("Rzuć kostką");
        rollBtn.setEnabled(true);

        scoreBtn.addClickListener(e -> {
            if (game.d2() == 0) {
                Notification.show("Najpierw rzuć kośćmi.");
                return;
            }
            int pts = game.scoreRound();
            Notification.show("Punkty w tej rundzie: " + pts);
            clearHighlights();
            scoreBtn.setEnabled(false);
            rollBtn.setEnabled(true);
        });

        rollBtn.addClickListener(e -> {
            game.roll();
            Notification.show("Wyrzucono: " + game.d1() + " i " + game.d2() + " (kolumna " + game.d2() + ")");
            highlightColumn(game.d2() - 1);
            rollBtn.setEnabled(false);
            scoreBtn.setEnabled(true);
        });

        var controls = new HorizontalLayout(rollBtn, picker, scoreBtn);
        controls.setAlignItems(Alignment.CENTER);

        add(title, controls, grid);
        setAlignItems(Alignment.CENTER);
    }

    private void onCell(int r, int c, Div b) {
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
        for (int r = 0; r < size; r++) {
            cells[r][col].removeClassName("noHighlight");
            cells[r][col].addClassName("highlighted");
        }
    }

    private void clearHighlights() {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                cells[r][c].removeClassName("highlighted");
                cells[r][c].addClassName("noHighlight");
            }
        }
    }
}
