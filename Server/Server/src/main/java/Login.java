import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Login extends FireEater{

    /**
     * Request as parameter
     */
    @Override
    public CWHResponse handle() {
        FirebaseDatabase database = getDatabase();
        DatabaseReference ref = database.getReference();

        return null;
    }
}