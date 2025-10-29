package com.psam.game;

import java.util.Optional;

public class Board {
    private final Integer size = 6;
    private final Project[][] grid = new Project[size][size]; // [row][col]

    public Optional<Project> getField(int r, int c) {
        return Optional.ofNullable(grid[r][c]);
    }

    public boolean isEmpty(int r, int c) {
        return grid[r][c] == null;
    }

    public void set(int r, int c, Project p) {
        grid[r][c] = p;
    }

    public int getSize() {
        return size;
    }
}
