package Rulesets.StandardPieces;

import Rulesets.Piece;
import Rulesets.StandardPieces.StandardPieces;

import java.util.HashSet;

public class King extends Piece {
    public boolean moved;

    public King(boolean isWhite) {
        super(isWhite);
    }

    public Piece copy() {
        King cpy = new King(this.white);
        cpy.moved = this.moved;
        return cpy;
    }

    public King(boolean isWhite, boolean move) {
        this(isWhite);
        moved = move;
    }

    public HashSet<Integer> getMoves(int loc, Piece[][] board, boolean forReal) {
        int r = loc / 8, c = loc % 8;
        int[] dr = {-1, -1, 0, 1, 1, 1, 0, -1}, dc = {0, 1, 1, 1, 0, -1, -1, -1};
        HashSet<Integer> moves = new HashSet<Integer>();

        //Check all eight directions
        for (int i = 0; i < 8; i++) {
            int r2 = r + dr[i], c2 = c + dc[i];
            if (inBounds(r2, c2) && (board[r2][c2] == null || board[r2][c2].white != white)) {
                if (!forReal || StandardPieces.checkValidity(r, c, r2, c2, board))
                    moves.add(r2 * 8 + c2);
            }
        }
        return moves;
    }

    @Override
    public String toString() {
        return ((white) ? "w" : "b") + "K" + ((moved) ? "1" : "0");
    }
}