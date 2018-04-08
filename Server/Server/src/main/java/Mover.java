import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.*;

public class Mover extends FireEater{

    @Override
    public CWHResponse handle(CWHRequest request) {

        String gameID = request.getExtras().get("gameid");
        int startingPlace = Integer.parseInt(request.getExtras().get("start"));
        int clientMove = Integer.parseInt(request.getExtras().get("end"));

        DatabaseReference gameRef = getDatabase().getReference().child("games").child(gameID);
        SynchronousListener getGame = new SynchronousListener();
        gameRef.addListenerForSingleValueEvent(getGame);
        Game game = getGame.getSnapshot().getValue(Game.class);


        ChessLogic.Piece[][] boardstate = toPieceArray(game.board);
        int r = getR(startingPlace);
        int c = getC(startingPlace);
        ChessLogic.Piece myPiece = boardstate[r][c];
        HashSet<Integer> nextMoves = myPiece.getMoves(startingPlace, boardstate, true);
        Boolean isValid = false;
        for(Integer i : nextMoves)
        {
            if(clientMove == i)
            {
                isValid = true;
                break;
            }
        }
        if(!isValid)
            return new CWHResponse("Invalid move", false);

        String moves = game.moves + (startingPlace+clientMove+" ");
        game.board.remove(startingPlace);
        game.board.put(Integer.toString(clientMove), myPiece.toString() );

        Map<String, Object> updateGame = new HashMap<>();
        updateGame.put("board", game.board);
        updateGame.put("moves", moves);
        updateGame.put("black", game.black);
        updateGame.put("white", game.white);

        gameRef.setValueAsync(updateGame);

        return null;
    }

    private static ChessLogic.Piece[][] toPieceArray(Map<String, String> map)
    {
        ChessLogic.Piece[][] board = new ChessLogic.Piece[8][8];
        for(String key : map.keySet())
        {
            String piece = map.get(key);
            Integer loc = Integer.parseInt(key);
            int r = getR(loc);
            int c = getC(loc);
            board[r][c] = getPiece(map.get(key));
        }
        return board;
    }

    private static ChessLogic.Piece getPiece(String s)
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

    private static ChessLogic.Piece[][] listToArray(List<List<ChessLogic.Piece>> l)
    {
        return new ChessLogic.Piece[8][8];
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
