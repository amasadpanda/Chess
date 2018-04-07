import java.util.HashSet;

public class ChessLogic {

    //Base Piece Class
    //ChessLogic.Piece piece; import static whatever.something.ChessLogic.*;
    //This is a Christian logic class, so please, NO swearing!
    public static abstract class Piece {
        boolean white;
        public Piece(boolean isWhite) {
            white=isWhite;
        }
        abstract HashSet<Integer> getMoves(int loc, Piece[][] board, boolean forReal);
        boolean inBounds(int r,int c) {
            return r>=0&&r<8&&c>=0&&c<8;
        }
    }

    public static void movePiece(int r1,int c1,int r2,int c2,Piece[][] board) {
        //All pawns that moved twice last turn, didn't move twice this turn.
        for(int i=0;i<8;i++) {
            for(int j=0;j<8;j++) {
                if(board[i][j]!=null&&board[i][j] instanceof Pawn) {
                    Pawn p=(Pawn)(board[i][j]);
                    p.movedTwiceLastTurn=false;
                }
            }
        }

        //Check if a pawn moved twice this turn.
        if(board[r1][c1] instanceof Pawn&&Math.abs(r2-r1)==2) {
            Pawn p=(Pawn)(board[r1][c1]);
            p.movedTwiceLastTurn=true;
        }

        //Castling
        if(board[r2][c2]!=null&&board[r1][c1].white==board[r2][c2].white) {
            Rook rook=(Rook)(board[r1][c1]);
            King king=(King)(board[r2][c2]);
            board[r1][c1]=null;
            board[r2][c2]=null;
            rook.moved=true;
            king.moved=true;
            if(c1<c2) {
                board[r2][2]=king;
                board[r2][3]=rook;
            }
            else {
                board[r2][6]=king;
                board[r2][5]=rook;
            }
            return;
        }

        //en Passant
        if(board[r1][c1] instanceof Pawn&&board[r2][c2]==null&&c2!=c1)
            board[r1][c2]=null;

        board[r2][c2]=board[r1][c1];
        board[r1][c1]=null;

        if(board[r2][c2] instanceof King) {
            King king=(King)(board[r2][c2]);
            king.moved=true;
        }
        if(board[r2][c2] instanceof Rook) {
            Rook rook=(Rook)(board[r2][c2]);
            rook.moved=true;
        }
    }

    //Pawn
    public static class Pawn extends Piece {
        boolean movedTwiceLastTurn;
        public Pawn(boolean isWhite) {
            super(isWhite);
        }
        HashSet<Integer> getMoves(int loc, Piece[][] board,boolean forReal){
            HashSet<Integer> moves=new HashSet<Integer>();
            int r=loc/8,c=loc%8;

            //If the Pawn is white, it starts on row 6 and goes up (down? I hate that convention.), otherwise, it starts on row 1 and goes down.
            int dr=-1,start=6;
            if(!white) {
                dr=1;
                start=1;
            }
            if(inBounds(r+dr,c)&&board[r+dr][c]==null) {
                if(!forReal||checkValidity(r,c,r+dr,c,board))
                    moves.add((r+dr)*8+c);

                //Pawns can move two squares on their first turn.
                if(r==start) {
                    if(inBounds(r+2*dr,c)&&board[r+2*dr][c]==null) {
                        if(!forReal||checkValidity(r,c,r+2*dr,c,board))
                            moves.add((r+2*dr)*8+c);
                    }
                }
            }

            //Check if it can capture
            if(inBounds(r+dr,c-1)&&board[r+dr][c-1]!=null&&board[r+dr][c-1].white!=white) {
                if(!forReal||checkValidity(r,c,r+dr,c-1,board))
                    moves.add((r+dr)*8+c-1);
            }
            if(inBounds(r+dr,c+1)&&board[r+dr][c+1]!=null&&board[r+dr][c+1].white!=white) {
                if(!forReal||checkValidity(r,c,r+dr,c+1,board))
                    moves.add((r+dr)*8+c+1);
            }

            //en Passant.
            if(inBounds(r+dr,c-1)&&board[r+dr][c-1]==null&&board[r][c-1] instanceof Pawn) {
                Pawn p=(Pawn)(board[r][c-1]);
                if(p.movedTwiceLastTurn) {
                    if(!forReal||checkValidity(r,c,r+dr,c-1,board))
                        moves.add((r+dr)*8+c-1);
                }
            }
            if(inBounds(r+dr,c+1)&&board[r+dr][c+1]==null&&board[r][c+1] instanceof Pawn) {
                Pawn p=(Pawn)(board[r][c+1]);
                if(p.movedTwiceLastTurn) {
                    if(!forReal||checkValidity(r,c,r+dr,c+1,board))
                        moves.add((r+dr)*8+c+1);
                }
            }

            return moves;
        }
    }

    //King
    public static class King extends Piece {
        boolean moved;
        public King(boolean isWhite) {
            super(isWhite);
        }
        HashSet<Integer> getMoves(int loc,Piece[][] board,boolean forReal) {
            int r=loc/8,c=loc%8;
            int[] dr= {-1,-1,0,1,1,1,0,-1},dc= {0,1,1,1,0,-1,-1,-1};
            HashSet<Integer> moves=new HashSet<Integer>();

            //Check all eight directions
            for(int i=0;i<8;i++) {
                int r2=r+dr[i],c2=c+dc[i];
                if(inBounds(r2,c2)&&(board[r2][c2]==null||board[r2][c2].white!=white)) {
                    if(!forReal||checkValidity(r,c,r2,c2,board))
                        moves.add(r2*8+c2);
                }
            }
            return moves;
        }
    }

    //Knight
    public static class Knight extends Piece {
        public Knight(boolean isWhite) {
            super(isWhite);
        }
        HashSet<Integer> getMoves(int loc,Piece[][] board,boolean forReal){
            int r=loc/8,c=loc%8;
            int[] dr= {-2,-1,1,2,2,1,-1,-2}, dc= {1,2,2,1,-1,-2,-2,-1};
            HashSet<Integer> moves=new HashSet<Integer>();
            for(int i=0;i<8;i++) {
                int r2=r+dr[i],c2=c+dc[i];
                if(inBounds(r2,c2)&&(board[r2][c2]==null||board[r2][c2].white!=white)) {
                    if(!forReal||checkValidity(r,c,r2,c2,board))
                        moves.add(r2*8+c2);
                }
            }
            return moves;
        }
    }

    //Rook
    public static class Rook extends Piece {
        boolean moved;
        public Rook(boolean isWhite) {
            super(isWhite);
        }
        HashSet<Integer> getMoves(int loc,Piece[][] board,boolean forReal) {
            int r=loc/8,c=loc%8;
            int[] dr= {-1,0,1,0},dc= {0,1,0,-1};
            HashSet<Integer> moves=new HashSet<Integer>();
            for(int i=0;i<4;i++) {
                int r2=r,c2=c;
                //Trying going as far as possible in each direction
                for(int j=0;j<8;j++) {
                    r2+=dr[i];
                    c2+=dc[i];
                    if(!inBounds(r2,c2))
                        break;
                    if(board[r2][c2]==null) {
                        if(!forReal||checkValidity(r,c,r2,c2,board))
                            moves.add(r2*8+c2);
                        continue;
                    }
                    //Capture
                    if(board[r2][c2].white!=white) {
                        if(!forReal||checkValidity(r,c,r2,c2,board))
                            moves.add(r2*8+c2);
                        break;
                    }

                    //This is the horrible stuff I have to do for castling.
                    if(moved||!(board[r2][c2] instanceof King))
                        break;
                    King king=(King)(board[r2][c2]);
                    if(!king.moved) {
                        if(c2>c) {
                            if(canCastle(r,c2,2,c,3,board)&&(!forReal||checkValidity(r,c,r2,c2,board)))
                                moves.add(r2*8+c2);
                        }
                        else {
                            if(canCastle(r,c2,6,c,5,board)&&(!forReal||checkValidity(r,c,r2,c2,board)))
                                moves.add(r2*8+c2);
                        }
                    }

                    //If there was a Piece here, the rook can't go any further in this direction.
                    break;
                }
            }
            return moves;
        }
    }

    //Bishop
    public static class Bishop extends Piece {
        public Bishop(boolean isWhite) {
            super(isWhite);
        }
        HashSet<Integer> getMoves(int loc,Piece[][] board,boolean forReal){
            int r=loc/8,c=loc%8;
            int[] dr= {-1,1,1,-1},dc= {1,1,-1,-1};
            HashSet<Integer> moves=new HashSet<Integer>();
            for(int i=0;i<4;i++) {
                int r2=r,c2=c;
                //Trying going as far as possible in each direction
                for(int j=0;j<8;j++) {
                    r2+=dr[i];
                    c2+=dc[i];
                    if(!inBounds(r2,c2))
                        break;
                    if(board[r2][c2]==null) {
                        if(!forReal||checkValidity(r,c,r2,c2,board))
                            moves.add(r2*8+c2);
                        continue;
                    }
                    //Capture
                    if(board[r2][c2].white!=white) {
                        if(!forReal||checkValidity(r,c,r2,c2,board))
                            moves.add(r2*8+c2);
                    }

                    break;
                }
            }
            return moves;
        }
    }

    //Queen
    public static class Queen extends Piece {
        public Queen(boolean isWhite) {
            super(isWhite);
        }

        //This is basically a rook with a king's dr/dc array.
        HashSet<Integer> getMoves(int loc,Piece[][] board,boolean forReal){
            int r=loc/8,c=loc%8;
            int[] dr= {-1,-1,0,1,1,1,0,-1},dc= {0,1,1,1,0,-1,-1,-1};
            HashSet<Integer> moves=new HashSet<Integer>();
            for(int i=0;i<8;i++) {
                int r2=r,c2=c;
                for(int j=0;j<8;j++) {
                    r2+=dr[i];
                    c2+=dc[i];
                    if(!inBounds(r2,c2))
                        break;
                    if(board[r2][c2]==null) {
                        if(!forReal||checkValidity(r,c,r2,c2,board))
                            moves.add(r2*8+c2);
                        continue;
                    }
                    //Capture
                    if(board[r2][c2].white!=white) {
                        if(!forReal||checkValidity(r,c,r2,c2,board))
                            moves.add(r2*8+c2);
                    }
                    break;
                }
            }
            return moves;
        }
    }


    static public int findKing(boolean color,Piece[][] board) {
        for(int i=0;i<8;i++) {
            for(int j=0;j<8;j++) {
                if(board[i][j]!=null&&board[i][j] instanceof King&&board[i][j].white==color)
                    return i*8+j;
            }
        }
        return -1;
    }

    static boolean checkValidity(int r,int c,int r2,int c2,Piece[][] board) {
        boolean good=false;

        //Castling
        if(board[r2][c2]!=null&&board[r][c].white==board[r2][c2].white) {
            Rook rook=(Rook)(board[r][c]);
            King king=(King)(board[r2][c2]);
            rook.moved=true;
            king.moved=true;
            board[r][c]=null;
            board[r2][c2]=null;
            int nextc=0,nextrc=0;
            if(c<c2) {
                board[r2][2]=king;
                board[r][3]=rook;
                nextc=2;
                nextrc=3;
            }
            else {
                board[r2][6]=king;
                board[r2][5]=rook;
                nextc=6;
                nextrc=5;
            }
            if(!inCheck(board[r][nextrc].white,board))
                good=true;
            board[r2][nextc]=null;
            board[r][nextrc]=null;
            board[r][c]=rook;
            board[r2][c2]=king;
        }
        //en Passant
        else if(board[r][c] instanceof Pawn&&board[r2][c2]==null&&c!=c2) {
            Pawn temp=(Pawn)(board[r][c2]);
            board[r2][c2]=board[r][c];
            board[r][c2]=null;
            board[r][c]=null;
            if(!inCheck(board[r2][c2].white,board))
                good=true;
            board[r][c2]=temp;
            board[r][c]=board[r2][c2];
            board[r2][c2]=null;
        }
        else {
            Piece temp=board[r2][c2];
            board[r2][c2]=board[r][c];
            board[r][c]=null;
            if(!inCheck(board[r2][c2].white,board))
                good=true;
            board[r][c]=board[r2][c2];
            board[r2][c2]=temp;
        }
        return good;
    }

    //TODO figure out how to not delete the rook.
    static boolean canCastle(int r,int c1,int c2,int rookc,int rookend,Piece[][] board) {
        King king=(King)(board[r][c1]);
        Rook rook=(Rook)(board[r][rookc]);
        board[r][rookc]=null;
        boolean possible=true;
        if(rookc<rookend) {
            for(int i=rookc+1;i<=rookend;i++) {
                if(board[r][i]!=null&&!(board[r][i] instanceof King))
                    return false;
            }
        }
        else {
            for(int i=rookc-1;i>=rookend;i--) {
                if(board[r][i]!=null&&!(board[r][i] instanceof King))
                    return false;
            }
        }
        if(c2<c1) {
            for(int i=c1;i>=c2;i--) {
                if(board[r][i]!=null&&!(board[r][i] instanceof King)) {
                    possible=false;
                    break;
                }
                board[r][i]=king;
                if(inCheck(king.white,board)) {
                    possible=false;
                }
                board[r][i]=null;
            }
        }
        else {
            for(int i=c1;i<=c2;i++) {
                if(board[r][i]!=null&&!(board[r][i] instanceof King)) {
                    possible=false;
                    break;
                }
                board[r][i]=king;
                if(inCheck(king.white,board)) {
                    possible=false;
                }
                board[r][i]=null;
            }
        }
        board[r][c1]=king;
        board[r][rookc]=rook;
        return possible;
    }

    static boolean inCheck(boolean color,Piece[][] board) {
        int loc=findKing(color,board);
        for(int i=0;i<8;i++) {
            for(int j=0;j<8;j++) {
                if(board[i][j]!=null&&board[i][j].white!=color) {
                    HashSet<Integer> moves=board[i][j].getMoves(i*8+j, board,false);
                    if(moves.contains(loc))
                        return true;
                }
            }
        }
        return false;
    }

    //One for checkmate, two for draw, zero for neither
    public static int gameOver(boolean color,Piece[][] board) {
        boolean check=inCheck(color,board);
        for(int i=0;i<8;i++) {
            for(int j=0;j<8;j++) {
                if(board[i][j]!=null&&board[i][j].white==color) {
                    if(board[i][j].getMoves(i*8+j,board,true).size()!=0)
                        return 0;
                }
            }
        }
        if(check)
            return 1;
        return 2;
    }
}