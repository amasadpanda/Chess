import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

public class SynchronousListener implements ValueEventListener {

    private Semaphore snapshotAssigned;
    private volatile DataSnapshot snapshot;

    public SynchronousListener() {
        snapshotAssigned = new Semaphore(0);
    }

    @Override
    public void onDataChange(DataSnapshot snapshot) {
        this.snapshot = snapshot;
        snapshotAssigned.release();
    }

    @Override
    public void onCancelled(DatabaseError error) {
        // Will not happen
        throw new IllegalStateException("onCancelled() was called in SynchronousListener! This should NEVER have happened!!!");
    }

    public DataSnapshot getSnapshot()  {
        if (snapshot == null) {
            try {
                snapshotAssigned.acquire();
            } catch (InterruptedException e) {
                throw new IllegalStateException("SynchronousListener's getSnapshot() was improperly interrupted while waiting for data change! Should never happen!");
            }
        }
        return snapshot;
    }
}
