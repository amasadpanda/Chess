import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

public class MatchmakingPool extends FireEater implements Runnable{

    // Strings are the users UIDs
    public static  Queue<String> pool;
    public MatchmakingPool()
    {
        pool = new ConcurrentLinkedQueue<>();
    }

    @Override
    public CWHResponse handle(CWHRequest request) {
        String UID = "";
        try {
            UID = FireEater.tokenToUID(request.getAuthID());
        } catch (Exception e) {
            return new CWHResponse("Cannot find user associated with the AuthID: " + request.getAuthID(), false);
        }

        pool.offer(UID);

        // should we check the queue here and make the game if another user was found?

        return null;
    }

    @Override
    public void run() {
        while(true)
        {
            if(pool.size() >= 2)
            {
                String user1 = pool.remove();
                String user2 = pool.remove();

                // place users in game here, do we automatically make the game here or send back CWHResponse back to client
                // first?
            }
            else
            {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.err.println("There was a problem with the Matchmaking pool thread.");
                }
            }
        }
    }
}
