package Rulesets.StandardPieces;

import Rulesets.Piece;
import Rulesets.StandardPieces.King;
import Rulesets.StandardPieces.StandardPieces;

import java.util.HashSet;

public class Rook extends Piece {
    public boolean moved;

    public Rook(boolean isWhite) {
        super(isWhite);

    }

    public Piece copy() {
        Rook cpy = new Rook(this.white);
        cpy.moved = this.moved;
        return cpy;
    }

    public HashSet<Integer> getMoves(int loc, Piece[][] board, boolean forReal) {
        int r = loc / 8, c = loc % 8;
        int[] dr = {-1, 0, 1, 0}, dc = {0, 1, 0, -1};
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
                    break;
                }

                //This is the horrible stuff I have to do for castling.
                if (moved || !(board[r2][c2] instanceof King))
                    break;
                King king = (King) (board[r2][c2]);
                if (!king.moved) {
                    if (c2 > c) {
                        if (StandardPieces.canCastle(r, c2, 2, c, 3, board) && (!forReal || StandardPieces.checkValidity(r, c, r2, c2, board)))
                            moves.add(r2 * 8 + c2);
                    } else {
                        if (StandardPieces.canCastle(r, c2, 6, c, 5, board) && (!forReal || StandardPieces.checkValidity(r, c, r2, c2, board)))
                            moves.add(r2 * 8 + c2);
                    }
                }

                //If there was a Rulesets.Piece here, the rook can't go any further in this direction.
                break;
            }
        }
        return moves;
    }

    @Override
    public String toString() {
        return ((white) ? "w" : "b") + "R";
    }
}