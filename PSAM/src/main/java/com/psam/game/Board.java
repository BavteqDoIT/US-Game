package com.psam.game;

import java.util.Optional;

public class Board {
    private final Project[][] grid = new Project[6][6]; // [row][col]

    public Optional<Project> get(int r, int c) { return Optional.ofNullable(grid[r][c]); }
    public boolean empty(int r, int c) { return grid[r][c] == null; }
    public void set(int r, int c, Project p) { grid[r][c] = p; }

    public int size() { return 6; }
}
