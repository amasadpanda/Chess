package Rulesets;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public abstract class Ruleset {

    private static HashMap<String, Ruleset> getRuleset = new HashMap<>();
    public static ArrayList<String> rulesetList = new ArrayList<>();

    String name;
    int rL, cL;

    public Ruleset(String name, int r, int c)
    {
        this.name = name;
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
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("rulesets");

        rulesetList.add(new Chess().name);
        rulesetList.add(new Chess960().name);
        rulesetList.add(new PeasantsRevolt().name);
        rulesetList.add(new Transformers().name);
        rulesetList.add(new Andernach().name);

        ref.setValueAsync(rulesetList);
    }

    public static ArrayList<String> getListOfRules()
    {
        return rulesetList;
    }
}