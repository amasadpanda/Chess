import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class GameLeaver extends FireEater {
    @Override
    public CWHResponse handle(CWHRequest request) {
        try
        {
            String UID = request.getExtras().get("uid");
            String username = request.getExtras().get("username");
            String gameID = request.getExtras().get("gameid");

            DatabaseReference ref = FireEater.getDatabase().getReference();
            DatabaseReference game = ref.child("games").child(gameID);

            SynchronousListener s = new SynchronousListener();
            game.addListenerForSingleValueEvent(s);
            Game g = s.getSnapshot().getValue(Game.class);

            String other = (!UID.equals(g.white)?g.white:g.black);
            ref.child("users").child(UID).child("current_games").child(gameID).removeValueAsync();
            ref.child("users").child(UID).child("past_games").child(gameID).setValueAsync(other);

            if(!g.turn.startsWith("winner"))
            {
                g.turn = "winner="+other;
                game.setValueAsync(g);
                return new CWHResponse("You left the empty game behind", true);
            }

            game.setValueAsync(g);
            return new CWHResponse("You have left the game, DISHONOR TO YOUR FAMILY", true);
        }
        catch (Exception ex)
        {
            return new CWHResponse("Failed to leave the game, try again!", false);
        }

    }
}
