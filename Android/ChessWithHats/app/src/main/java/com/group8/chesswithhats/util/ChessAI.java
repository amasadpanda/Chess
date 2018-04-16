package com.group8.chesswithhats.util;

import java.util.HashSet;

import static com.group8.chesswithhats.util.ChessLogic.*;

public class ChessAI {

    //return two ints: the piece and its destination.
    //If you plan to modify board, MAKE A COPY!!
    //Actually maybe don't? It gets reinitialized by the server after each move.
    public static int[] getMove(Piece[][] board, boolean white){

        for(int i=0;i<8;i++){
            for(int j=0;j<8;j++){
                if(board[i][j]!=null && board[i][j].white==white){
                    HashSet<Integer> moves = board[i][j].getMoves(i*8+j,board);
                    if(moves.size()>0){
                        return new int[]{i*8+j,getRandom(moves)};
                    }
                }
            }
        }

        throw new Error("THERE ARE NO MOVES LEFT.\nI AM IN CHECK.\nERROR. ERROR.\nABORT.\nTHE ONLY WINNING MOVE IS NOT TO PLAY.");

    }

    private static int getRandom(HashSet<Integer> set){
        Integer a[] = new Integer[set.size()];
        set.toArray(a);
        return a[(int)Math.random()*set.size()];
    }

}
