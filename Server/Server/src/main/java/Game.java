import Rulesets.Piece;
import Rulesets.Ruleset;

import java.util.HashMap;
import java.util.Map;

public class Game {

    // these are set as UIDs
    public String  black, white;
    public String moves;
    public String turn = "white";
    public String gametype;
    public Map<String, String> board;

    public Game()
    {

    }

    public Game(String UID)
    {
        int coinToss = (int)Math.random()*2;
        if(coinToss == 1)
            black = UID;
        else
            white = UID;
    }

    public Game(String user1, String user2)
    {
        int coinToss = (int)Math.random()*2;
        if(coinToss == 1)
        {
            black = user1;
            white = user2;
        }
        else
        {
            black = user2;
            white = user1;
        }
    }

    public Ruleset getRuleset()
    {
        if(gametype.equals("Chess") || gametype.equals("Ranked Chess"))
            return Ruleset.get("Chess");
        return Ruleset.get(gametype);
    }

    public void setGametype(String game)
    {
        gametype = game;
        // set starting board config here
        board = toHashMap(getRuleset().getStartState());
    }

    public boolean movePiece(int r1, int c1, int r2, int c2, Piece[][] board, Piece promoteTo)
    {
        return getRuleset().movePiece(r1, c1, r2, c2, board, promoteTo);
    }

    public Piece[][] getBoard()
    {
        return getRuleset().toPieceArray(board);
    }

    public int gameOver(Piece[][] board)
    {
        return getRuleset().gameOver(board);
    }


    public void updateHashMap(Piece[][] board)
    {
        Map<String, String> map = new HashMap<>();
        for(int r = 0; r < board.length; r++)
        {
            for(int c = 0; c < board[0].length; c++)
            {
                if(board[r][c] != null)
                    map.put("x"+(r*8+c), board[r][c].toString());
            }
        }
        this.board = map;
    }

    public static Map<String, String> toHashMap(Piece[][] board)
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


    private static int getR(int loc)
    {
        return loc/8;
    }

    private static int getC(int loc)
    {
        return loc%8;
    }
}