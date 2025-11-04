package com.psam.game;

import com.psam.ui.utils.UIPoints;
import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class GameService {

    private final Random rng = new Random();
    private final Board board = new Board();

    private int lastD1 = 0, lastD2 = 0;
    private boolean firstColumnUsed = false;
    private boolean secondColumnUsed = false;

    private boolean roundActive = false;
    private int roundCount = 0;
    private int totalPoints = 0;
    private int roundPoints = 0;
    private UIPoints uiPoints;

    private static final int MAX_ROUNDS = 9;

    public void setUIPoints(UIPoints uiPoints) {
        this.uiPoints = uiPoints;
    }

    public Board board() { return board; }
    public int d1() { return lastD1; }
    public int d2() { return lastD2; }
    public int getRoundCount() { return roundCount; }
    public boolean isRoundActive() { return roundActive; }

    public void startRound() {
        if (roundActive)
            throw new IllegalStateException("Runda już trwa!");

        if (isGameOver())
            throw new IllegalStateException("Gra już się zakończyła!");

        lastD1 = 1 + rng.nextInt(6);
        lastD2 = 1 + rng.nextInt(6);
        firstColumnUsed = false;
        secondColumnUsed = false;
        roundActive = true;
    }

    public record PlaceResult(Project project, int row, int col, String message, boolean roundEnded) {}

    public PlaceResult place(int row, int chosenColumn) {
        if (!roundActive)
            throw new IllegalStateException("Najpierw rozpocznij rundę!");

        int targetColumn1 = lastD1 - 1;
        int targetColumn2 = lastD2 - 1;

        boolean isFirstCol = chosenColumn == targetColumn1;
        boolean isSecondCol = chosenColumn == targetColumn2;

        if (!isFirstCol && !isSecondCol)
            throw new IllegalArgumentException("Wybrana kolumna nie jest aktywna w tej rundzie!");

        Project project;
        int gainedPoints;

        if (isFirstCol && !firstColumnUsed) {
            project = rollToProject(lastD2);
            board.set(row, targetColumn1, project);
            gainedPoints = board.getPoints(row, targetColumn1);
            roundPoints += gainedPoints;
            firstColumnUsed = true;
        } else if (isSecondCol && !secondColumnUsed) {
            project = rollToProject(lastD1);
            board.set(row, targetColumn2, project);
            gainedPoints = board.getPoints(row, targetColumn2);
            roundPoints += gainedPoints;
            secondColumnUsed = true;
        } else {
            throw new IllegalStateException("W tej kolumnie już postawiono budynek!");
        }

        boolean roundEnded = firstColumnUsed && secondColumnUsed;
        String message = (isFirstCol ? "Pierwszy" : "Drugi") +
                " ruch: " + project + " w kolumnie " + (chosenColumn + 1) +
                " (+ " + gainedPoints + " pkt.)";

        if (roundEnded) {
            totalPoints += roundPoints;
            roundCount++;
            uiPoints.setRoundScore(roundCount, roundPoints);

            message += " Runda zakończona! Łącznie: " + totalPoints + " pkt. (Runda " + roundCount + "/" + MAX_ROUNDS + ")";
            resetRound();
        }

        return new PlaceResult(
                project,
                row,
                chosenColumn,
                message,
                roundEnded
        );
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
        firstColumnUsed = false;
        secondColumnUsed = false;
        roundPoints = 0;
        roundActive = false;
    }

    public boolean isGameOver() {
        return roundCount >= MAX_ROUNDS;
    }

    public void resetGame() {
        board.clear();
        roundCount = 0;
        totalPoints = 0;
        resetRound();
    }
}
