import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.HashMap;
import java.util.Map;

public class AcceptGame extends FireEater {

    @Override
    public CWHResponse handle(CWHRequest request) {
        String UID = request.getExtras().get("uid");
        String gameID = request.getExtras().get("gameid");
        String username = request.getExtras().get("username");

        DatabaseReference ref = FireEater.getDatabase().getReference();

        DatabaseReference userRef = ref.child("users").child(UID).child("game_invitations").child(gameID);
        userRef.removeValueAsync();

       // userRef.addListenerForSingleValueEvent(s);
       // DataSnapshot gameValid = s.getSnapshot();
       // if(gameValid.getValue() == null)
       //     return new CWHResponse("No game invitation to accept", false);
        SynchronousListener s = new SynchronousListener();
        DatabaseReference gameRef = ref.child("games").child(gameID).child("black");
        gameRef.addListenerForSingleValueEvent(s);
        String blackUID = (String)s.getSnapshot().getValue();

        s = new SynchronousListener();
        gameRef = ref.child("games").child(gameID).child("white");
        gameRef.addListenerForSingleValueEvent(s);
        String whiteUID = (String)s.getSnapshot().getValue();
        // move from game_invitations to games

        boolean whichPlayerIs = (whiteUID.equals(UID));
        String otherUserName = (whichPlayerIs)?FireEater.UIDToUsername(blackUID):FireEater.UIDToUsername(whiteUID);

        Map<String, Object> updateGameList = new HashMap<>();
        updateGameList.put(gameID, (whichPlayerIs)?otherUserName:username);
        userRef = ref.child("users").child(whiteUID).child("current_games");
        userRef.updateChildrenAsync(updateGameList);

        updateGameList.put(gameID, (whichPlayerIs)?username:otherUserName);
        userRef = ref.child("users").child(blackUID).child("current_games");
        userRef.updateChildrenAsync(updateGameList);

        return new CWHResponse("Accepted game request", true);
    }
}
