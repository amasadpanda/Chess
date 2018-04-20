package Rulesets;

import java.util.HashSet;

public abstract class Piece {

    public boolean white;
    protected int drawableID;

    public Piece(boolean isWhite) {

        white = isWhite;

    }

    public Piece() {
        this(true);
    }

    public int getDrawableID() {
        return drawableID;
    }

    public abstract HashSet<Integer> getMoves(int loc, Piece[][] board, boolean forReal);

    public HashSet<Integer> getMoves(int loc, Piece[][] board) {
        return getMoves(loc, board, true);
    }

    public boolean inBounds(int r, int c) {
        return r >= 0 && r < 8 && c >= 0 && c < 8;
    }

    public abstract Piece copy();
}