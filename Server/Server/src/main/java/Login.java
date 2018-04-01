import com.google.firebase.database.*;

public class Login extends FireEater{

    /**
     * Request as parameter
     */
    @Override
    public CWHResponse handle(CWHRequest request) {
        final FirebaseDatabase database = getDatabase();
        DatabaseReference usersRef = database.getReference().child("users");

        String username = request.getExtras().get("username");
        // set this to the desired user's UID
        DatabaseReference usersPath = usersRef.child(username);
        usersPath.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                User get = dataSnapshot.getValue(User.class);
                if(get == null)
                {
                   DatabaseReference usersRef = dataSnapshot.getRef();
                   usersRef.setValueAsync(new User(username));
                   System.out.println("created new users");
                }
                else
                {
                    System.out.println(dataSnapshot.getKey() + " " +get.username);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // ...
                System.out.println(databaseError.getMessage());
            }
        });
        return null;
    }
}
