package Rulesets;

import Rulesets.StandardPieces.*;

import java.util.ArrayList;
import java.util.Map;

public class Chess960 extends Chess {
    public Chess960() {
        super("Chess960", 8, 8);
    }

    @Override
    public Piece[][] getStartState() {
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
