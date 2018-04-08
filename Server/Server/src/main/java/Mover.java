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

        ChessLogic.Piece[][] boardstate = Game.toPieceArray(game.board);
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

        game.board.remove("x" + startingPlace);
        game.board.put("x"+(clientMove), myPiece.toString() );

        Map<String, Object> updateGame = new HashMap<>();
        updateGame.put("board", game.board);
        updateGame.put("moves", moves);
        updateGame.put("black", game.black);
        updateGame.put("white", game.white);

        gameRef.setValueAsync(updateGame);

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
