package Rulesets.StandardPieces;

import Rulesets.Piece;
import Rulesets.StandardPieces.StandardPieces;

import java.util.HashSet;

public class Bishop extends Piece {
    public Bishop(boolean isWhite) {
        super(isWhite);
    }

    public Piece copy() {
        return new Bishop(this.white);
    }

    public HashSet<Integer> getMoves(int loc, Piece[][] board, boolean forReal) {
        int r = loc / 8, c = loc % 8;
        int[] dr = {-1, 1, 1, -1}, dc = {1, 1, -1, -1};
        HashSet<Integer> moves = new HashSet<Integer>();
        for (int i = 0; i < 4; i++) {
            int r2 = r, c2 = c;
            //Trying going as far as possible in each direction
            for (int j = 0; j < 8; j++) {
                r2 += dr[i];
                c2 += dc[i];
                if (!inBounds(r2, c2))
                    break;
                if (board[r2][c2] == null) {
                    if (!forReal || StandardPieces.checkValidity(r, c, r2, c2, board))
                        moves.add(r2 * 8 + c2);
                    continue;
                }
                //Capture
                if (board[r2][c2].white != white) {
                    if (!forReal || StandardPieces.checkValidity(r, c, r2, c2, board))
                        moves.add(r2 * 8 + c2);
                }

                break;
            }
        }
        return moves;
    }

    @Override
    public String toString() {
        return ((white) ? "w" : "b") + "B";
    }
}