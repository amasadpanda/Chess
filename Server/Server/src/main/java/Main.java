
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.FileInputStream;

public class Main {
    public static void main(String[] args) throws Exception {

        System.out.println("goodbye!");
        JettyServer server = new JettyServer("chessWithHats", -1, 1235, "resources/jetty/ChessWithHats.jks", "hhdus84hg61ghd7", "ldiif0746sk7aq9");
        server.start();
    }
}