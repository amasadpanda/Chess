import com.google.firebase.database.DatabaseReference;

public class AcceptFriend extends FireEater {


    @Override
    public CWHResponse handle(CWHRequest request) {

        String friendUID = request.getExtras().get("uid");
        if(friendUID == null)
            return new CWHResponse("Friend's UID not provided", false);
        if(!FireEater.isUIDExist(friendUID))
            return new CWHResponse("Friend's UID was not found in the database", false);

        DatabaseReference ref = FireEater.getDatabase().getReference();
        //ref.child


        return null;
    }
}
