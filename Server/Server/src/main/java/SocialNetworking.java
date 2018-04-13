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
        try {
            String senderUsername = request.getExtras().get("username");
            String senderUID = request.getExtras().get("uid");
            String destUsername = request.getExtras().get("friend");
            String destUID = FireEater.usernameToUID(destUsername);

            // Place the request into the destination's friend_invitations with format of UID:username
            FirebaseDatabase.getInstance().getReference().child("users").child(destUID).child("friend_invitations").child(senderUID).setValueAsync(senderUsername).get();
            return new CWHResponse("Friend request sent!", true);
        }
        catch (Exception exc)
        {
            return new CWHResponse("Failed to send friend request: " + exc.getMessage(), false);
        }
        /*
            Old tim code, replaced 4/13/2018 by Philip

            String sendTo = request.getExtras().get("frienduid");
        String UID = request.getExtras().get("uid");
        String username = request.getExtras().get("username");

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
        updateFriendsInv.put(UID, username);
        friend.updateChildrenAsync(updateFriendsInv);

        return new CWHResponse("Request sent to " + friendsUsername, true);
         */
    }
}
