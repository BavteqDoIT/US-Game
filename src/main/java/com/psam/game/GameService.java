package com.psam.game;

import com.psam.ui.screens.GameScreen;
import com.psam.ui.utils.UIPoints;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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

    private boolean setupPhase = true;
    private int setupBuildingsPlaced = 0;

    private boolean highlightAllColumns = true;
    public boolean waitingForSecondPlacementAfterDouble = false;
    public boolean activeBonus = false;

    public void setUIPoints(UIPoints uiPoints) { this.uiPoints = uiPoints; }
    public Board board() { return board; }
    public int d1() { return lastD1; }
    public int d2() { return lastD2; }
    public int getRoundCount() { return roundCount; }
    public boolean isRoundActive() { return roundActive; }
    public boolean shouldHighlightAllColumns() { return highlightAllColumns; }
    public int getTotalPoints() { return totalPoints; }
    public boolean getWaitingForSecondPlacementAfterDouble() { return waitingForSecondPlacementAfterDouble; }

    public void startRound() {
        if (setupPhase)
            throw new IllegalStateException("Najpierw zakończ fazę początkową (postaw 2 budynki).");
        if (roundActive)
            throw new IllegalStateException("Runda już trwa!");
        if (isGameOver())
            throw new IllegalStateException("Gra już się zakończyła!");

        lastD1 = 1 + rng.nextInt(6);
        lastD2 = 1 + rng.nextInt(6);
        resetRoundFlags();
        roundActive = true;
    }

    public record PlaceResult(Project project, int row, int col, String message, boolean roundEnded) {}

    public PlaceResult place(int row, int col, Project chosenProject) {
        if (board.get(row, col) != null) {
            throw new IllegalStateException("To pole jest już zajęte! Wybierz inne miejsce.");
        }
        if(setupPhase) {
            board.set(row, col, chosenProject);
            setupBuildingsPlaced++;

            String msg = "Faza początkowa: postawiono " + chosenProject + " w kolumnie " + (col + 1);

            if (setupBuildingsPlaced >= 2) {
                setupPhase = false;
                highlightAllColumns = false;
                msg += " Faza początkowa zakończona — możesz rozpocząć pierwszą rundę (rzuć kostkami).";
            }

            return new PlaceResult(chosenProject, row, col, msg, false);
        } else if (!setupPhase) {
            board.set(row, col, chosenProject);
            String msg = "Faza bonusowa: postawiono " + chosenProject + " w kolumnie " + (col + 1)
                    + " (+ " + 1 + " pkt).";
            return new PlaceResult(chosenProject, row, col, msg, true);
        } else {
            return null;
        }
    }

    public PlaceResult place(int row, int chosenColumn) {
        if (!roundActive)
            throw new IllegalStateException("Najpierw rozpocznij rundę!");

        int targetColumn1 = lastD1 - 1;
        int targetColumn2 = lastD2 - 1;
        if (!hasFreeSpaceInColumn(targetColumn1)) {
            List<Integer> freer = findFreerColumnsRecursive(targetColumn1, 1);
            if (!freer.isEmpty()) {
                targetColumn1 = freer.get(0);
            }
        }

        if (!hasFreeSpaceInColumn(targetColumn2)) {
            List<Integer> freer = findFreerColumnsRecursive(targetColumn2, 1);
            if (!freer.isEmpty()) {
                targetColumn2 = freer.get(0);
            }
        }

        boolean isFirstCol = chosenColumn == targetColumn1;
        boolean isSecondCol = chosenColumn == targetColumn2;

        Project project;
        int gainedPoints;

        if (waitingForSecondPlacementAfterDouble) {
            project = Project.PLAC;
            board.set(row, chosenColumn, project);
            waitingForSecondPlacementAfterDouble = false;
            boolean isOver = handleRoundEndIfNeeded();
            if(isOver){
                resetRoundFlags();
            }

            String msg = "Drugi ruch po dublecie: postawiono Plac w kolumnie " + (chosenColumn + 1)
                    + (isOver ? " Runda zakończona!" : "");
            return new PlaceResult(project, row, chosenColumn, msg, isOver);
        }

        if (isDouble(lastD1, lastD2)) {
            project = rollToProject(lastD1);
            board.set(row, targetColumn1, project);

            highlightAllColumns = true;
            waitingForSecondPlacementAfterDouble = true;

            String msg = "Dubel! Postawiono " + project +
                    "\n Teraz możesz postawić FABRYKĘ w dowolnej kolumnie.";
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

        boolean roundEnded = firstColumnUsed && secondColumnUsed;
        boolean isOver = roundEnded && handleRoundEndIfNeeded();

        String msg = "Ruch: " + project + " w kolumnie " + (chosenColumn + 1)
                         + (isOver ? " Runda zakończona!" : "");
        return new PlaceResult(project, row, chosenColumn, msg, isOver);
    }

    private boolean handleRoundEndIfNeeded() {
        if (isBonusRound()) {
            highlightAllColumns = true;
            activeBonus = true;
            return false;
        }
        return true;
    }

    private PlaceResult handleProjectPlacement(Project project, int row, int col,
                                               boolean isFirstCol, boolean isSecondCol, boolean secondMove) {
        board.set(row, col, project);

        if (isFirstCol) firstColumnUsed = true;
        if (isSecondCol) secondColumnUsed = true;

        boolean isOver = false;
        if (secondMove) {
            isOver = handleRoundEndIfNeeded();
        }

        String label = secondMove ? "Specjalny ruch (drugi)" : "Specjalny ruch (pierwszy)";
        String msg = label + ": " + project + " w kolumnie " + (col + 1)
                + (isOver ? " Runda zakończona!" : "");

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

    public void resetRound() {
        System.out.println("total points: " + totalPoints + ", round points: " + roundPoints);
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
        setupPhase = true;
        setupBuildingsPlaced = 0;
        highlightAllColumns = true;
        resetRound();
    }

    public void endInitialPhase() {
        setupPhase = false;
        highlightAllColumns = false;
    }

    public boolean isSetupPhase() {
        return setupPhase;
    }

    public boolean hasFreeSpaceInColumn(int col) {
        for (int row = 0; row < board.getSize() - 1; row++) {
            if (board.get(row, col) == null) {
                return true;
            }
        }
        return false;
    }

    public List<Integer> findFreerColumnsRecursive(int col, int step) {
        int size = board.getSize();
        List<Integer> result = new ArrayList<>();

        int lowerCol = col - step;
        int upperCol = col + step;

        int freeLower = 0;
        int freeUpper = 0;

        if (lowerCol >= 0) {
            for (int row = 0; row < size; row++) {
                if (board.get(row, lowerCol) == null) {
                    freeLower++;
                }
            }
        }

        if (upperCol < size) {
            for (int row = 0; row < size; row++) {
                if (board.get(row, upperCol) == null) {
                    freeUpper++;
                }
            }
        }

        if (freeLower > 0 || freeUpper > 0) {
            if (freeLower > freeUpper) {
                result.add(lowerCol);
            } else if (freeUpper > freeLower) {
                result.add(upperCol);
            } else if (freeLower == freeUpper && freeLower > 0) {
                if (lowerCol >= 0) result.add(lowerCol);
                if (upperCol < size) result.add(upperCol);
            }
            return result;
        }

        boolean canGoLeft = lowerCol >= 0;
        boolean canGoRight = upperCol < size;

        if (!canGoLeft && !canGoRight) {
            return result;
        }

        return findFreerColumnsRecursive(col, step + 1);
    }

    public int calculatePointsForRow(int targetRow) {
        int cols = board.getSize();
        int rows = 5;
        boolean[][] visited = new boolean[rows][cols];
        int totalPoints = 0;

        for (int col = 0; col < cols; col++) {
            Project project = board.get(targetRow, col);
            if (project == null || project == Project.PLAC || project == Project.FABRYKA) continue;
            if (visited[targetRow][col]) continue;

            int[] start = findTopLeftStart(targetRow, col, project, new boolean[rows][cols]);
            boolean[][] groupVisited = new boolean[rows][cols];
            int points = sumPointsByColumns(start[0], start[1], project, groupVisited);

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (groupVisited[r][c]) visited[r][c] = true;
                }
            }

            totalPoints += points;
        }

        return totalPoints;
    }

    public void addRoundPoints(int points) {
        roundPoints += points;
        totalPoints += points;
        roundCount++;
        uiPoints.setRoundScore(roundCount, roundPoints);
        resetRound();
    }

    private int[] findTopLeftStart(int row, int col, Project project, boolean[][] visited) {
        int cols = board.getSize();
        int rows = 5;

        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            System.out.println("⛔ [" + row + "," + col + "] poza planszą – zwracam ten punkt.");
            return new int[]{row, col};
        }

        if (visited[row][col]) {
            System.out.println("⚠️ [" + row + "," + col + "] już odwiedzone – pomijam.");
            return new int[]{row, col};
        }

        Project current = board.get(row, col);
        if (current != project || current == Project.FABRYKA || current == Project.PLAC) {
            System.out.println("❌ [" + row + "," + col + "] to nie " + project + " (znaleziono " + current + ") – przerywam.");
            return new int[]{row, col};
        }

        visited[row][col] = true;
        System.out.println("✅ Analizuję [" + row + "," + col + "] = " + current);

        int bestRow = row;
        int bestCol = col;

        if (col > 0 && board.get(row, col - 1) == project) {
            System.out.println("↩️ Szukam dalej w lewo od [" + row + "," + col + "]");
            int[] left = findTopLeftStart(row, col - 1, project, visited);
            if (left[1] < bestCol || (left[1] == bestCol && left[0] < bestRow)) {
                System.out.println("🟦 Aktualizuję start: " + bestRow + "," + bestCol + " → " + left[0] + "," + left[1] + " (lewo lepsze)");
                bestRow = left[0];
                bestCol = left[1];
            }
        }

        if (row > 0 && board.get(row - 1, col) == project) {
            System.out.println("⬆️ Szukam dalej w górę od [" + row + "," + col + "]");
            int[] up = findTopLeftStart(row - 1, col, project, visited);
            if (up[0] < bestRow || (up[0] == bestRow && up[1] < bestCol)) {
                System.out.println("🟩 Aktualizuję start: " + bestRow + "," + bestCol + " → " + up[0] + "," + up[1] + " (góra lepsza)");
                bestRow = up[0];
                bestCol = up[1];
            }
        }

        System.out.println("🏁 Dla [" + row + "," + col + "] zwracam start: [" + bestRow + "," + bestCol + "]");
        return new int[]{bestRow, bestCol};
    }


    private int sumPointsByColumns(int startRow, int startCol, Project project, boolean[][] visited) {
        int cols = board.getSize();
        int rows = 5;
        int totalPoints = 0;

        System.out.println("→ Start zliczania kolumnowo od [" + startRow + "," + startCol + "] dla " + project);

        List<int[]> stack = new ArrayList<>();
        stack.add(new int[]{startRow, startCol});

        while (!stack.isEmpty()) {
            int[] pos = stack.remove(stack.size() - 1);
            int r = pos[0];
            int c = pos[1];

            if (r < 0 || r >= rows || c < 0 || c >= cols) continue;
            if (visited[r][c]) continue;

            Project current = board.get(r, c);
            if (current != project || current == Project.FABRYKA || current == Project.PLAC) continue;

            visited[r][c] = true;
            int pts = board.getPoints(r, c);
            totalPoints += pts;
            System.out.println("  • Liczę pole [" + r + "," + c + "] = " + project + " (+ " + pts + " pkt)");

            stack.add(new int[]{r - 1, c});
            stack.add(new int[]{r + 1, c});
            stack.add(new int[]{r, c - 1});
            stack.add(new int[]{r, c + 1});
        }

        System.out.println("→ Grupa " + project + " = " + totalPoints + " pkt");
        return totalPoints;
    }

    public void countFinalPoints() {
        findProject();
    }

    private void findProject() {
        totalPoints = getTotalPoints();
        int cols = board.getSize();
        int rows = 5;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (board.get(row, col) == Project.FABRYKA) {
                    fabricFound(row, col);
                }
                if (board.get(row, col) == Project.PLAC) {
                    squareFound(row,col);
                }
            }
        }
    }

    private void fabricFound(int row, int col) {
        int points = 0;

        boolean hasNatureBonus = false;
        boolean hasNegativeParcel = false;

        int[][] dirs = {
                {-1, 0},
                {1, 0},
                {0, -1},
                {0, 1}
        };

        for (int[] d : dirs) {
            int r = row + d[0];
            int c = col + d[1];

            if (r < 0 || r >= 5 || c < 0 || c >= board.getSize()) {
                continue;
            }

            Project neighbor = board.get(r, c);

            if (neighbor == null) continue;

            switch (neighbor) {
                case JEZIORO:
                case LAS:
                    hasNatureBonus = true;
                    break;

                case DOM:
                    points -= 2;
                    hasNegativeParcel = true;
                    break;

                case PLAC:
                    points -= 5;
                    hasNegativeParcel = true;
                    break;
            }
        }

        if (hasNatureBonus && !hasNegativeParcel) {
            points += 10;
        }
        totalPoints += points;

        System.out.println("Fabryka odnaleziona! \n Łącznie punktów doszło : " + points
                + "\nA suma punktów obecna to: " + totalPoints);
    }

    private void squareFound(int row, int col) {

        boolean hasHouse = false;
        boolean hasForest = false;
        boolean hasLake = false;

        int[][] dirs = {
                {-1, 0},
                {1, 0},
                {0, -1},
                {0, 1}
        };

        for (int[] d : dirs) {

            int r = row + d[0];
            int c = col + d[1];

            if (r < 0 || r >= 5 || c < 0 || c >= board.getSize()) {
                continue;
            }

            Project neighbor = board.get(r, c);

            if (neighbor == null) {
                continue;
            }

            switch (neighbor) {
                case DOM:
                    hasHouse = true;
                    break;

                case LAS:
                    hasForest = true;
                    break;

                case JEZIORO:
                    hasLake = true;
                    break;
            }
        }

        if (hasHouse && hasForest && hasLake) {
            totalPoints += 10;
            System.out.println("Warunek związany z placem został spełniony + 10 pkt! \n Total points: " + totalPoints );
        }
    }
}
