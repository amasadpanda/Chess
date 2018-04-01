/*
    Assumes that the person to be added is stored as key-value pair with the key "userid"
 */

import com.google.firebase.database.*;

public class SocialNetworking extends FireEater{

    @Override
    public CWHResponse handle(CWHRequest request) {
        String sendTo = request.getExtras().get("userid");
        String senderID;

        final FirebaseDatabase database = getDatabase();
        DatabaseReference usersRef = database.getReference().child("users");

        DatabaseReference usersPath = usersRef.child(sendTo);
        SynchronousListener getUser = new SynchronousListener();
        usersPath.addListenerForSingleValueEvent(getUser);

        DataSnapshot user = getUser.getSnapshot();
        User u = user.getValue(User.class);
        if(u == null)
        {
            return new CWHResponse("Cannot find user " + sendTo, false);
        }
        else
        {

            // update user's friend invitation list
            DatabaseReference ref = user.getRef();

        }



        return null;
    }
}
