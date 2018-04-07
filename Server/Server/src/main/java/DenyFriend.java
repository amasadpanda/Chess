import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

public class DenyFriend extends FireEater {
    @Override
    public CWHResponse handle(CWHRequest request) {
        String UID = request.getExtras().get("uid");
        String friendUID = request.getExtras().get("frienduid");
        String friendUser = request.getExtras().get("friend");

        DatabaseReference ref = FireEater.getDatabase().getReference();
        DatabaseReference userRef = ref.child("users").child(UID).child("friend_invitations").child(friendUID);

        SynchronousListener s = new SynchronousListener();
        userRef.addListenerForSingleValueEvent(s);
        DataSnapshot isFriendRequest = s.getSnapshot();
        if(isFriendRequest.getValue() == null)
            return new CWHResponse("No friend invitation to reject", false);
        userRef.removeValueAsync();
        return new CWHResponse("Rejected friend request from " + friendUser, true);
    }
}
