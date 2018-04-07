import java.util.HashSet;

public class Mover extends FireEater{

    @Override
    /**
     * request.extras contains first & second, the starting location and ending location of the move to be made
     */
    public CWHResponse handle(CWHRequest request) {
        String gameID;
        int startingPlace = Integer.parseInt(request.getExtras().get("first"));
        int clientMove = Integer.parseInt(request.getExtras().get("second"));
        ChessLogic.Piece[][] boardstate = new ChessLogic.Piece[8][8];

        ChessLogic.Piece myPiece = boardstate[startingPlace][0];
        HashSet<Integer> moves = myPiece.getMoves(startingPlace, boardstate, true);
        for(Integer i : moves)
        {

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
