import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

public class MatchmakingPool extends FireEater{

    // Strings are the users UIDs
    public static  Queue<String> pool;
    public MatchmakingPool()
    {
        pool = new ConcurrentLinkedQueue<>();
    }

    @Override
    public CWHResponse handle(CWHRequest request) {

        String UID = request.getExtras().get("uid");

        if(pool.isEmpty())
        {
            pool.offer(UID);
            return new CWHResponse("Place in matchingmaking pool", true);
        }
        else
        {
            String pooledUser = pool.remove();

            DatabaseReference ref = FireEater.getDatabase().getReference();
            DatabaseReference newGame = ref.child("games").push();
            DatabaseReference user1Path = ref.child("users").child(UID).child("current_games");
            DatabaseReference user2Path = ref.child("users").child(pooledUser).child("current_games");

            // makes the actual game in games database
            String gameID = newGame.getKey();
            Game g = new Game(UID, pooledUser);
            newGame.setValueAsync(g);

            // update user information
            Map<String, String> updateGameList = new HashMap<>();
            updateGameList.put(gameID, "random");
            user1Path.setValueAsync(updateGameList);
            user2Path.setValueAsync(updateGameList);

            return new CWHResponse("Matched with user", true);
        }
    }
}
