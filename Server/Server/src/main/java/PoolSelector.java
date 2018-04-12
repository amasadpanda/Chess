import java.util.HashMap;

public class PoolSelector extends FireEater{

    HashMap<String, FireEater> pool;

    public PoolSelector(){

        pool = new HashMap<>();

        pool.put("Ranked Chess", new MatchmakingPool());
        pool.put("Ranked Chess960", new MatchmakingPool());
        pool.put("Ranked Peasants' Revolt", new MatchmakingPool());

        pool.put("Chess", new MatchmakingPool());
        pool.put("Chess960", new MatchmakingPool());
        pool.put("Peasants' Revolt", new MatchmakingPool());

    }

    @Override
    public CWHResponse handle(CWHRequest request) {
        return pool.get(request.getExtras().get("gametype")).handle(request);
    }
}
