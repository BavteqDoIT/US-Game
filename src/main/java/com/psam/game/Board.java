package com.psam.game;

public class Board {
    private static final int size = 6;
    private final Project[][] grid = new Project[size][size]; // [row][col]

    public boolean isEmpty(int row, int col) {
        return grid[row][col] == null;
    }

    public void set(int row, int col, Project project) {
        if (row < 0 || row >= size || col < 0 || col >= size)
            throw new IndexOutOfBoundsException("Nieprawidłowe współrzędne: (" + row + ", " + col + ")");
        grid[row][col] = project;
    }

    public Project get(int row, int col) {
        if (row < 0 || row >= size || col < 0 || col >= size)
            throw new IndexOutOfBoundsException("Nieprawidłowe współrzędne: (" + row + ", " + col + ")");
        return grid[row][col];
    }

    public int getSize() {
        return size;
    }

    public void clear() {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                grid[r][c] = null;
            }
        }
    }
}
