import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import com.google.gson.Gson;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Semaphore;


public class Main {
    public static void main(String[] args) throws Exception {
        FireEater.initialize("resources/firebase/chess-with-hats-firebase-adminsdk-e6fic-f6835bdf65.json");
        Login test = new Login();
        Scanner scan = new Scanner(System.in);
        System.out.println("goodbye!");

        FireEater.initialize("resources/firebase/chess-with-hats-firebase-adminsdk-e6fic-f6835bdf65.json");

        JettyServer server = new JettyServer("chessWithHats", -1, 1235, "resources/jetty/ChessWithHats.jks", "hhdus84hg61ghd7", "ldiif0746sk7aq9");
        server.start();

        new Scanner(System.in).next();
    }




    private static class TestData
    {
        public String s1;
        public String s2;
        public TestData(String something1, String something2)
        {
            this.s1 = something1;
            this.s2 = something2;
        }
    }
}