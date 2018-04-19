import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.util.HashMap;

public class CWHRequest {

    public enum RequestType {
        MAKE_MOVE, ACCEPT_FRIEND, DENY_FRIEND, ACCEPT_GAME, DENY_GAME, FRIEND_REQUEST, GAME_CREATION,
        MATCHMAKING_REQUEST, COMPUTER_GAME, CREATE_ACCOUNT, LEAVE_GAME
    }

    @Expose
    private String authID;

    @Expose
    private RequestType requestType;

    @Expose
    private HashMap<String, String> extras;

    public CWHRequest(String authID, RequestType requestType)
    {
        this.authID = authID;
        this.requestType = requestType;
        this.extras = new HashMap<String, String>();
    }

    public String getAuthID() {
        return authID;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public HashMap<String, String> getExtras() {
        return extras;
    }

    public void put(String key, String value) { extras.put(key, value); }

    private String getJSON()
    {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(this);
    }
}
