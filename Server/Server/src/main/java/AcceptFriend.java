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
        String username = request.getExtras().get("username");
        String UID = request.getExtras().get("uid");
        String friendUID = request.getExtras().get("frienduid");
        String friendUser = request.getExtras().get("friend");

        DatabaseReference ref = FireEater.getDatabase().getReference();
        DatabaseReference userRef = ref.child("users").child(UID).child("friend_invitations").child(friendUID);

        // Check to see if the friend request exists
        SynchronousListener s = new SynchronousListener();
        userRef.addListenerForSingleValueEvent(s);
        DataSnapshot isFriendRequest = s.getSnapshot();
        if(isFriendRequest.getValue() == null)
            return new CWHResponse("No friend request to accept!", false);

        // If we're here, it exists, and we can remove it!
        userRef.removeValueAsync();

        // Add the friend to BOTH users' friends lists. (Modified by Philip 4/13/2018 8:29 PM)
        ref.child("users").child(UID).child("friends").child(friendUID).setValueAsync(friendUser);
        ref.child("users").child(friendUID).child("friends").child(UID).setValueAsync(username);
        return new CWHResponse("Accepted friend request from " + friendUser + ".", true);
    }
}
