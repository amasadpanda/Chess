package Rulesets.StandardPieces;

import Rulesets.Piece;

import java.util.HashSet;

public class Queen extends Piece {

        public Queen(boolean isWhite){
        super(isWhite);
        }

        public Piece copy(){
            return new Queen(this.white);
        }

//This is basically a rook with a king's dr/dc array.
public HashSet<Integer> getMoves(int loc, Piece[][]board, boolean forReal){
        int r=loc/8,c=loc%8;
        int[]dr={-1,-1,0,1,1,1,0,-1},dc={0,1,1,1,0,-1,-1,-1};
        HashSet<Integer> moves=new HashSet<Integer>();
        for(int i=0;i< 8;i++){
        int r2=r,c2=c;
        for(int j=0;j< 8;j++){
        r2+=dr[i];
        c2+=dc[i];
        if(!inBounds(r2,c2))
        break;
        if(board[r2][c2]==null){
        if(!forReal||StandardPieces.checkValidity(r,c,r2,c2,board))
        moves.add(r2*8+c2);
        continue;
        }
        //Capture
        if(board[r2][c2].white!=white){
        if(!forReal||StandardPieces.checkValidity(r,c,r2,c2,board))
        moves.add(r2*8+c2);
        }
        break;
        }
        }
        return moves;
        }

@Override
public String toString(){
        return((white)?"w":"b")+"Q";
        }
}
