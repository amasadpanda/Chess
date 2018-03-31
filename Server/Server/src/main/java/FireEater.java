import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class FireEater {

    private static final String _RESOURCEKEYPATH = "resources/firebase/chess-with-hats-firebase-adminsdk-e6fic-f6835bdf65.json";

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
    public abstract CWHResponse handle();


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

        isInitialized = true;
    }

    protected static FirebaseDatabase getDatabase() throws NullPointerException
    {
        if(database == null)
            throw new NullPointerException();
        return database;
    }

}
