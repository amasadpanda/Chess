
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        FireEater.initialize("resources/firebase/chess-with-hats-firebase-adminsdk-e6fic-f6835bdf65.json");
        Login test = new Login();
        Scanner scan = new Scanner(System.in);
        System.out.println("goodbye!");

        System.out.println(FireEater.UIDToUsername("tim"));

        while(true)
        {
            Thread.sleep(5);
        }
    }
}