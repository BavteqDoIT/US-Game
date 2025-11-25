package com.psam.ui.screens;

import com.psam.game.GameService;
import com.psam.ui.utils.MessageService;
import com.psam.ui.utils.UIGrid;
import com.psam.ui.utils.UIPoints;
import com.psam.ui.utils.UILabelFactory;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@CssImport("./styles/styles.css")
@Route("main")
public class GameScreen extends VerticalLayout {

    private Button rollButton;
    private Button resetButton;
    private Button endRoundButton;
    private final GameService game;
    private String lastRollMessage = "";

    @Autowired
    public GameScreen(GameService game) {
        this.game = game;
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        setSizeFull();
        setSpacing(true);

        MessageService messageService = new MessageService();
        UIGrid grid = new UIGrid(game, messageService);
        game.setUIGrid(grid);
        grid.setOnEndRoundEnabled(() -> enableEndRoundButton());
        grid.refreshHighlights();

        UIPoints points = new UIPoints();
        game.setUIPoints(points);

        String[] ranges = {"3–4", "5–6", "7", "8–9", "10–11"};
        VerticalLayout rowLabels = UILabelFactory.createColumnLabels(ranges);

        HorizontalLayout gridWithLabels = new HorizontalLayout(rowLabels, grid);
        gridWithLabels.setAlignItems(FlexComponent.Alignment.START);
        gridWithLabels.setSpacing(true);

        messageService.log("W fazie początkowej musisz postawić 2 dowolne budynki w dowolnym miejscu na planszy.");

        rollButton = new Button("🎲 Rzuć kostkami");
        resetButton = new Button("↩️ Reset rundy");
        endRoundButton = new Button("✅ Zakończ rundę");

        rollButton.setEnabled(false);
        resetButton.setEnabled(true);
        endRoundButton.setEnabled(false);

        resetButton.addClickListener(e -> {
            try {
                messageService.log("Zresetowano rundę");
            } catch (Exception exception){
                messageService.log(exception.getMessage());
            }
        });

        endRoundButton.addClickListener(e -> {
           try {
               if(game.getRoundCount() >= 8){
                   messageService.log("Gratulację! Zdobyłeś " + game.getTotalPoints() + " punktów!");
               }
               messageService.log("Zakończono rundę! By rozpocząć kolejną rzuć kostkami.");
               rollButton.setEnabled(true);
               resetButton.setEnabled(false);
               endRoundButton.setEnabled(false);
           }catch (Exception exception){
               messageService.log(exception.getMessage());
           }
        });



        rollButton.addClickListener (e -> {
            try {
                game.startRound();
                rollButton.setEnabled(false);
                resetButton.setEnabled(true);
                endRoundButton.setEnabled(false);
                grid.clearHighlights();
                grid.highlightRoundColumns(game.isDouble(game.d1(), game.d2()));
                if(game.isDouble(game.d1(), game.d2())) {
                    lastRollMessage = "Wyrzuciłeś podwójnie 🎲 " + game.d1() +" → "+
                            "początkowo postaw " + game.rollToProject(game.d1()).name() + " w podświetlonej kolumnie, a następnie Plac w dowolnym dostępnym miejscu";
                } else if (game.isSame(game.rollToProject(game.d1()), game.rollToProject(game.d2()))) {
                    lastRollMessage = "Wyrzuciłeś: 🎲 " + game.d1() + " i 🎲 " + game.d2() +" → "+
                            "początkowo postaw " + game.rollToProject(game.d1()).name() + " w jednej z podświetlonych kolumn, a następnie Fabrykę w drugiej";
                } else {
                    int d1 = game.d1();
                    int d2 = game.d2();

                    String project1 = game.rollToProject(d1).name();
                    String project2 = game.rollToProject(d2).name();

                    boolean d1Free = game.hasFreeSpaceInColumn(d1 - 1);
                    boolean d2Free = game.hasFreeSpaceInColumn(d2 - 1);

                    String msg1;
                    String msg2;

                    if (d1Free) {
                        msg1 = project1 + " w kolumnie " + d2;
                    } else {
                        msg1 = "kolumna " + d2 + " (dla " + project1 + ") jest przepełniona — postaw budynek w podświetlonej kolumnie";
                    }

                    if (d2Free) {
                        msg2 = project2 + " w kolumnie " + d1;
                    } else {
                        msg2 = "kolumna " + d1 + " (dla " + project2 + ") jest przepełniona — postaw budynek w podświetlonej kolumnie";
                    }

                    lastRollMessage =
                            "Wyrzuciłeś: 🎲 " + d1 + " i 🎲 " + d2 + " → możesz postawić " +
                                    msg1 + " oraz " + msg2;
                }
                messageService.log(lastRollMessage);

                if (game.isGameOver()) {
                    UI.getCurrent().navigate("end");
                }
            } catch (Exception ex) {
                messageService.log("Błąd: " + ex.getMessage());
            }
        });

        endRoundButton.addClickListener(e->{
            try{
                if(!game.isSetupPhase()) {
                    grid.askUserForRowIfNeeded(game.d1() + game.d2());
                    System.out.println("Total punkty: " + game.getTotalPoints());
                } else {
                    game.endInitialPhase();
                }
                if (game.getRoundCount() >= 9) {
                    showEndGameButton(grid);
                }
            } catch (Exception exception){
                messageService.log(exception.getMessage());
            }
        });

        resetButton.addClickListener(e->{
            try{
                if(!game.isSetupPhase()) {
                    game.restoreSnapshot();
                    grid.restoreFromBoard(game.board());
                    grid.clearHighlights();
                    grid.highlightRoundColumns(game.isDouble(game.d1(), game.d2()));
                    messageService.log("Zresetowano rundę! " + lastRollMessage);
                    endRoundButton.setEnabled(false);
                } else {
                    game.resetGame();
                    UI.getCurrent().getPage().reload();
                }
            } catch (Exception exception){
                messageService.log(exception.getMessage());
            }
        });

        HorizontalLayout controls = new HorizontalLayout(rollButton, resetButton, endRoundButton);
        controls.setAlignItems(FlexComponent.Alignment.CENTER);
        controls.setSpacing(true);

        add(controls);
        add(gridWithLabels);
        add(points);
        add(messageService.getMessagePanel());

        setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, controls);
        setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, gridWithLabels);
        setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, points);
        setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, messageService.getMessagePanel());
    }

    public void enableEndRoundButton() {
        endRoundButton.setEnabled(true);
        rollButton.setEnabled(false);
        resetButton.setEnabled(true);
    }

    private void showEndGameButton(UIGrid grid) {
        rollButton.setVisible(false);
        resetButton.setVisible(false);
        endRoundButton.setVisible(false);

        Button backToStartButton = new Button("⬅️ Wróć do ekranu startowego");
        backToStartButton.addClickListener(ev -> {
            try {
                game.resetGame();
                UI.getCurrent().navigate(StartScreen.class);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        HorizontalLayout newControls = new HorizontalLayout(backToStartButton);
        newControls.setAlignItems(FlexComponent.Alignment.CENTER);
        newControls.setSpacing(true);

        add(newControls);
        setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, newControls);

        grid.showEndGameDialog();
    }
}
