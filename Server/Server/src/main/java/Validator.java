import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class Validator {

    private static final CWHResponse _BAD_AUTHID = new CWHResponse("Invalid credentials", false);
    private static final CWHResponse _BAD_FRIEND = new CWHResponse("Cannot find friend's username", false);

    private static HashMap<CWHRequest.RequestType, FireEater> eaters;

    public static CWHResponse processRequest(CWHRequest request)
    {
        CWHRequest.RequestType type = request.getRequestType();
        if(type == CWHRequest.RequestType.CREATE_ACCOUNT)
        {
            if(!request.getAuthID().isEmpty())
                return new CWHResponse("The AuthID is not empty", false);
            if(!request.getExtras().containsKey("email"))
                return new CWHResponse("Supply an email", false);
            if(!request.getExtras().containsKey("username"))
                return new CWHResponse("Supply an username", false);
            if(!request.getExtras().containsKey("password"))
                return new CWHResponse("Supply a password", false);
        }
        else
        {
            String UID = getUID(request.getAuthID());
            String username = FireEater.UIDToUsername(UID);
            if(UID == null || username == null)
                return _BAD_AUTHID;
            request.put("uid", UID);
            request.put("username", username);

            if(type == CWHRequest.RequestType.ACCEPT_FRIEND || type == CWHRequest.RequestType.DENY_FRIEND ||
                    type == CWHRequest.RequestType.GAME_CREATION || type == CWHRequest.RequestType.FRIEND_REQUEST)
            {
                String friendUsername = request.getExtras().get("friend");
                if(friendUsername == null)
                    return _BAD_FRIEND;
                try {
                    String friendUID = FireEater.usernameToUID(friendUsername);
                    if(friendUID == null)
                        return _BAD_FRIEND;
                    request.put("frienduid", friendUID);
                } catch (Exception e) {
                    return _BAD_FRIEND;
                }
                if(type == CWHRequest.RequestType.GAME_CREATION)
                {
                    if(request.getExtras().get("gametype") == null)
                        return new CWHResponse("No gametype specified", false);
                }
            }
            if(type == CWHRequest.RequestType.ACCEPT_GAME || type == CWHRequest.RequestType.DENY_GAME
                    || type == CWHRequest.RequestType.MAKE_MOVE)
            {
                String gameID = request.getExtras().get("gameid");
                if(gameID == null)
                    return new CWHResponse("No gameid specified", false);
                DatabaseReference ref = FireEater.getDatabase().getReference().child("games").child(gameID);
                SynchronousListener checkValidGame = new SynchronousListener();
                ref.addListenerForSingleValueEvent(checkValidGame);
              //  if(!checkValidGame.getSnapshot().exists())
              //      return new CWHResponse("Game not found", false);
            }
            if(type == CWHRequest.RequestType.MAKE_MOVE)
            {
                if(!request.getExtras().containsKey("start") || !request.getExtras().containsKey("end"))
                    return new CWHResponse("No location specified", false);

            }
        }
        return eaters.get(request.getRequestType()).handle(request);
    }


    public static void initialize()
    {
        System.out.print("[Initializing Request Handlers]...");
        eaters = new HashMap<>();
        eaters.put(CWHRequest.RequestType.CREATE_ACCOUNT,       new CreateUser());
        eaters.put(CWHRequest.RequestType.FRIEND_REQUEST,       new SocialNetworking());
        eaters.put(CWHRequest.RequestType.GAME_CREATION,        new GameMaker());
        eaters.put(CWHRequest.RequestType.MATCHMAKING_REQUEST,  new MatchmakingPool());
        eaters.put(CWHRequest.RequestType.ACCEPT_FRIEND,        new AcceptFriend());
        eaters.put(CWHRequest.RequestType.DENY_FRIEND,          new DenyFriend());
        eaters.put(CWHRequest.RequestType.ACCEPT_GAME,          new AcceptGame());
        eaters.put(CWHRequest.RequestType.DENY_GAME,            new DenyGame());
        eaters.put(CWHRequest.RequestType.MAKE_MOVE,            new Mover());

        System.out.print("DONE\n");
    }

    private static String getUID(String AuthID)
    {
        String UID;
        try {
            UID = FireEater.tokenToUID(AuthID);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if(UID == null || UID.isEmpty())
            return null;

        return UID;
    }

}
