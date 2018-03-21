import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Main {
    private static Scanner scan = new Scanner(System.in);
    public static void main(String[] args) throws Exception {
        while(true)
        {
            System.out.println("Start producer or consumer or http enqueuer (p/c/h)?");
            String response = scan.next();
            scan.nextLine();
            if (response.toLowerCase().equals("p"))
            {
                System.out.println("Starting producer...");
                startProducer();
                break;
            }
            else if (response.toLowerCase().equals("c"))
            {
                System.out.println("Starting consumer...");
                startConsumer();
                break;
            }
            else if (response.toLowerCase().equals("h"))
            {
                System.out.println("Starting https enqueuer...");
                startEnqueuer();
                break;
            }
            else
            {
                System.out.println("Invalid input! Please try again! :)");
            }
        }
    }

    private static void startEnqueuer() throws Exception {
        DateOutputHandler dateOutputHandler = new DateOutputHandler(System.out, System.lineSeparator());
        Producer producer = new Producer("127.0.0.1", "mainQueue");
        producer.connect();
        JettyEnqueuerHttpServer server = new JettyEnqueuerHttpServer("enqueuer1", -1, 1357,
                "ChessWithHats.jks", "hhdus84hg61ghd7", "ldiif0746sk7aq9",
                producer, new EnqueuerRequestValidator(100, 1024, dateOutputHandler),
                dateOutputHandler);
        //EnqueuerHttpsServer server = new EnqueuerHttpsServer("enqueuer1", producer, 10000, 10000, 1357, new File("ChessWithHats.jks"), "hhdus84hg61ghd7", "ChessWithHatsKey", "ldiif0746sk7aq9");
        //EnqueuerHttpServer server = new EnqueuerHttpServer("enqueuer1", producer, 10000, 10000, 1357);
        server.start();
        System.out.println("Server started!");
    }

    private static void startProducer() throws IOException, TimeoutException {
        Producer p = new Producer("127.0.0.1", "mainQueue");
        p.connect();
        System.out.println("Connected!");

        while(true)
        {
            System.out.println("Enter a message, or exit to stop:");
            String message = scan.nextLine();
            if (message.toLowerCase().equals("exit"))
            {
                break;
            }
            else if (message.toLowerCase().equals("send100000"))
            {
                System.out.println("Sending 100000 small messages....");
                long start = System.currentTimeMillis();
                for(int i = 0; i < 100000; i++)
                {
                    p.sendMessage(("Number " + i).getBytes(Charset.forName("UTF-8")));
                }
                System.out.println("All sent! Took " + (System.currentTimeMillis()-start)/1000.0 + "s");
            }
            else
            {
                p.sendMessage(message.getBytes(Charset.forName("UTF-8")));
            }
        }

        p.disconnect();
        System.out.println("Disconnected!");
    }

    private static void startConsumer() throws IOException, TimeoutException {
        Consumer c = new Consumer("127.0.0.1","mainQueue");
        c.connect();
    }
}
