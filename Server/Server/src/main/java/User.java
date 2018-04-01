import java.util.Map;

public class User {

        public String username;
        public Map<String, String> current_games;
        public Map<String, String> past_games;
        public Map<String, String> friends;
        public Map<String, String> game_invitations;
        public Map<String, String> friend_invitations;

        public User(String name) {
            username = name;
        }

        public User(String name, Map<String, String> cg, Map<String, String> pg, Map<String, String> fri, Map<String, String> gi, Map<String, String> fi)
        {
            username = name;
            current_games = cg;
            past_games = pg;
            friends = fri;
            game_invitations = gi;
            friend_invitations = fi;
        }

        public User()
        {

        }
}
