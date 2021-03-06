
import java.util.ArrayList;
import java.util.HashSet;

public class ChessLogic {

    //Base Piece Class
    //ChessLogic.Piece piece; import static whatever.something.ChessLogic.*;
    //This is a Christian logic class, so please, NO swearing!
    public static abstract class Piece {

        public boolean white;

        public Piece(boolean isWhite) {
            white = isWhite;
        }

        public abstract int getType();

        public abstract HashSet<Integer> getMoves(int loc, Piece[][] board, boolean forReal);

        public HashSet<Integer> getMoves(int loc, Piece[][] board) {
            return getMoves(loc, board, true);
        }

        boolean inBounds(int r, int c) {
            return r >= 0 && r < 8 && c >= 0 && c < 8;
        }

        abstract Piece copy();
    }

    public static boolean movePiece(int r1, int c1, int r2, int c2, Piece[][] board) {
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

    //Pawn
    public static class Pawn extends Piece {

        boolean movedTwiceLastTurn;

        public Pawn(boolean isWhite) {
            super(isWhite);
        }

        public int getType(){
            return 0;
        }

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
                if (!forReal || checkValidity(r, c, r + dr, c, board))
                    moves.add((r + dr) * 8 + c);

                //Pawns can move two squares on their first turn.
                if (r == start) {
                    if (inBounds(r + 2 * dr, c) && board[r + 2 * dr][c] == null) {
                        if (!forReal || checkValidity(r, c, r + 2 * dr, c, board))
                            moves.add((r + 2 * dr) * 8 + c);
                    }
                }
            }

            //Check if it can capture
            if (inBounds(r + dr, c - 1) && board[r + dr][c - 1] != null && board[r + dr][c - 1].white != white) {
                if (!forReal || checkValidity(r, c, r + dr, c - 1, board))
                    moves.add((r + dr) * 8 + c - 1);
            }
            if (inBounds(r + dr, c + 1) && board[r + dr][c + 1] != null && board[r + dr][c + 1].white != white) {
                if (!forReal || checkValidity(r, c, r + dr, c + 1, board))
                    moves.add((r + dr) * 8 + c + 1);
            }

            //en Passant.
            if (inBounds(r + dr, c - 1) && board[r + dr][c - 1] == null && board[r][c - 1] instanceof Pawn && board[r][c - 1].white != board[r][c].white) {
                Pawn p = (Pawn) (board[r][c - 1]);
                if (p.movedTwiceLastTurn) {
                    if (!forReal || checkValidity(r, c, r + dr, c - 1, board))
                        moves.add((r + dr) * 8 + c - 1);
                }
            }
            if (inBounds(r + dr, c + 1) && board[r + dr][c + 1] == null && board[r][c + 1] instanceof Pawn && board[r][c + 1].white != board[r][c].white) {
                Pawn p = (Pawn) (board[r][c + 1]);
                if (p.movedTwiceLastTurn) {
                    if (!forReal || checkValidity(r, c, r + dr, c + 1, board))
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

    //King
    public static class King extends Piece {
        boolean moved;

        public King(boolean isWhite) {
            super(isWhite);
        }

        public int getType(){
            return 5;
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
            System.out.println("KING GET MOVES");
            int r = loc / 8, c = loc % 8;
            int[] dr = {-1, -1, 0, 1, 1, 1, 0, -1}, dc = {0, 1, 1, 1, 0, -1, -1, -1};
            HashSet<Integer> moves = new HashSet<Integer>();

            //Check all eight directions
            for (int i = 0; i < 8; i++) {
                int r2 = r + dr[i], c2 = c + dc[i];
                if (inBounds(r2, c2) && (board[r2][c2] == null || board[r2][c2].white != white)) {
                    if (!forReal || checkValidity(r, c, r2, c2, board))
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

    //Knight
    public static class Knight extends Piece {
        public Knight(boolean isWhite) {
            super(isWhite);
        }

        public int getType(){
            return 2;
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
                    if (!forReal || checkValidity(r, c, r2, c2, board))
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

    //Rook
    public static class Rook extends Piece {
        boolean moved;

        public Rook(boolean isWhite) {
            super(isWhite);
        }

        public int getType(){
            return 1;
        }

        public Piece copy() {
            Rook cpy = new Rook(this.white);
            cpy.moved = this.moved;
            return cpy;
        }

        public HashSet<Integer> getMoves(int loc, Piece[][] board, boolean forReal) {
            System.out.println("ROOK GET MOVES");
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
                        if (!forReal || checkValidity(r, c, r2, c2, board))
                            moves.add(r2 * 8 + c2);
                        continue;
                    }
                    //Capture
                    if (board[r2][c2].white != white) {
                        if (!forReal || checkValidity(r, c, r2, c2, board))
                            moves.add(r2 * 8 + c2);
                        break;
                    }

                    //This is the horrible stuff I have to do for castling.
                    if (moved || !(board[r2][c2] instanceof King))
                        break;
                    King king = (King) (board[r2][c2]);
                    if (!king.moved) {
                        if (c2 > c) {
                            if (canCastle(r, c2, 2, c, 3, board) && (!forReal || checkValidity(r, c, r2, c2, board)))
                                moves.add(r2 * 8 + c2);
                        } else {
                            try {
                                if (canCastle(r, c2, 6, c, 5, board) && (!forReal || checkValidity(r, c, r2, c2, board)))
                                    moves.add(r2 * 8 + c2);
                            }
                            catch (Exception exc)
                            {
                                System.out.println("r = " + r + ", c = " + c + ", r2 = " + r2 + ", c2 = " + c2);
                                System.out.println("board[r2][c2] = " + board[r2][c2]);
                                System.out.println("board[r][c] = " + board[r][c]);
                                System.out.println("board[r][c2] = " + board[r][c2]);

                                throw exc;
                            }
                        }
                    }

                    //If there was a Piece here, the rook can't go any further in this direction.
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

    //Bishop
    public static class Bishop extends Piece {
        public Bishop(boolean isWhite) {
            super(isWhite);
        }

        public int getType(){
            return 3;
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
                        if (!forReal || checkValidity(r, c, r2, c2, board))
                            moves.add(r2 * 8 + c2);
                        continue;
                    }
                    //Capture
                    if (board[r2][c2].white != white) {
                        if (!forReal || checkValidity(r, c, r2, c2, board))
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

    //Queen
    public static class Queen extends Piece {
        public Queen(boolean isWhite) {
            super(isWhite);
        }

        public int getType(){
            return 4;
        }

        public Piece copy() {
            return new Queen(this.white);
        }

        //This is basically a rook with a king's dr/dc array.
        public HashSet<Integer> getMoves(int loc, Piece[][] board, boolean forReal) {
            int r = loc / 8, c = loc % 8;
            int[] dr = {-1, -1, 0, 1, 1, 1, 0, -1}, dc = {0, 1, 1, 1, 0, -1, -1, -1};
            HashSet<Integer> moves = new HashSet<Integer>();
            for (int i = 0; i < 8; i++) {
                int r2 = r, c2 = c;
                for (int j = 0; j < 8; j++) {
                    r2 += dr[i];
                    c2 += dc[i];
                    if (!inBounds(r2, c2))
                        break;
                    if (board[r2][c2] == null) {
                        if (!forReal || checkValidity(r, c, r2, c2, board))
                            moves.add(r2 * 8 + c2);
                        continue;
                    }
                    //Capture
                    if (board[r2][c2].white != white) {
                        if (!forReal || checkValidity(r, c, r2, c2, board))
                            moves.add(r2 * 8 + c2);
                    }
                    break;
                }
            }
            return moves;
        }

        @Override
        public String toString() {
            return ((white) ? "w" : "b") + "Q";
        }
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

    static boolean checkValidity(int r, int c, int r2, int c2, Piece[][] board) {
        System.out.println("CHECK VALIDITY");
        // Cloning added by Philip 4/19 to fix rook deletion
        Piece[][] boardClone = new Piece[board.length][board[0].length];
        for(int i = 0; i < board.length; i++)
            for(int j = 0; j < board[i].length; j++)
                boardClone[i][j] = board[i][j] == null ? null : board[i][j].copy();
        board = boardClone;

        boolean good = false;
//        boolean color=board[r][c].white;
//        movePiece(r,c,r2,c2,board);
//        if(!inCheck(color,board));
//        good=true;
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

    //TODO figure out how to not delete the rook.
    static boolean canCastle(int r, int c1, int c2, int rookc, int rookend, Piece[][] board) {
        System.out.println("CAN CASTLE CALLED");
        // Cloning added by Philip 4/19 to fix rook deletion
        Piece[][] boardClone = new Piece[board.length][board[0].length];
        for(int i = 0; i < board.length; i++)
            for(int j = 0; j < board[i].length; j++)
                boardClone[i][j] = board[i][j];
        board = boardClone;

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

    static boolean inCheck(boolean color, Piece[][] board) {
        System.out.println("IN CHECK");
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

    //One for checkmate, two for draw, zero for neither
    public static int gameOver(boolean color, Piece[][] board) {
        System.out.println("GAME OVER");
        boolean check = inCheck(color, board);
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

    public static Piece[][] makeBasicChessBoard(){
        Piece[][] board=new Piece[8][8];
        for(int i=0;i<8;i++){
            board[1][i]=new Pawn(false);
            board[6][i]=new Pawn(true);
        }
        //Black
        board[0][0]=new Rook(false);
        board[0][1]=new Knight(false);
        board[0][2]=new Bishop(false);
        board[0][3]=new Queen(false);
        board[0][4]=new King(false);
        board[0][5]=new Bishop(false);
        board[0][6]=new Knight(false);
        board[0][7]=new Rook(false);

        //White
        board[7][0]=new Rook(true);
        board[7][1]=new Knight(true);
        board[7][2]=new Bishop(true);
        board[7][3]=new Queen(true);
        board[7][4]=new King(true);
        board[7][5]=new Bishop(true);
        board[7][6]=new Knight(true);
        board[7][7]=new Rook(true);

        return board;
    }

    public static Piece[][] makePeasantRevoltBoard(){
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

    public static Piece[][] makeRandomizedBoardThatIsAValidChess960Board(){
        Piece[][] board=new Piece[8][8];
        for(int i=0;i<8;i++) {
            board[1][i]=new Pawn(false);
            board[6][i]=new Pawn(true);
        }
        ArrayList<Integer> spotsLeft=new ArrayList<Integer>();
        for(int i=0;i<8;i++)
            spotsLeft.add(i);

        //Bishop on even
        int index=(int)(Math.random()*4)*2;
        board[0][index]=new Bishop(false);
        board[7][index]=new Bishop(true);
        spotsLeft.remove((Object)new Integer(index));

        //Bishop on odd
        index=(int)(Math.random()*4)*2+1;
        board[0][index]=new Bishop(false);
        board[7][index]=new Bishop(true);
        spotsLeft.remove((Object)new Integer(index));

        //Queen
        index=(int)(Math.random()*6);
        index=spotsLeft.get(index);
        board[0][index]=new Queen(false);
        board[7][index]=new Queen(true);
        spotsLeft.remove((Object)new Integer(index));

        //Knights
        index=(int)(Math.random()*5);
        index=spotsLeft.get(index);
        board[0][index]=new Knight(false);
        board[7][index]=new Knight(true);
        spotsLeft.remove((Object)new Integer(index));
        index=(int)(Math.random()*4);
        index=spotsLeft.get(index);
        board[0][index]=new Knight(false);
        board[7][index]=new Knight(true);
        spotsLeft.remove((Object)new Integer(index));

        //Everything else
        board[0][spotsLeft.get(0)]=new Rook(false);
        board[0][spotsLeft.get(1)]=new King(false);
        board[0][spotsLeft.get(2)]=new Rook(false);
        board[7][spotsLeft.get(0)]=new Rook(true);
        board[7][spotsLeft.get(1)]=new King(true);
        board[7][spotsLeft.get(2)]=new Rook(true);

        return board;
    }

}
