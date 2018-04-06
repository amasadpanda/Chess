import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

/*
currently assumes that all information gotten is valid
the passing class should check it
 */

public class AcceptFriend extends FireEater {


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
            return new CWHResponse("No friend invitation to accept", false);
        userRef.removeValueAsync();

        userRef = ref.child("users").child(UID).child("friends");
        Map<String, Object> updateFriendList = new HashMap<>();
        updateFriendList.put(friendUID, friendUser);
        userRef.updateChildrenAsync(updateFriendList);
        return new CWHResponse("Accepted friend request from " + friendUser, true);
    }
}
