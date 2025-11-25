package com.psam.game;

import com.psam.ui.screens.GameScreen;
import com.psam.ui.utils.UIGrid;
import com.psam.ui.utils.UIPoints;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameService {
    public static class RoundSnapshot {
        public Board board;
        public int totalPoints;
        public int roundCount;
        public boolean isBonusRound;
        public boolean activeBonus;
        public int d1;
        public int d2;
        public boolean setupPhase;
        public int setupBuildingsPlaced;
        public Set<Project> availableBonusProjects;
    }

    private static final int MAX_ROUNDS = 9;
    private RoundSnapshot snapshot;
    private int placeCount = 0;

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
    private UIGrid uiGrid;

    public void setUIGrid(UIGrid uiGrid) {
        this.uiGrid = uiGrid;
    }
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
        saveSnapshot();
        placeCount = 0;
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
        System.out.println("---- PLACE CALLED ----");
        System.out.println("Row = " + row + ", Col = " + chosenColumn);
        System.out.println("RoundActive = " + roundActive);
        System.out.println("waitingForSecondPlacementAfterDouble = " + waitingForSecondPlacementAfterDouble);
        System.out.println("firstColumnUsed = " + firstColumnUsed + ", secondColumnUsed = " + secondColumnUsed);
        System.out.println("lastD1 = " + lastD1 + ", lastD2 = " + lastD2 + "\n");

        System.out.println("Initial targetColumn1 = " + targetColumn1);
        System.out.println("Initial targetColumn2 = " + targetColumn2 + "\n");
        if (!hasFreeSpaceInColumn(targetColumn1)) {
            List<Integer> freer = findFreerColumnsRecursive(targetColumn1, targetColumn2, 1);
            if (!freer.isEmpty()) {
                targetColumn1 = freer.get(0);
            }
        }

        if (!hasFreeSpaceInColumn(targetColumn2)) {
            List<Integer> freer = findFreerColumnsRecursive(targetColumn2, targetColumn1, 1);
            if (!freer.isEmpty()) {
                targetColumn2 = freer.get(0);
            }
        }

        boolean isFirstCol = chosenColumn == targetColumn1;
        boolean isSecondCol = chosenColumn == targetColumn2;

        System.out.println("FINAL targetColumn1 = " + targetColumn1);
        System.out.println("FINAL targetColumn2 = " + targetColumn2);
        System.out.println("chosenColumn == targetColumn1 ? " + (chosenColumn == targetColumn1));
        System.out.println("chosenColumn == targetColumn2 ? " + (chosenColumn == targetColumn2) + "\n");

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
            System.out.println("→ DRUGI RUCH PO DUBLECIE (waitingForSecondPlacementAfterDouble=true)" + "\n");
            return new PlaceResult(project, row, chosenColumn, msg, isOver);

        }

        if (isDouble(lastD1, lastD2)) {
            System.out.println("→ DUBEL WYKRYTY! (d1=d2)");
            System.out.println("Pierwszy projekt: " + rollToProject(lastD1) + "\n");
            project = rollToProject(lastD1);
            board.set(row, targetColumn1, project);

            highlightAllColumns = true;
            waitingForSecondPlacementAfterDouble = true;

            String msg = "Dubel! Postawiono " + project +
                    "\n Teraz możesz postawić FABRYKĘ w dowolnej kolumnie.";
            return new PlaceResult(project, row, chosenColumn, msg, false);
        }

        if (isSame(rollToProject(lastD1), rollToProject(lastD2))) {
            System.out.println("→ SPECJALNY RUCH (isSame=true)");
            System.out.println("placed = " + placed + "\n");
            if (!placed) {
                project = rollToProject(isFirstCol ? lastD2 : lastD1);
                placed = true;
                return handleProjectPlacement(project, row, chosenColumn, isFirstCol, isSecondCol, false);
            } else {
                project = Project.FABRYKA;
                return handleProjectPlacement(project, row, chosenColumn, isFirstCol, isSecondCol, true);
            }
        }

        System.out.println("→ STANDARDOWY RUCH");
        System.out.println("isFirstCol = " + isFirstCol + ", firstColumnUsed = " + firstColumnUsed);
        System.out.println("isSecondCol = " + isSecondCol + ", secondColumnUsed = " + secondColumnUsed + "\n");


        if (isFirstCol && !firstColumnUsed) {
            project = rollToProject(lastD2);
            firstColumnUsed = true;
            placeCount++;
        } else if (isSecondCol && !secondColumnUsed || !secondColumnUsed && placeCount != 0) {
            project = rollToProject(lastD1);
            secondColumnUsed = true;
        }
        else {
            System.out.println("❌ ERROR: próba postawienia w kolumnie " + chosenColumn);
            System.out.println("targetColumn1 = " + targetColumn1 + ", used = " + firstColumnUsed);
            System.out.println("targetColumn2 = " + targetColumn2 + ", used = " + secondColumnUsed + "\n");
            throw new IllegalStateException("W tej kolumnie już postawiono budynek!");
        }

        board.set(row, chosenColumn, project);

        boolean roundEnded = firstColumnUsed && secondColumnUsed;
        boolean isOver = roundEnded && handleRoundEndIfNeeded();

        String msg = "Ruch: " + project + " w kolumnie " + (chosenColumn + 1)
                         + (isOver ? " Runda zakończona!" : "");
        System.out.println("→ PLACED PROJECT = " + project);
        System.out.println("firstColumnUsed = " + firstColumnUsed + ", secondColumnUsed = " + secondColumnUsed);
        System.out.println("---------------------------");
        return new PlaceResult(project, row, chosenColumn, msg, isOver);
    }

    private boolean handleRoundEndIfNeeded() {
        if (isBonusRound()) {
            uiGrid.messageService.log("Runda Bonusowa! Wybierz bonusowy projekt i umieść w dowolnym wolnym miejscu! Pamiętaj projekt nie będzie dostępny już w innych bonusowych rundach do końca gry!");
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

    public Project rollToProject(int roll) {
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

    public List<Integer> findFreerColumnsRecursive(int col, int otherCol, int step) {
        int rows = board.getRowCount();
        int cols = board.getColCount();
        List<Integer> result = new ArrayList<>();

        int lowerCol = col - step;
        int upperCol = col + step;

        int freeLower = (lowerCol >= 0) ? countFree(lowerCol) : 0;
        int freeUpper = (upperCol < cols) ? countFree(upperCol) : 0;

        if (lowerCol >= 0)
            System.out.println("Kolumna " + lowerCol + " ma " + freeLower + " wolnych pól");
        if (upperCol < cols)
            System.out.println("Kolumna " + upperCol + " ma " + freeUpper + " wolnych pól");

        if (freeLower > 0 || freeUpper > 0) {

            int chosen = -1;

            if (freeLower > freeUpper) chosen = lowerCol;
            else if (freeUpper > freeLower) chosen = upperCol;
            else if (freeLower == freeUpper && freeLower > 0)
                chosen = Math.min(lowerCol, upperCol);

            if (chosen == otherCol && countFree(chosen) == 1) {
                System.out.println("⚠ Kolumna " + chosen +
                        " ma tylko 1 miejsce i jest zarezerwowana dla drugiej kostki – szukam dalej!");
                return findFreerColumnsRecursive(col, otherCol, step + 1);
            }

            if (chosen >= 0) {
                result.add(chosen);
                return result;
            }
        }

        boolean canGoLeft = lowerCol >= 0;
        boolean canGoRight = upperCol < cols;

        if (!canGoLeft && !canGoRight) {
            System.out.println("Brak dalszych kolumn do sprawdzenia, kończę rekurencję");
            return result;
        }

        System.out.println("Nie znaleziono bezpiecznych kolumn, idę dalej krok " + (step + 1));
        return findFreerColumnsRecursive(col, otherCol, step + 1);
    }

    private int countFree(int col) {
        int free = 0;
        for (int row = 0; row < board.getRowCount(); row++) {
            if (board.get(row, col) == null) free++;
        }
        return free;
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

    public boolean isRoundReadyToEnd() {
        return firstColumnUsed && secondColumnUsed;
    }
    public void saveSnapshot() {
        RoundSnapshot snap = new RoundSnapshot();

        snap.board = board.copy();

        snap.totalPoints = totalPoints;
        snap.roundCount = roundCount;
        snap.activeBonus = activeBonus;

        snap.d1 = lastD1;
        snap.d2 = lastD2;
        snap.setupPhase = this.setupPhase;
        snap.setupBuildingsPlaced = this.setupBuildingsPlaced;
        snap.availableBonusProjects = new HashSet<>(uiGrid.getAvailableBonusProjects());
        this.snapshot = snap;
    }

    public void restoreSnapshot() {
        if (snapshot == null) return;

        for (int r = 0; r < board.getSize(); r++) {
            for (int c = 0; c < board.getSize(); c++) {
                board.set(r, c, snapshot.board.get(r, c));
            }
        }

        totalPoints = snapshot.totalPoints;
        roundCount = snapshot.roundCount;


        activeBonus = snapshot.activeBonus;

        lastD1 = snapshot.d1;
        lastD2 = snapshot.d2;
        this.setupPhase = snapshot.setupPhase;
        this.setupBuildingsPlaced = snapshot.setupBuildingsPlaced;
        uiGrid.setAvailableBonusProjects(new HashSet<>(snapshot.availableBonusProjects));
        firstColumnUsed = false;
        secondColumnUsed = false;
        placed = false;
        highlightAllColumns = false;
        roundPoints = 0;
    }
}
