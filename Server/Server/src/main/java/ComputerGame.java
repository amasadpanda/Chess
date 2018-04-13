import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class ComputerGame extends FireEater{
    @Override
    public CWHResponse handle(CWHRequest request) {

        String UID = request.getExtras().get("uid");
        String gametype = request.getExtras().get("gametype");
        DatabaseReference ref = FireEater.getDatabase().getReference();

        // Creates new game in games list
        DatabaseReference newGame = ref.child("games").push();
        String gameID = newGame.getKey();

        // Adds the game object to the game lists
        Game g = new Game(UID, "COMPUTER");
        g.setGametype(gametype);

        newGame.setValueAsync(g);

        // Add the computer game to the user's current games list (added by Philip 4/12/2018 10:30PM)
        Map<String, Object> updateGameList = new HashMap<>();
        updateGameList.put(gameID, "COMPUTER");
        DatabaseReference userRef = ref.child("users").child(UID).child("current_games");
        userRef.updateChildrenAsync(updateGameList);

        return new CWHResponse("Invitation sent", true);
    }
}
