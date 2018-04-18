import java.util.Map;

public class User {

        public String username;
        public Map<String, String> current_games;
        public Map<String, String> past_games;
        public Map<String, String> friends;
        public Map<String, String> game_invitations;
        public Map<String, String> friend_invitations;
        public Double rank;

        public User(String name) {
            username = name;
            rank = 1200.0;
        }

        public User(String name, Map<String, String> cg, Map<String, String> pg, Map<String, String> fri, Map<String, String> gi, Map<String, String> fi, Double ra)
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

        /*
        scores
            win = 1
            draw = .5
            loss = 0
         */
        public static double newElo(double elo, double eloOther, double score)
        {
            double expected = 1/(1+Math.pow(10, (eloOther - elo)/400));
            return elo + getKFactor(elo)*(score-expected);
        }

        private static double getKFactor(Double rank)
        {
            if(rank < 2100)
                return 32;
            if(rank < 2400)
                return 24;
            return 16;
        }
}
