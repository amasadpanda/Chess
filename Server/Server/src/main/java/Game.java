public class Game {

    // these are set as UIDs
    public String  black, white;
    public String moves;

    public Game(String UID)
    {
        int coinToss = (int)Math.random()*2;
        if(coinToss == 1)
            black = UID;
        else
            white = UID;
    }

    public Game(String user1, String user2)
    {
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
    }
}
