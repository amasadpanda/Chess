import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MatchmakingPool extends FireEater{

    private static final Logger logger = Logger.getLogger(MatchmakingPool.class.getName());


    // Strings are the users UIDs
    public Queue<String> pool;
    private String poolName;

    public MatchmakingPool(String name)
    {
        pool = new ConcurrentLinkedQueue<>();
        poolName = name;
    }

    @Override
    public CWHResponse handle(CWHRequest request) {
        String UID = request.getExtras().get("uid");
        String gametype = request.getExtras().get("gametype");
        String username = request.getExtras().get("username");

        if(pool.isEmpty())
        {
            pool.offer(UID);
            logger.log(Level.INFO, poolName + " added " + UID);
            return new CWHResponse("Place in matchingmaking pool", true);
        }
        else
        {
            String pooledUser = pool.peek();
            if(pooledUser.equals(UID))
                return new CWHResponse("You are already in this matchmaking pool", false);

            DatabaseReference ref = FireEater.getDatabase().getReference();
            DatabaseReference newGame = ref.child("games").push();
            DatabaseReference user1Path = ref.child("users").child(UID).child("current_games");
            DatabaseReference user2Path = ref.child("users").child(pooledUser).child("current_games");

            // makes the actual game in games database
            String gameID = newGame.getKey();
            Game g = new Game(UID, pooledUser);
            g.setGametype(poolName);
            newGame.setValueAsync(g);

            // update requested user's list
            String otherUserName = FireEater.UIDToUsername(pooledUser);
            Map<String, Object> updateGameList = new HashMap<>();
            updateGameList.put(gameID, otherUserName);
            user1Path.updateChildrenAsync(updateGameList);

            //update pooled user's list
            updateGameList.put(gameID, username);
            user2Path.updateChildrenAsync(updateGameList);


            return new CWHResponse("Matched with user", true);
        }
    }
}
