package com.psam.game;

import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class GameService {

    private final Random rng = new Random();
    private final Board board = new Board();

    private int lastD1 = 0, lastD2 = 0;
    private int turnStage = 0;     // 0 - brak rundy, 1 - pierwszy ruch, 2 - drugi ruch
    private boolean roundActive = false;

    public Board board() { return board; }
    public int d1() { return lastD1; }
    public int d2() { return lastD2; }
    public int getTurnStage() { return turnStage; }
    public boolean isRoundActive() { return roundActive; }


    public void startRound() {
        if (roundActive)
            throw new IllegalStateException("Runda już trwa!");

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
            turnStage = 2;
            return new PlaceResult(
                    project,
                    row,
                    column,
                    "Pierwszy ruch: " + project + " w kolumnie " + (column + 1),
                    false
            );

        } else if (turnStage == 2) {
            column = lastD2 - 1;
            project = rollToProject(lastD1);
            board.set(row, column, project);

            int points = scoreRound();
            resetRound();

            return new PlaceResult(
                    project,
                    row,
                    column,
                    "Drugi ruch: " + project + " w kolumnie " + (column + 1)
                            + ". Runda zakończona! Punkty: " + points,
                    true
            );
        }

        throw new IllegalStateException("Nieprawidłowy stan rundy!");
    }

    public int scoreRound() {
        int sum = lastD1 + lastD2;
        int row = mapSumToRow(sum);
        int pts = 0;
        for (int c = 0; c < board.getSize(); c++) {
            if (!board.isEmpty(row, c)) pts++;
        }
        return pts;
    }

    private int mapSumToRow(int s) {
        return switch (s) {
            case 3, 4 -> 0;
            case 5, 6 -> 1;
            case 7 -> 2;
            case 8 -> 3;
            case 9, 10 -> 4;
            case 11, 12 -> 5;
            default -> 0;
        };
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
    }
}
