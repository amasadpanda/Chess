import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.FileInputStream;

public class Main {
    public static void main(String[] args) throws Exception {
        FileInputStream serviceAccount = new FileInputStream("resources/firebase/chess-with-hats-firebase-adminsdk-e6fic-f6835bdf65.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://chess-with-hats.firebaseio.com")
                .build();

        FirebaseApp.initializeApp(options);

        System.out.println("Hi");
    }
}
