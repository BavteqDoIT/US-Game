package com.psam.game;

public class Board {
    private final int size = 6;
    public int getRowCount() { return 5; }
    public int getColCount() { return 6; }
    private final Project[][] grid = new Project[size][size];
    private final int[][] points = new int[size][size];

    public Board() {
        generatePoints();
    }

    public void set(int row, int col, Project project) {
        grid[row][col] = project;
    }

    public int getSize() {
        return size;
    }

    public Project get(int row, int col) {
        return grid[row][col];
    }

    public int getPoints(int row, int col) {
        return points[row][col];
    }

    private void generatePoints() {
        int[][] template = {
                {3,0,2,2,0,3},
                {0,1,0,0,1,0},
                {3,0,1,1,0,2},
                {0,1,0,0,1,0},
                {3,0,2,2,0,3},
                {0,0,0,0,0,0}
        };

        for (int r = 0; r < size; r++) {
            System.arraycopy(template[r], 0, points[r], 0, size);
        }
    }

    public void clear() {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                grid[r][c] = null;
            }
        }
    }

    public Board copy() {
        Board b = new Board();

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                b.grid[r][c] = this.grid[r][c];
            }
        }

        return b;
    }

    public Project[][] getGrid() {
        return grid;
    }
}
