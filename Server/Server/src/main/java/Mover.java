import Rulesets.Piece;
import Rulesets.Ruleset;
import Rulesets.StandardPieces.StandardPieces;
import com.google.firebase.database.DatabaseReference;

import java.util.*;

public class Mover extends FireEater{

    @Override
    public CWHResponse handle(CWHRequest request) {
        try {

            String gameID = request.getExtras().get("gameid");;
            String promotion = request.getExtras().get("promote");
            int startingPlace = Integer.parseInt(request.getExtras().get("start"));
            int clientMove = Integer.parseInt(request.getExtras().get("end"));

            DatabaseReference gameRef = getDatabase().getReference().child("games").child(gameID);
            SynchronousListener getGame = new SynchronousListener();
            gameRef.addListenerForSingleValueEvent(getGame);

            Game game = getGame.getSnapshot().getValue(Game.class);
            Piece[][] boardstate = game.getBoard();
            int r = getR(startingPlace);
            int c = getC(startingPlace);
            Piece myPiece = boardstate[r][c];
            HashSet<Integer> nextMoves = myPiece.getMoves(startingPlace, boardstate, true);

            // Changed by Philip 4/16/2018 1:56PM. Before you were looking throughe everything in nextMoves to get the same result...
            if (!nextMoves.contains(clientMove))
                return new CWHResponse("Invalid move!", false);

            //pawn promotion


            game.movePiece(r,c, getR(clientMove), getC(clientMove), boardstate, game.getRuleset().getPiece(promotion));
            game.updateHashMap(boardstate);

            // Before game.move may have been null so null would be at the beginning of game moves.. fixed by PHilip 4/16/2018 1:40PM
            String moves = (game.moves == null ? "" : game.moves) + (startingPlace + ">" + clientMove + " ");

            String turn = game.turn;
            turn = (turn.equals("white")?"black":"white");

            Map<String, Object> updateGame = new HashMap<>();
            updateGame.put("board", game.board);
            updateGame.put("moves", moves);
            updateGame.put("black", game.black);
            updateGame.put("white", game.white);
            updateGame.put("turn", turn);
            updateGame.put("gametype", game.gametype); // Line added by Philip 4/16/2018 1:37 PM

            int whoWins = game.gameOver( boardstate);

            if(whoWins > 0)
            {
                if(game.gametype.equals("Ranked Chess"))
                {
                    SynchronousListener getWhite = new SynchronousListener();
                    SynchronousListener getBlack = new SynchronousListener();

                    DatabaseReference whiteRef = FireEater.getDatabase().getReference().child("users").child(game.white).child("rank");
                    DatabaseReference blackRef = FireEater.getDatabase().getReference().child("users").child(game.black).child("rank");

                    blackRef.addListenerForSingleValueEvent(getBlack);
                    whiteRef.addListenerForSingleValueEvent(getWhite);

                    Double white = getWhite.getSnapshot().getValue(Double.class);
                    Double black = getBlack.getSnapshot().getValue(Double.class);

                    Double newElowhite = User.newElo(white, black, (whoWins == 2)?(.5):whoWins);
                    Double newEloBlack = User.newElo(black, white, (whoWins == 2)?(.5):whoWins*-1);

                    whiteRef.setValueAsync(newElowhite);
                    blackRef.setValueAsync(newEloBlack);
                }

                switch(whoWins)
                {
                    case 1: updateGame.put("turn", "winner="+game.white);
                        break;
                    case -1: updateGame.put("turn", "winner="+game.black);
                        break;
                    case 2: updateGame.put("turn", "winner=Nobody!!!");
                        break;
                }
            }

            gameRef.setValueAsync(updateGame);

            return new CWHResponse("Move made!", true);
        }
        catch (Exception exc)
        {
            return new CWHResponse("Failed to make move!", true);
        }
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