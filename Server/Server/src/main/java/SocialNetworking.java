import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;

/*
   This class takes requests from users for a friend invitation and adds the user to the friend's invitation list.
   It uses the AuthID to return the UID.
 */
public class SocialNetworking extends FireEater{

    /**
     *
     * @param request request.extras needs to contain the key-value pair "username":"" which is the friend to be added
     * @return
     */
    @Override
    public CWHResponse handle(CWHRequest request) {
        String sendTo = request.getExtras().get("frienduid");
        String UID = request.getExtras().get("uid");

        final FirebaseDatabase database = getDatabase();
        DatabaseReference usersRef = database.getReference().child("users");

        DatabaseReference usersPath = usersRef.child(sendTo);
        SynchronousListener getUser = new SynchronousListener();
        usersPath.addListenerForSingleValueEvent(getUser);

        DataSnapshot user = getUser.getSnapshot();
        User u = user.getValue(User.class);

        String friendsUsername = request.getExtras().get("friend");
        DatabaseReference friend = user.getRef().child("friend_invitations");
        Map<String, Object> updateFriendsInv = new HashMap<>();
        updateFriendsInv.put(UID, friendsUsername);
        friend.updateChildrenAsync(updateFriendsInv);

        return new CWHResponse("Request sent to " + friendsUsername, true);

    }
}
