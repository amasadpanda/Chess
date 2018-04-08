import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game {

    // these are set as UIDs
    public String  black, white;
    public String moves;
    public Map<String, String> board;

    public Game()
    {
        initStartState();
    }

    public Game(String UID)
    {
        this();
        int coinToss = (int)Math.random()*2;
        if(coinToss == 1)
            black = UID;
        else
            white = UID;
    }

    public Game(String user1, String user2)
    {
        this();
        int coinToss = (int)Math.random()*2;
        if(coinToss == 1)
        {
            black = user1;
            white = user2;
        }
        else
        {
            black = user2;
            white = user1;
        }
        initStartState();
    }

    private void initStartState()
    {

    }
}
