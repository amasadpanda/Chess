import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public void setGametype(String game)
    {
        gametype = game;
        // set starting board config here
        if(game.contains("960"))
        {
            board = toHashMap(makeRandomizedBoardThatIsAValidChess960Board());
        }
        else if(game.contains("Revolt"))
        {
            board = toHashMap(makePeasantRevoltBoard());
        }
        else
        {
            board = toHashMap(makeBasicChessBoard());
        }


    }

    public static ChessLogic.Piece[][] makeBasicChessBoard(){
        ChessLogic.Piece[][] board=new ChessLogic.Piece[8][8];
        for(int i = 0;i<8;i++){
            board[1][i]=new ChessLogic.Pawn(false);
            board[6][i]=new ChessLogic.Pawn(true);
        }
        //Black
        board[0][0]=new ChessLogic.Rook(false);
        board[0][1]=new ChessLogic.Knight(false);
        board[0][2]=new ChessLogic.Bishop(false);
        board[0][3]=new ChessLogic.Queen(false);
        board[0][4]=new ChessLogic.King(false);
        board[0][5]=new ChessLogic.Bishop(false);
        board[0][6]=new ChessLogic.Knight(false);
        board[0][7]=new ChessLogic.Rook(false);

        //White
        board[7][0]=new ChessLogic.Rook(true);
        board[7][1]=new ChessLogic.Knight(true);
        board[7][2]=new ChessLogic.Bishop(true);
        board[7][3]=new ChessLogic.Queen(true);
        board[7][4]=new ChessLogic.King(true);
        board[7][5]=new ChessLogic.Bishop(true);
        board[7][6]=new ChessLogic.Knight(true);
        board[7][7]=new ChessLogic.Rook(true);

        return board;
    }

    public static ChessLogic.Piece[][] makePeasantRevoltBoard(){
        ChessLogic.Piece[][] board=new ChessLogic.Piece[8][8];
        for(int i=0;i<8;i++)
            board[6][i]=new ChessLogic.Pawn(true);
        board[7][4]=new ChessLogic.King(true);

        board[0][1]=new ChessLogic.Knight(false);
        board[0][2]=new ChessLogic.Knight(false);
        board[0][4]=new ChessLogic.King(false);
        board[0][6]=new ChessLogic.Knight(false);
        board[1][4]=new ChessLogic.Pawn(false);

        return board;
    }

    public static ChessLogic.Piece[][] makeRandomizedBoardThatIsAValidChess960Board(){
        ChessLogic.Piece[][] board=new ChessLogic.Piece[8][8];
        for(int i=0;i<8;i++) {
            board[1][i]=new ChessLogic.Pawn(false);
            board[6][i]=new ChessLogic.Pawn(true);
        }
        ArrayList<Integer> spotsLeft=new ArrayList<Integer>();
        for(int i=0;i<8;i++)
            spotsLeft.add(i);

        //Bishop on even
        int index=(int)(Math.random()*4)*2;
        board[0][index]=new ChessLogic.Bishop(false);
        board[7][index]=new ChessLogic.Bishop(true);
        spotsLeft.remove((Object)new Integer(index));

        //Bishop on odd
        index=(int)(Math.random()*4)*2+1;
        board[0][index]=new ChessLogic.Bishop(false);
        board[7][index]=new ChessLogic.Bishop(true);
        spotsLeft.remove((Object)new Integer(index));

        //Queen
        index=(int)(Math.random()*6);
        index=spotsLeft.get(index);
        board[0][index]=new ChessLogic.Queen(false);
        board[7][index]=new ChessLogic.Queen(true);
        spotsLeft.remove((Object)new Integer(index));

        //Knights
        index=(int)(Math.random()*5);
        index=spotsLeft.get(index);
        board[0][index]=new ChessLogic.Knight(false);
        board[7][index]=new ChessLogic.Knight(true);
        spotsLeft.remove((Object)new Integer(index));
        index=(int)(Math.random()*4);
        index=spotsLeft.get(index);
        board[0][index]=new ChessLogic.Knight(false);
        board[7][index]=new ChessLogic.Knight(true);
        spotsLeft.remove((Object)new Integer(index));

        //Everything else
        board[0][spotsLeft.get(0)]=new ChessLogic.Rook(false);
        board[0][spotsLeft.get(1)]=new ChessLogic.King(false);
        board[0][spotsLeft.get(2)]=new ChessLogic.Rook(false);
        board[7][spotsLeft.get(0)]=new ChessLogic.Rook(true);
        board[7][spotsLeft.get(1)]=new ChessLogic.King(true);
        board[7][spotsLeft.get(2)]=new ChessLogic.Rook(true);

        return board;
    }

    public static HashMap<String, String> toHashMap(ChessLogic.Piece[][] board)
    {
        HashMap<String, String> map = new HashMap<>();
        for(int r = 0; r < board.length; r++)
        {
            for(int c = 0; c < board[0].length; c++)
            {
                if(board[r][c] != null)
                    map.put("x"+(r*8+c), board[r][c].toString());
            }
        }
        return map;
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
