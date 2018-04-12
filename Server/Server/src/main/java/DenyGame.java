import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

public class DenyGame extends FireEater {
    @Override
    public CWHResponse handle(CWHRequest request) {
        String UID = request.getExtras().get("uid");
        String gameID = request.getExtras().get("gameid");

        DatabaseReference ref = FireEater.getDatabase().getReference();
        DatabaseReference userRef = ref.child("users").child(UID).child("game_invitations").child(gameID);

        SynchronousListener s = new SynchronousListener();
        userRef.addListenerForSingleValueEvent(s);
        DataSnapshot validGame = s.getSnapshot();
        if(validGame.getValue() == null)
            return new CWHResponse("No game invitation to reject", false);
        userRef.removeValueAsync();

        DatabaseReference gameRef = ref.child("games").child(gameID);
        gameRef.removeValueAsync();

        return new CWHResponse("Rejected game request" , true); //validGame.getValue() return friends uid string?
    }
}
