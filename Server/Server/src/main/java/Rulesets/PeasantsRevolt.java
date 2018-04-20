package Rulesets;

import Rulesets.StandardPieces.*;

import java.util.Map;

public class PeasantsRevolt extends Chess{
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

}
