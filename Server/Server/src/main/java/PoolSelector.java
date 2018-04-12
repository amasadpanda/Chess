import java.util.HashMap;

public class PoolSelector extends FireEater{

    HashMap<String, FireEater> pool;

    public PoolSelector(){

        pool = new HashMap<>();

        pool.put("rankedchess", new MatchmakingPool());
        pool.put("ranked960", new MatchmakingPool());
        pool.put("rankedpeasant", new MatchmakingPool());

        pool.put("chess", new MatchmakingPool());
        pool.put("960", new MatchmakingPool());
        pool.put("peasant", new MatchmakingPool());

    }

    @Override
    public CWHResponse handle(CWHRequest request) {
        return pool.get(request.getExtras().get("gametype")).handle(request);
    }
}
