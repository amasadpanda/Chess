package Rulesets;

import Rulesets.StandardPieces.*;

import java.util.Map;

public class PeasantsRevolt extends Ruleset{
    public PeasantsRevolt() {
        super("Peasants' Revolt", 8, 8);
    }

    @Override
    public Piece[][] getStartState() {
        Piece[][] board=new Piece[8][8];
        for(int i=0;i<8;i++)
            board[6][i]=new Pawn(true);
        board[7][4]=new King(true);

        board[0][1]=new Knight(false);
        board[0][2]=new Knight(false);
        board[0][4]=new King(false);
        board[0][6]=new Knight(false);
        board[1][4]=new Pawn(false);

        return board;
    }

    @Override
    /**
     * 1 = white wins
     * -1 = black wins
     * 0 = none
     * 2 = tie
     */
    public int gameOver(Piece[][] board) {
        int whiteWins = check(true, board);
        if(whiteWins == 0)
            return check(true, board);
        else
            return whiteWins;
    }

    @Override
    public Piece[][] toPieceArray(Map<String, String> map) {
        Piece[][] board = new Piece[8][8];
        for(String key : map.keySet())
        {
            String piece = map.get(key);
            Integer loc = Integer.parseInt(key.substring(1));
            int r = getR(loc);
            int c = getC(loc);
            board[r][c] = StandardPieces.getPiece(map.get(key));
        }
        return board;
    }

    private int check(boolean color, Piece[][] board)
    {
        boolean check = StandardPieces.inCheck(color, board);
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] != null && board[i][j].white == color) {
                    if (board[i][j].getMoves(i * 8 + j, board, true).size() != 0)
                        return 0;
                }
            }
        }
        if (check)
            return 1;
        return 2;
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

    @Override
    public Piece getPiece(String piece) {
        return StandardPieces.getPiece(piece);
    }
}
