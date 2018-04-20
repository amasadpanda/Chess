package Rulesets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public abstract class Ruleset {

    private transient static HashMap<String, Ruleset> getRuleset = new HashMap<>();

    transient String name;
    transient int rL, cL;

    public Ruleset(String name, int r, int c)
    {
        getRuleset.put(name, this);
        rL = r;
        cL = c;
    }

    public abstract Piece[][] getStartState();

    public abstract int gameOver(Piece[][] board);

    public abstract Piece[][] toPieceArray(Map<String, String> map);

    public abstract boolean movePiece(int r1, int c1, int r2, int c2, Piece[][] board, Piece promoteTo);

    public abstract Piece getPiece(String piece);

    public static Map<String, String> getStartStateHashMap(Piece[][] board)
    {
        Map<String, String> map = new HashMap<>();
        for(int r = 0; r < board.length; r++)
        {
            for(int c = 0; c < board[0].length; c++)
            {
                if(board[r][c] != null)
                    map.put("x"+((r*board.length)+c), board[r][c].toString());
            }
        }
        return map;
    }

    public int getR(int i)
    {
            return i/rL;
    }

    public int getC(int i)
    {
        return i%cL;
    }

    public static Ruleset get(String name)
    {
        return getRuleset.get(name);
    }

    public static void init()
    {
        new Chess();
        new Chess960();
        new PeasantsRevolt();
        new Transformers();
    }
}