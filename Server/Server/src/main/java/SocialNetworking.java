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
        String sendTo = "tim";//request.getExtras().get("username");
        String UID = "phio";

        try {
        //    UID = FireEater.tokenToUID(request.getAuthID());
        } catch (Exception e) {
            e.printStackTrace();
        }

        final FirebaseDatabase database = getDatabase();
        DatabaseReference usersRef = database.getReference().child("users");

        DatabaseReference usersPath = usersRef.child(sendTo);
        SynchronousListener getUser = new SynchronousListener();
        usersPath.addListenerForSingleValueEvent(getUser);

        DataSnapshot user = getUser.getSnapshot();
        User u = user.getValue(User.class);

        if(u == null || UID.equals(""))
        {
            return new CWHResponse("Cannot find user " + sendTo, false);
        }
        else // The friend is found, update their friends invitation list.
        {
            // vvv This should never return an empty string because it has already been checked up top.
            String friendsUsername = FireEater.UIDToUsername(UID);
            DatabaseReference friend = user.getRef().child("friend_invitations");
            Map<String, Object> updateFriendsInv = new HashMap<>();
            updateFriendsInv.put(UID, friendsUsername);
            friend.updateChildrenAsync(updateFriendsInv);

            return new CWHResponse("Added friend " + friendsUsername, true);
        }
    }
}
