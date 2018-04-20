package Rulesets.StandardPieces;

import Rulesets.Piece;

import java.util.HashSet;

public class Knight extends Piece {
    public Knight(boolean isWhite) {
        super(isWhite);
    }

    public Piece copy() {
        return new Knight(this.white);
    }

    public HashSet<Integer> getMoves(int loc, Piece[][] board, boolean forReal) {
        int r = loc / 8, c = loc % 8;
        int[] dr = {-2, -1, 1, 2, 2, 1, -1, -2}, dc = {1, 2, 2, 1, -1, -2, -2, -1};
        HashSet<Integer> moves = new HashSet<Integer>();
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
        return ((white) ? "w" : "b") + "N";
    }
}
