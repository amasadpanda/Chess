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

        // Changed by Philip 4/16/2018 1:56PM. Before you were looking throughe everything in nextMoves to get the same result...
        Boolean isValid = nextMoves.contains(clientMove);

        if(!isValid)
            return new CWHResponse("Invalid move", false);

        // moving the actual board
        if(ChessLogic.movePiece(r, c, getR(clientMove), getC(clientMove), boardstate))
            game.board = Game.toHashMap(boardstate);
        else
        {
            game.board.remove("x" + startingPlace);
            game.board.put("x"+(clientMove), myPiece.toString() );
        }


        // Before game.move may have been null so null would be at the beginning of game moves.. fixed by PHilip 4/16/2018 1:40PM
        String moves = (game.moves == null ? "" : game.moves) + (startingPlace+">"+clientMove+" ");
        String turn = game.turn;
        if(turn.equals("white"))
            turn = "black";
        else
            turn = "white";

        Map<String, Object> updateGame = new HashMap<>();
        updateGame.put("board", game.board);
        updateGame.put("moves", moves);
        updateGame.put("black", game.black);
        updateGame.put("white", game.white);
        updateGame.put("turn", turn);
        updateGame.put("gametype", game.gametype); // Line added by Philip 4/16/2018 1:37 PM

        gameRef.setValueAsync(updateGame);

        return new CWHResponse("Move made", true);
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
