import Rulesets.Ruleset;

import java.util.ArrayList;
import java.util.HashMap;

public class PoolSelector extends FireEater{

    HashMap<String, FireEater> pool;

    public PoolSelector(){

        pool = new HashMap<>();

        ArrayList<String> rules = Ruleset.getListOfRules();

        pool.put("Ranked Chess", new MatchmakingPool("Ranked Chess"));

        for(String s : rules)
            pool.put(s, new MatchmakingPool(s));
    }

    @Override
    public CWHResponse handle(CWHRequest request) {
        String gametype = request.getExtras().get("gametype");
        if(gametype == null)
            return new CWHResponse("Not a valid gametype!", false);
        return pool.get(gametype).handle(request);
    }
}
