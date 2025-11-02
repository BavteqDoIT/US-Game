package com.psam.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Board {
    private final int size = 6;
    private final Project[][] grid = new Project[size][size];
    private final int[][] points = new int[size][size]; // punkty na planszy

    public Board() {
        generatePoints(); // losowe rozmieszczenie punktów
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
        List<Integer> allPoints = new ArrayList<>(Collections.nCopies(size * size, 0));

        fillPoints(allPoints, 3, 5);
        fillPoints(allPoints, 2, 5);
        fillPoints(allPoints, 1, 6);

        Collections.shuffle(allPoints);


        int index = 0;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                points[r][c] = allPoints.get(index++);
            }
        }
    }

    private void fillPoints(List<Integer> list, int value, int count) {
        int placed = 0;
        for (int i = 0; i < list.size() && placed < count; i++) {
            if (list.get(i) == 0) {
                list.set(i, value);
                placed++;
            }
        }
    }

    public void printPointsLayout() {
        System.out.println("=== ROZKŁAD PUNKTÓW NA PLANSZY (dla UIGrid) ===");
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                System.out.print(points[r][c] + " ");
            }
            System.out.println();
        }
    }

    public void clear() {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                grid[r][c] = null;
            }
        }
    }
}
