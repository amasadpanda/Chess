import com.google.firebase.database.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Login extends FireEater{

    /**
     * Request as parameter
     */
    @Override
    public CWHResponse handle() {
        final FirebaseDatabase database = getDatabase();
        DatabaseReference ref = database.getReference("chess-with-hats/users");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // ...
                //System.out.println(dataSnapshot.)
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // ...
            }
        });
        return null;
    }

    static class User
    {
        
    }
}
