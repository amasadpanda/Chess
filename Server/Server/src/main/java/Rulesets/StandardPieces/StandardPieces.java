package Rulesets.StandardPieces;

import Rulesets.Piece;

import java.util.HashSet;

public class StandardPieces {

    public static boolean checkValidity(int r, int c, int r2, int c2, Piece[][] board) {
        boolean good = false;

        //Castling
        if (board[r2][c2] != null && board[r][c].white == board[r2][c2].white) {
            Rook rook = (Rook) (board[r][c]);
            King king = (King) (board[r2][c2]);
            rook.moved = true;
            king.moved = true;
            board[r][c] = null;
            board[r2][c2] = null;
            int nextc = 0, nextrc = 0;
            if (c < c2) {
                board[r2][2] = king;
                board[r][3] = rook;
                nextc = 2;
                nextrc = 3;
            } else {
                board[r2][6] = king;
                board[r2][5] = rook;
                nextc = 6;
                nextrc = 5;
            }
            if (!inCheck(board[r][nextrc].white, board))
                good = true;
            board[r2][nextc] = null;
            board[r][nextrc] = null;
            board[r][c] = rook;
            board[r2][c2] = king;
        }
        //en Passant
        else if (board[r][c] instanceof Pawn && board[r2][c2] == null && c != c2) {
            Pawn temp = (Pawn) (board[r][c2]);
            board[r2][c2] = board[r][c];
            board[r][c2] = null;
            board[r][c] = null;
            if (!inCheck(board[r2][c2].white, board))
                good = true;
            board[r][c2] = temp;
            board[r][c] = board[r2][c2];
            board[r2][c2] = null;
        } else {
            Piece temp = board[r2][c2];
            board[r2][c2] = board[r][c];
            board[r][c] = null;
            if (!inCheck(board[r2][c2].white, board))
                good = true;
            board[r][c] = board[r2][c2];
            board[r2][c2] = temp;
        }
        return good;
    }

    public static boolean inCheck(boolean color, Piece[][] board) {
        int loc = findKing(color, board);
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] != null && board[i][j].white != color) {
                    HashSet<Integer> moves = board[i][j].getMoves(i * 8 + j, board, false);
                    if (moves.contains(loc))
                        return true;
                }
            }
        }
        return false;
    }

    static public int findKing(boolean color, Piece[][] board) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] != null && board[i][j] instanceof King && board[i][j].white == color)
                    return i * 8 + j;
            }
        }
        return -1;
    }

    static boolean canCastle(int r, int c1, int c2, int rookc, int rookend, Piece[][] board) {
        King king = (King) (board[r][c1]);
        Rook rook = (Rook) (board[r][rookc]);
        board[r][rookc] = null;
        boolean possible = true;
        if (rookc < rookend) {
            for (int i = rookc + 1; i <= rookend; i++) {
                if (board[r][i] != null && !(board[r][i] instanceof King))
                    return false;
            }
        } else {
            for (int i = rookc - 1; i >= rookend; i--) {
                if (board[r][i] != null && !(board[r][i] instanceof King))
                    return false;
            }
        }
        if (c2 < c1) {
            for (int i = c1; i >= c2; i--) {
                if (board[r][i] != null && !(board[r][i] instanceof King)) {
                    possible = false;
                    break;
                }
                board[r][i] = king;
                if (inCheck(king.white, board)) {
                    possible = false;
                }
                board[r][i] = null;
            }
        } else {
            for (int i = c1; i <= c2; i++) {
                if (board[r][i] != null && !(board[r][i] instanceof King)) {
                    possible = false;
                    break;
                }
                board[r][i] = king;
                if (inCheck(king.white, board)) {
                    possible = false;
                }
                board[r][i] = null;
            }
        }
        board[r][c1] = king;
        board[r][rookc] = rook;
        return possible;
    }

    public static Piece getPiece(String s)
    {
        if(s == null)
            return null;
        boolean b = (s.charAt(0) == 'w');
        switch(s.charAt(1))
        {
            case 'P': {
                boolean move = (s.charAt(2) == '1');
                return new Pawn(b, move);
            }
            case 'N': return new Knight(b);
            case 'B': return new Bishop(b);
            case 'R': return new Rook(b);
            case 'Q': return new Queen(b);
            case 'K': {
                boolean move = (s.charAt(2) == '1');
                return new King(b, move);
            }
        }
        return null;
    }

}

