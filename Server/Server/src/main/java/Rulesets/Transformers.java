package Rulesets;

import Rulesets.StandardPieces.*;

public class Transformers extends Chess {
    public Transformers()
    {
        super("Transformers", 8, 8);
    }

    @Override
    public boolean movePiece(int r1, int c1, int r2, int c2, Piece[][] board, Piece promoteTo) {
        boolean ret=false;
        //All pawns that moved twice last turn, didn't move twice this turn.
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] != null && board[i][j] instanceof Pawn) {
                    Pawn p = (Pawn) (board[i][j]);
                    p.movedTwiceLastTurn = false;
                }
            }
        }

        //Check if a pawn moved twice this turn.
        if (board[r1][c1] instanceof Pawn && Math.abs(r2 - r1) == 2) {
            Pawn p = (Pawn) (board[r1][c1]);
            p.movedTwiceLastTurn = true;
        }

        //Castling
        if (board[r2][c2] != null && board[r1][c1].white == board[r2][c2].white) {
            ret=true;
            Rook rook = (Rook) (board[r1][c1]);
            King king = (King) (board[r2][c2]);
            board[r1][c1] = null;
            board[r2][c2] = null;
            rook.moved = true;
            king.moved = true;
            if (c1 < c2) {
                board[r2][2] = king;
                board[r2][3] = rook;
            } else {
                board[r2][6] = king;
                board[r2][5] = rook;
            }
            return ret; //TODO: Ethan review this, it was just "return;" and i made it "return ret;"
        }

        //en Passant
        if (board[r1][c1] instanceof Pawn && board[r2][c2] == null && c2 != c1){
            ret=true;
            board[r1][c2] = null;
        }

        // pawn promotion
        if(r2 == 0 || r2 == 7 & board[r1][c1] instanceof Pawn)
            board[r1][c1] = promoteTo;

        board[r2][c2] = board[r1][c1];
        board[r1][c1] = null;
        boolean e = board[r2][c2].white;
        Piece[] b = new Piece[5];
        b[0] = new Pawn(e);
        b[1] = new Queen(e);
        b[2] = new Rook(e);
        b[3] = new Knight(e);
        b[4] = new Bishop(e);

        board[r2][c2] = b[(int)Math.random()*6];

        if (board[r2][c2] instanceof King) {
            King king = (King) (board[r2][c2]);
            king.moved = true;
        }
        if (board[r2][c2] instanceof Rook) {
            Rook rook = (Rook) (board[r2][c2]);
            rook.moved = true;
        }

        return ret;
    }
}
