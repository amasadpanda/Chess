import java.util.Map;

public class User {

        public String username;
        public Map<String, String> current_games;
        public Map<String, String> past_games;
        public Map<String, String> friends;
        public Map<String, String> game_invitations;
        public Map<String, String> friend_invitations;
        public Integer rank;

        public User(String name) {
            username = name;
            rank = 1200;
        }

        public User(String name, Map<String, String> cg, Map<String, String> pg, Map<String, String> fri, Map<String, String> gi, Map<String, String> fi, Integer ra)
        {
            username = name;
            current_games = cg;
            past_games = pg;
            friends = fri;
            game_invitations = gi;
            friend_invitations = fi;
            rank = ra;
        }

        public User()
        {

        }
}
