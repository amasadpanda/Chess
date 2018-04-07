import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class Validator {

    private static final CWHResponse _BAD_AUTHID = new CWHResponse("The AuthID is invalid", false);
    private static final CWHResponse _BAD_FRIEND = new CWHResponse("Cannot find friend's username", false);

    private static HashMap<CWHRequest.RequestType, FireEater> eaters;

    public static CWHResponse processRequest(CWHRequest request)
    {
        String UID = request.getExtras().get("uid");//<<< testing, actual >>>getUID(request.getAuthID());
        if(UID.equals(""))
            return _BAD_AUTHID;
        String username = FireEater.UIDToUsername(UID);
        //should check the username to see if it exists ^^^^
        //request.put("uid", UID);
        switch (request.getRequestType())
        {
            case LOGIN:{
                if(username != null && !request.getExtras().get("username").equals(username))
                {
                    return new CWHResponse("Username does not match with database records", false);
                }
                break;
            }
            case MATCHMAKING_REQUEST:{
                break;
            }
            case ACCEPT_GAME:{
                //String gameID = request.getExtras().get("game");
                break;
            }
            case ACCEPT_FRIEND:
            case DENY_FRIEND:
            case GAME_CREATION:
            case FRIEND_REQUEST:{
                String friendUsername = request.getExtras().get("friend");
                // username with no spaces
                friendUsername.matches("([A-Z|a-z|0-9])*");
                if(friendUsername == null || friendUsername.equals(""))
                    return _BAD_FRIEND;
                try {
                    String friendUID = FireEater.usernameToUID(friendUsername);
                    request.put("frienduid", friendUID);
                } catch (Exception e) {
                    return _BAD_FRIEND;
                }
                break;
            }
        }

        return eaters.get(request.getRequestType()).handle(request);
    }

    public static void initialize()
    {
        System.out.print("[Initializing Request Handlers]...");
        eaters = new HashMap<>();
        eaters.put(CWHRequest.RequestType.LOGIN, new Login());
        eaters.put(CWHRequest.RequestType.FRIEND_REQUEST, new SocialNetworking());
        eaters.put(CWHRequest.RequestType.GAME_CREATION, new GameMaker());
        eaters.put(CWHRequest.RequestType.MATCHMAKING_REQUEST, new MatchmakingPool());
        eaters.put(CWHRequest.RequestType.ACCEPT_FRIEND, new AcceptFriend());
        eaters.put(CWHRequest.RequestType.DENY_FRIEND, new DenyFriend());
        eaters.put(CWHRequest.RequestType.ACCEPT_GAME, new AcceptGame());

        System.out.print("DONE\n");
    }

    private static String getUID(String AuthID)
    {
        String UID;
        try {
            UID = FireEater.tokenToUID(AuthID);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        if(UID.equals("") || UID == null)
            return "";

        return UID;
    }

}
