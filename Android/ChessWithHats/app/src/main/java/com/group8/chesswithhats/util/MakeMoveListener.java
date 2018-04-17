package com.group8.chesswithhats.util;

public abstract class MakeMoveListener {
    public boolean makeMove(int start, int end, boolean white){
        return makeMove(start,end,false,white);
    }
    public abstract boolean makeMove(int start, int end, boolean promotion, boolean white);
}
