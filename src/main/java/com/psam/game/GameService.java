package com.psam.game;

import com.psam.ui.utils.UIPoints;
import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class GameService {

    private static final int MAX_ROUNDS = 9;

    private final Random rng = new Random();
    private final Board board = new Board();

    private int lastD1 = 0;
    private int lastD2 = 0;
    private boolean firstColumnUsed = false;
    private boolean secondColumnUsed = false;
    private boolean placed = false;

    private boolean roundActive = false;
    private int roundCount = 0;
    private int totalPoints = 0;
    private int roundPoints = 0;
    private UIPoints uiPoints;

    private boolean highlightAllColumns = false;
    private boolean waitingForSecondPlacementAfterDouble = false;
    private boolean activeBonus = false;

    public void setUIPoints(UIPoints uiPoints) { this.uiPoints = uiPoints; }
    public Board board() { return board; }
    public int d1() { return lastD1; }
    public int d2() { return lastD2; }
    public int getRoundCount() { return roundCount; }
    public boolean isRoundActive() { return roundActive; }
    public boolean shouldHighlightAllColumns() { return highlightAllColumns; }
    public int getTotalPoints() { return totalPoints; }

    public void startRound() {
        if (roundActive)
            throw new IllegalStateException("Runda już trwa!");
        if (isGameOver())
            throw new IllegalStateException("Gra już się zakończyła!");

        lastD1 = rng.nextInt(6) + 1;
        lastD2 = rng.nextInt(6) + 1;
        resetRoundFlags();
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

        Project project;
        int gainedPoints;

        if (activeBonus && !waitingForSecondPlacementAfterDouble) {
            project = Project.DOM;
            board.set(row, chosenColumn, project);

            gainedPoints = board.getPoints(row, chosenColumn);
            roundPoints += gainedPoints;

            String origin = getRoundOriginText();
            totalPoints += roundPoints;
            roundCount++;
            uiPoints.setRoundScore(roundCount, roundPoints);

            String msg = "BONUS! Postawiono DOM w kolumnie " + (chosenColumn + 1)
                    + " (+ " + gainedPoints + " pkt). Runda zakończona! Łącznie: "
                    + totalPoints + " pkt. (Runda " + roundCount + "/" + MAX_ROUNDS + " / " + origin + ")";

            resetRound();
            return new PlaceResult(project, row, chosenColumn, msg, true);
        }

        if (waitingForSecondPlacementAfterDouble) {
            project = Project.PLAC;
            board.set(row, chosenColumn, project);

            gainedPoints = board.getPoints(row, chosenColumn);
            roundPoints += gainedPoints;
            waitingForSecondPlacementAfterDouble = false;

            boolean isOver = handleRoundEndIfNeeded();

            String msg = "Drugi ruch po dublecie: postawiono Plac w kolumnie " + (chosenColumn + 1)
                    + " (+ " + gainedPoints + " pkt)." + (isOver ? " Runda zakończona!" : "");
            return new PlaceResult(project, row, chosenColumn, msg, isOver);
        }

        if (isDouble(lastD1, lastD2)) {
            project = rollToProject(lastD1);
            board.set(row, targetColumn1, project);

            gainedPoints = board.getPoints(row, targetColumn1);
            roundPoints += gainedPoints;

            highlightAllColumns = true;
            waitingForSecondPlacementAfterDouble = true;

            String msg = "Dubel! Postawiono " + project + " (+ " + gainedPoints +
                    " pkt). Teraz możesz postawić FABRYKĘ w dowolnej kolumnie.";
            return new PlaceResult(project, row, chosenColumn, msg, false);
        }

        if (isSame(rollToProject(lastD1), rollToProject(lastD2))) {
            if (!placed) {
                project = rollToProject(isFirstCol ? lastD2 : lastD1);
                placed = true;
                return handleProjectPlacement(project, row, chosenColumn, isFirstCol, isSecondCol, false);
            } else {
                project = Project.FABRYKA;
                return handleProjectPlacement(project, row, chosenColumn, isFirstCol, isSecondCol, true);
            }
        }

        if (isFirstCol && !firstColumnUsed) {
            project = rollToProject(lastD2);
            firstColumnUsed = true;
        } else if (isSecondCol && !secondColumnUsed) {
            project = rollToProject(lastD1);
            secondColumnUsed = true;
        } else {
            throw new IllegalStateException("W tej kolumnie już postawiono budynek!");
        }

        board.set(row, chosenColumn, project);
        gainedPoints = board.getPoints(row, chosenColumn);
        roundPoints += gainedPoints;

        boolean roundEnded = firstColumnUsed && secondColumnUsed;
        boolean isOver = roundEnded && handleRoundEndIfNeeded();

        String msg = "Ruch: " + project + " w kolumnie " + (chosenColumn + 1)
                + " (+ " + gainedPoints + " pkt.)" + (isOver ? " Runda zakończona!" : "");
        return new PlaceResult(project, row, chosenColumn, msg, isOver);
    }

    private boolean handleRoundEndIfNeeded() {
        if (isBonusRound()) {
            highlightAllColumns = true;
            activeBonus = true;
            return false;
        }
        totalPoints += roundPoints;
        roundCount++;
        uiPoints.setRoundScore(roundCount, roundPoints);
        resetRound();
        return true;
    }

    private String getRoundOriginText() {
        if (isDouble(lastD1, lastD2)) return "Dubel";
        if (isSame(rollToProject(lastD1), rollToProject(lastD2))) return "Ten sam projekt";
        return "Zwykła runda";
    }

    private PlaceResult handleProjectPlacement(Project project, int row, int col,
                                               boolean isFirstCol, boolean isSecondCol, boolean secondMove) {
        board.set(row, col, project);
        int gainedPoints = board.getPoints(row, col);
        roundPoints += gainedPoints;

        if (isFirstCol) firstColumnUsed = true;
        if (isSecondCol) secondColumnUsed = true;

        boolean isOver = false;
        if (secondMove) {
            isOver = handleRoundEndIfNeeded();
        }

        String label = secondMove ? "Specjalny ruch (drugi)" : "Specjalny ruch (pierwszy)";
        String msg = label + ": " + project + " w kolumnie " + (col + 1)
                + " (+ " + gainedPoints + " pkt.)" + (isOver ? " Runda zakończona!" : "");

        return new PlaceResult(project, row, col, msg, isOver);
    }


    public boolean isBonusRound() {
        return roundCount % 3 == 2;
    }

    public boolean isDouble(int d1, int d2) {
        return d1 == d2;
    }

    public boolean isSame(Project first, Project second) {
        return first == second;
    }

    private Project rollToProject(int roll) {
        return switch (roll) {
            case 1, 4 -> Project.DOM;
            case 2, 5 -> Project.LAS;
            case 3, 6 -> Project.JEZIORO;
            default -> throw new IllegalArgumentException("Nieprawidłowy wynik: " + roll);
        };
    }

    private void resetRound() {
        lastD1 = 0;
        lastD2 = 0;
        resetRoundFlags();
        roundPoints = 0;
        roundActive = false;
        activeBonus = false;
    }

    private void resetRoundFlags() {
        firstColumnUsed = false;
        secondColumnUsed = false;
        highlightAllColumns = false;
        waitingForSecondPlacementAfterDouble = false;
        placed = false;
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
