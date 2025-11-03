package com.psam.game;

import com.psam.ui.utils.UIPoints;
import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class GameService {

    private final Random rng = new Random();
    private final Board board = new Board();

    private int lastD1 = 0, lastD2 = 0;
    private int turnStage = 0;     // 0 - brak rundy, 1 - pierwszy ruch, 2 - drugi ruch
    private boolean roundActive = false;
    private int roundCount = 0;
    private int totalPoints = 0;
    private int roundPoints = 0;
    private UIPoints uiPoints;

    public void setUIPoints(UIPoints uiPoints) {
        this.uiPoints = uiPoints;
    }


    // maksymalna liczba rund w grze (możesz zmienić np. na 10)
    private static final int MAX_ROUNDS = 9;

    public Board board() { return board; }
    public int d1() { return lastD1; }
    public int d2() { return lastD2; }
    public void setRoundCount(int roundCount) { this.roundCount = roundCount; }
    public int getRoundCount() { return this.roundCount; }
    public int getTurnStage() { return turnStage; }
    public boolean isRoundActive() { return roundActive; }

    public void startRound() {
        if (roundActive)
            throw new IllegalStateException("Runda już trwa!");

        if (isGameOver())
            throw new IllegalStateException("Gra już się zakończyła!");

        lastD1 = 1 + rng.nextInt(6);
        lastD2 = 1 + rng.nextInt(6);
        turnStage = 1;
        roundActive = true;
    }

    public record PlaceResult(Project project, int row, int col, String message, boolean roundEnded) {}

    public PlaceResult place(int row) {
        if (!roundActive)
            throw new IllegalStateException("Najpierw rzuć kostkami!");

        int column;
        Project project;

        if (turnStage == 1) {
            column = lastD1 - 1;
            project = rollToProject(lastD2);
            board.set(row, column, project);

            int gainedPoints = board.getPoints(row, column);
            roundPoints += gainedPoints;
            turnStage = 2;
            return new PlaceResult(
                    project,
                    row,
                    column,
                    "Pierwszy ruch: " + project + " w kolumnie " + (column + 1)
                            + " (+ " + gainedPoints + " pkt.)",
                    false
            );
        } else if (turnStage == 2) {
            column = lastD2 - 1;
            project = rollToProject(lastD1);
            board.set(row, column, project);

            int gainedPoints = board.getPoints(row, column); // ✅ punkty za to jedno pole
            roundPoints += gainedPoints;
            totalPoints += roundPoints;

            roundCount++;
            uiPoints.setRoundScore(roundCount, roundPoints);
            resetRound();
            return new PlaceResult(
                    project,
                    row,
                    column,
                    "Drugi ruch: " + project + " w kolumnie " + (column + 1)
                            + ". Runda zakończona! Zdobyto +" + gainedPoints + " pkt."
                            + " Łącznie: " + totalPoints + " pkt."
                            + " (Runda " + roundCount + "/" + MAX_ROUNDS + ")",
                    true
            );
        }

        throw new IllegalStateException("Nieprawidłowy stan rundy!");
    }


    public int getTotalPoints() {
        return totalPoints;
    }

    private Project rollToProject(int roll) {
        return switch (roll) {
            case 1, 2 -> Project.DOM;
            case 3 -> Project.LAS;
            case 4 -> Project.JEZIORO;
            case 5 -> Project.FABRYKA;
            case 6 -> Project.PLAC;
            default -> throw new IllegalArgumentException("Nieprawidłowy wynik: " + roll);
        };
    }

    private void resetRound() {
        lastD1 = 0;
        lastD2 = 0;
        turnStage = 0;
        roundActive = false;
        roundPoints = 0;
    }

    public boolean isGameOver() {
        return roundCount >= MAX_ROUNDS;
    }

    public void resetGame() {
        board.clear();
        roundCount = 0;
        resetRound();
        totalPoints = 0;
    }
}
