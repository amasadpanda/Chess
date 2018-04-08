import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game {

    // these are set as UIDs
    public String  black, white;
    public String moves;
    public Map<String, String> board;

    public Game()
    {

    }

    public Game(String UID)
    {
        this();
        int coinToss = (int)Math.random()*2;
        if(coinToss == 1)
            black = UID;
        else
            white = UID;
    }

    public Game(String user1, String user2)
    {
        this();
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


    public static ChessLogic.Piece[][] toPieceArray(Map<String, String> map)
    {
        ChessLogic.Piece[][] board = new ChessLogic.Piece[8][8];
        for(String key : map.keySet())
        {
            String piece = map.get(key);
            Integer loc = Integer.parseInt(key.substring(1));
            int r = getR(loc);
            int c = getC(loc);
            board[r][c] = getPiece(map.get(key));
        }
        return board;
    }

    public static ChessLogic.Piece getPiece(String s)
    {
        boolean b = (s.charAt(0) == 'w');
        switch(s.charAt(1))
        {
            case 'P': {
                boolean move = (s.charAt(2) == '1');
                return new ChessLogic.Pawn(b, move);
            }
            case 'N': return new ChessLogic.Knight(b);
            case 'B': return new ChessLogic.Bishop(b);
            case 'R': return new ChessLogic.Rook(b);
            case 'Q': return new ChessLogic.Queen(b);
            case 'K': {
                boolean move = (s.charAt(2) == '1');
                return new ChessLogic.King(b, move);
            }
        }
        return null;
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
