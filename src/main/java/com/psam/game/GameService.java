package com.psam.game;

import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class GameService {
    private final Random rng = new Random();
    private final Board board = new Board();
    private int lastD1 = 0, lastD2 = 0;

    public Board board() { return board; }
    public int d1() { return lastD1; }
    public int d2() { return lastD2; }

    public void roll() {
        lastD1 = 1 + rng.nextInt(6);
        lastD2 = 1 + rng.nextInt(6);
    }

    public void place(int row, int col, Project p) {
        if (lastD2 == 0) throw new IllegalStateException("Najpierw rzuć kośćmi.");
        if (col != lastD2 - 1) throw new IllegalArgumentException("Musisz wybrać kolumnę " + lastD2 + ".");
        if (!board.isEmpty(row, col)) throw new IllegalStateException("Pole zajęte.");
        board.set(row, col, p);
    }

    public int scoreRound() {
        int sum = lastD1 + lastD2;
        int row = mapSumToRow(sum);
        int pts = 0;
        for (int c = 0; c < board.getSize(); c++) if (!board.isEmpty(row, c)) pts++;
        lastD1 = lastD2 = 0;
        return pts;
    }

    private int mapSumToRow(int s) {
        return switch (s) {
            case 3,4 -> 0; case 5,6 -> 1; case 7 -> 2; case 8 -> 3; case 9,10 -> 4; case 11 -> 5;
            default -> 0;
        };
    }
}
