import com.google.api.core.ApiFuture;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.database.*;

import java.util.concurrent.Executor;


/**
 * The Login class takes a CWHRequest containing the user's UID and matches it to the username.
 * If the user is not found, a new user is created in the database. Note, this user will not have any fields available
 * except for its username.
 *
 * Currently it only prints out the user's username, but more functionality in the return should be added in the future.
 *
 * CWHResponse format:
 *  - If user is not in the database, returns a message informing a new user was created and sets success flag to false.
 *  - If user is in the database, return a user is found message and sets success flag to true.
 */
public class Login extends FireEater{

    private static FirebaseAuth auth;
    public Login()
    {
        auth = FirebaseAuth.getInstance();
    }

    private void createUser(String email, String username, String password)
    {
        UserRecord.CreateRequest create = new UserRecord.CreateRequest();
        create.setEmail(email);
        create.setDisplayName(username);
        create.setPassword(password);
        auth.createUserAsync(create);
    }

    @Override
    public CWHResponse handle(CWHRequest request) {
        FirebaseDatabase database = getDatabase();
        DatabaseReference usersRef = database.getReference().child("users");

        String uid = request.getExtras().get("uid");
        // set this .child() parameter to the desired user's UID
        DatabaseReference usersPath = usersRef.child(uid);
        SynchronousListener s = new SynchronousListener();
        usersPath.addListenerForSingleValueEvent(s);

        DataSnapshot userSnapshot = s.getSnapshot();
        User get = userSnapshot.getValue(User.class);
        if(get == null)
        {
            String username = request.getExtras().get("username");
            usersPath.setValueAsync(new User(username));
            System.out.println("created new users");
            return new CWHResponse("Created new user: " + username, false);
        }
        else
        {
            System.out.println(userSnapshot.getKey() + " " +get.username);
        }
        return null;
    }

    public static void getUser(String UID)
    {
        ApiFuture<UserRecord> user = auth.getUserAsync(UID);
        Executor ex = new Executor() {
            @Override
            public void execute(Runnable command) {

            }
        };

    }
}
