import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import com.google.gson.Gson;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;


public class Main {
    public static void main(String[] args) throws Exception {
        FireEater.initialize("resources/firebase/chess-with-hats-firebase-adminsdk-e6fic-f6835bdf65.json");
        Validator.initialize();
        startServer();
        System.out.println("goodbye!");


        CWHRequest c = new CWHRequest("null auth id", CWHRequest.RequestType.MAKE_MOVE);
        c.put("gameid", "-L9QYy3Fc9De6RnhgVCE");
        c.put("start", "1");
        c.put("end", "2");
        Mover m = new Mover();
        System.out.println(m.handle(c));

        new Scanner(System.in).next();
    }

    private static void startServer() throws Exception {
        JettyServer server = new JettyServer("chessWithHats", -1, 1235, "resources/jetty/ChessWithHats.jks", "hhdus84hg61ghd7", "ldiif0746sk7aq9");
        server.start();
    }
}