import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.database.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public abstract class FireEater {

    private static FirebaseDatabase database;
    private static boolean isInitialized = false;

    public FireEater()
    {

    }

    /**
     *
     * TODO: Custom requests class as parameter
     *
     */
    public abstract CWHResponse handle(CWHRequest cwhRequest);


    protected static void initialize(String serviceKeyPath) throws IOException {

        if(isInitialized)
            return;

        // Fetch the service account key JSON file contents
        FileInputStream serviceAccount = new FileInputStream(serviceKeyPath);

        // Initialize the app with a service account, granting admin privileges
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://chess-with-hats.firebaseio.com")
                .build();
        FirebaseApp.initializeApp(options);

        isInitialized = true;
    }

    /*
        Given the auth token, determine the UID associated with it!
     */
    protected static String tokenToUID(String authID) throws ExecutionException, InterruptedException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdTokenAsync(authID).get();
        return decodedToken.getUid();
    }

    /*
        Give a username, looks up in Firebase the UID that belongs too.
     */
    protected static String usernameToUID(String username) throws Exception {
        final Semaphore semaphore = new Semaphore(0);
        StringBuilder result = new StringBuilder();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference valueRef = database.getReference().child("users");
        Query myQuery = valueRef.orderByChild("username").equalTo(username);
        myQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // Do not try to access what is not there!
                if (snapshot.getChildrenCount() > 0) {
                    result.append(snapshot.getChildren().iterator().next().getKey());
                }
                semaphore.release();
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
        try {
            semaphore.tryAcquire(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // Should not happen
        }
        if (result.length() <= 0)
        {
            throw new Exception("Username to UID took too long!");
        }
        return result.toString();
    }

    protected static FirebaseDatabase getDatabase() throws NullPointerException
    {
        if(database == null)
            throw new NullPointerException();
        return database;
    }

}
