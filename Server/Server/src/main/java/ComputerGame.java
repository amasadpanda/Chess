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

        Map<String, Object> map = new HashMap<>();
        map.put(gameID, "COMPUTER");
        ref.child("users").child(UID).child("current_games").updateChildrenAsync(map);

        newGame.setValueAsync(g);
        return new CWHResponse("Invitation sent", true);
    }
}
