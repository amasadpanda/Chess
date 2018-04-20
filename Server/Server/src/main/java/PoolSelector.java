import java.util.HashMap;

public class PoolSelector extends FireEater{

    HashMap<String, FireEater> pool;

    public PoolSelector(){

        pool = new HashMap<>();

        pool.put("Ranked Chess", new MatchmakingPool("Ranked Chess"));

        pool.put("Chess", new MatchmakingPool("Chess"));
        pool.put("Chess960", new MatchmakingPool("Chess960"));
        pool.put("Peasants' Revolt", new MatchmakingPool("Peasants' Revolt"));
        pool.put("Transformers", new MatchmakingPool("Transformers"));
    }

    @Override
    public CWHResponse handle(CWHRequest request) {
        String gametype = request.getExtras().get("gametype");
        if(gametype == null)
            return new CWHResponse("Not a valid gametype!", false);
        return pool.get(gametype).handle(request);
    }
}
