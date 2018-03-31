import com.google.firebase.database.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class Login extends FireEater{

    /**
     * Request as parameter
     */
    @Override
    public CWHResponse handle() {
        final FirebaseDatabase database = getDatabase();
        DatabaseReference usersRef = database.getReference().child("users");
        // method to get username based on all users -> usersRef.orderByChild().equalTo().addListenerForSingleValueEvent();

        // set this to the desired user's UID
        DatabaseReference usersPath = usersRef.child("new person");
        usersPath.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // ...
                User test = dataSnapshot.getValue(User.class);
                if(test == null)
                {
                    DatabaseReference usersRef = dataSnapshot.getRef();
                    usersRef.setValueAsync(new User("heeeellllo"));
                    System.out.println("created new users");
                }
                else
                    System.out.println(dataSnapshot.getKey() + " " +test.username);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // ...
                System.out.println(databaseError.getMessage());
            }
        });
        return null;
    }

    static class User {
        public String username;

        public User(String name) {
            username = name;
        }

        public User()
        {

        }
    }
}
