package Rulesets.StandardPieces;

import Rulesets.Piece;

import java.util.HashSet;

public class Pawn extends Piece {
    public boolean movedTwiceLastTurn;

    public Pawn(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public Piece copy() {
        Pawn cpy = new Pawn(this.white);
        cpy.movedTwiceLastTurn = this.movedTwiceLastTurn;
        return cpy;
    }

    public Pawn(boolean isWhite, boolean moved) {
        this(isWhite);
        movedTwiceLastTurn = moved;
    }

    public HashSet<Integer> getMoves(int loc, Piece[][] board, boolean forReal) {
        HashSet<Integer> moves = new HashSet<Integer>();
        int r = loc / 8, c = loc % 8;

        //If the Pawn is white, it starts on row 6 and goes up (down? I hate that convention.), otherwise, it starts on row 1 and goes down.
        int dr = -1, start = 6;
        if (!white) {
            dr = 1;
            start = 1;
        }
        if (inBounds(r + dr, c) && board[r + dr][c] == null) {
            if (!forReal || StandardPieces.checkValidity(r, c, r + dr, c, board))
                moves.add((r + dr) * 8 + c);

            //Pawns can move two squares on their first turn.
            if (r == start) {
                if (inBounds(r + 2 * dr, c) && board[r + 2 * dr][c] == null) {
                    if (!forReal || StandardPieces.checkValidity(r, c, r + 2 * dr, c, board))
                        moves.add((r + 2 * dr) * 8 + c);
                }
            }
        }

        //Check if it can capture
        if (inBounds(r + dr, c - 1) && board[r + dr][c - 1] != null && board[r + dr][c - 1].white != white) {
            if (!forReal || StandardPieces.checkValidity(r, c, r + dr, c - 1, board))
                moves.add((r + dr) * 8 + c - 1);
        }
        if (inBounds(r + dr, c + 1) && board[r + dr][c + 1] != null && board[r + dr][c + 1].white != white) {
            if (!forReal || StandardPieces.checkValidity(r, c, r + dr, c + 1, board))
                moves.add((r + dr) * 8 + c + 1);
        }

        //en Passant.
        if (inBounds(r + dr, c - 1) && board[r + dr][c - 1] == null && board[r][c - 1] instanceof Pawn && board[r][c - 1].white != board[r][c].white) {
            Pawn p = (Pawn) (board[r][c - 1]);
            if (p.movedTwiceLastTurn) {
                if (!forReal || StandardPieces.checkValidity(r, c, r + dr, c - 1, board))
                    moves.add((r + dr) * 8 + c - 1);
            }
        }
        if (inBounds(r + dr, c + 1) && board[r + dr][c + 1] == null && board[r][c + 1] instanceof Pawn && board[r][c + 1].white != board[r][c].white) {
            Pawn p = (Pawn) (board[r][c + 1]);
            if (p.movedTwiceLastTurn) {
                if (!forReal || StandardPieces.checkValidity(r, c, r + dr, c + 1, board))
                    moves.add((r + dr) * 8 + c + 1);
            }
        }

        return moves;
    }

    @Override
    public String toString() {
        return ((white) ? "w" : "b") + "P" + ((movedTwiceLastTurn) ? "1" : "0");
    }
}
