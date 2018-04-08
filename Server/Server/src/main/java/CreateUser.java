import com.google.api.core.ApiFuture;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.database.*;

import java.util.concurrent.ExecutionException;
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
public class CreateUser extends FireEater{

    private static FirebaseAuth auth;
    public CreateUser()
    {
        auth = FirebaseAuth.getInstance();
    }

    private CWHResponse createUser(String email, String username, String password)
    {
        try{

            UserRecord.CreateRequest create = new UserRecord.CreateRequest();
            create.setEmail(email);
            create.setDisplayName(username);
            create.setPassword(password);

            String doesExist = FireEater.usernameToUID(username);
            if(doesExist != null)
                return new CWHResponse("Username already taken", false);

            // this is a hacky check to see if the email is already taken
            UserRecord r = auth.createUserAsync(create).get();
            String UID = r.getUid();

            // database below

            DatabaseReference usersPath = getDatabase().getReference().child("users").child(UID);
            SynchronousListener s = new SynchronousListener();
            usersPath.setValueAsync(new User(username));
            return new CWHResponse("Created new user: " + username, true);
        } catch (Exception e)
        {
            return new CWHResponse("Something went wrong when creating user!\n" + e.getMessage() , false);
        }
    }

    @Override
    public CWHResponse handle(CWHRequest request) {
        String username = request.getExtras().get("username");
        String password = request.getExtras().get("password");
        String email = request.getExtras().get("email");
        // this is for the FirebaseAuth
        return createUser(email, username, password);
    }

    public static void getUser(String UID)
    {
        try {
            UserRecord user = auth.getUserAsync(UID).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        Executor ex = new Executor() {
            @Override
            public void execute(Runnable command) {

            }
        };

    }
}