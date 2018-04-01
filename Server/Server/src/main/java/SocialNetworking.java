/*
    Assumes that the person to be added is stored as key-value pair with the key "userid"
    NOT DONE YET
 */

import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class SocialNetworking extends FireEater{

    @Override
    public CWHResponse handle(CWHRequest request) {
        String sendTo = request.getExtras().get("userid");
        String UID = "";
        try {
            UID = FireEater.tokenToUID(request.getAuthID());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
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
        else
        {
            // update user's friend invitation list
            DatabaseReference ref = user.getRef().child("friend_invitations");
            Map<String, Object> updateFriendsInv = new HashMap<>();
            updateFriendsInv.put(UID, FireEater.UIDToUsername(UID));
        }



        return null;
    }
}
