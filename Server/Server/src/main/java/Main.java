
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.FileInputStream;

public class Main {
    public static void main(String[] args) throws Exception {
        FireEater.initialize("resources/firebase/chess-with-hats-firebase-adminsdk-e6fic-f6835bdf65.json");
        Login test = new Login();
        test.handle();
        System.out.println("goodbye!");
        while(true)
        {
            Thread.sleep(3);
        }
    }
}