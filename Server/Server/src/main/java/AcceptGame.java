import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class AcceptGame extends FireEater {

    @Override
    public CWHResponse handle(CWHRequest request) {
        String UID = request.getExtras().get("uid");
        String gameID = request.getExtras().get("game");

        DatabaseReference ref = FireEater.getDatabase().getReference();
        DatabaseReference userRef = ref.child("users").child(UID).child("game_invitations").child(gameID);

        SynchronousListener s = new SynchronousListener();
        userRef.addListenerForSingleValueEvent(s);
        DataSnapshot gameValid = s.getSnapshot();
        if(gameValid.getValue() == null)
            return new CWHResponse("No game invitation to accept", false);
        // move from game_invitations to games
        userRef.removeValueAsync();
        userRef = ref.child("users").child(UID).child("current_games");
        Map<String, Object> updateGameList = new HashMap<>();
        updateGameList.put(gameID, "placeholder");
        userRef.updateChildrenAsync(updateGameList);
        return new CWHResponse("Accepted game request", true);
    }
}
