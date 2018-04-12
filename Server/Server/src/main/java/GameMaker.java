import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

/*
    This class will create a new game on invitation. This creates a new game under games with a unique ID generated by Firebase.
    The game invitation will then be added to the other user's game invitations list.
 */
public class GameMaker extends FireEater {

    @Override
    public CWHResponse handle(CWHRequest request) {
        DatabaseReference ref = FireEater.getDatabase().getReference();
        String UID = request.getExtras().get("uid");
        String username = request.getExtras().get("username");
        String invitee = request.getExtras().get("friend");
        String inviteeUID = request.getExtras().get("frienduid");
        String gameType = request.getExtras().get("gametype");

        DatabaseReference inviteeRef = ref.child("users").child(inviteeUID);

        // Creates new game in games list
        DatabaseReference newGame = ref.child("games").push();
        String gameID = newGame.getKey();

        // Adds the game invitation to the invetee's list
        inviteeRef = inviteeRef.child("game_invitations");
        Map<String, Object > addGameInv = new HashMap<>();
        addGameInv.put(gameID, username+";"+gameType);
        inviteeRef.updateChildrenAsync(addGameInv);

        // Adds the game object to the game lists
        Game g = new Game(UID, inviteeUID);
        newGame.setValueAsync(g);
        return new CWHResponse("Invitation sent", true);
    }
}
