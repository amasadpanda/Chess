import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.ListUsersPage;
import com.google.firebase.database.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * FireEater - written by Tim, March 31, 2018
 * Purpose: Abstract server-side component for request handling, bridging the Jetty server to Firebase.
 * Functions: Implementing classes are objects that can handle different requests. This class includes static methods for general Firebase operations.
 * @database - Object representing the Firebase database instance - to communicate with the database.
 * @isInitialized - flag indicating whether a connection exists between Firebase and this server.
 */

public abstract class FireEater {

    private static FirebaseDatabase database;
    private static boolean isInitialized = false;

    public FireEater()
    {

    }

    /**
     *component
     */
    public abstract CWHResponse handle(CWHRequest request);


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

        database = FirebaseDatabase.getInstance();
        System.out.print("[Initializing Database]...");
        SynchronousListener sl = new SynchronousListener();
        database.getReference().addListenerForSingleValueEvent(sl);
        sl.getSnapshot();
        System.out.print("DONE\n");
        isInitialized = true;
    }

    /**
     *
     * @param UID
     * @return Username matched with the UID or empty string if not found.
     */
    public static String UIDToUsername(String UID)
    {
        DatabaseReference usersPath = database.getReference().child("users").child(UID);
        SynchronousListener s = new SynchronousListener();
        usersPath.addListenerForSingleValueEvent(s);
        Object b = s.getSnapshot().child("username").getValue();
        if(b == null)
            return null;
        return b.toString();
    }

    /*
        Given the auth token, determine the UID associated with it!
     */
    protected static String tokenToUID(String authID) throws ExecutionException, InterruptedException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdTokenAsync(authID).get();
        return decodedToken.getUid();
    }

    /*
        Give a username, looks up in Firebase the UID that belongs too, or null if the uername is not taken.
     */
    protected static String usernameToUID(String username) throws Exception {
        ListUsersPage page = FirebaseAuth.getInstance().listUsersAsync(null).get();
        for (ExportedUserRecord user : page.iterateAll())
        {
            if (user.getDisplayName().equals(username))
            {
                return user.getUid();
            }
        }
        return null;
    }


    protected static FirebaseDatabase getDatabase() throws NullPointerException
    {
        if(database == null)
            throw new NullPointerException("Firebase Database was not initialized!");
        return database;
    }

}