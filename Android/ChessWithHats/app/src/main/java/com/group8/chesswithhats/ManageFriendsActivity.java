package com.group8.chesswithhats;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.group8.chesswithhats.server.CWHRequest;
import com.group8.chesswithhats.server.CWHResponse;
import com.group8.chesswithhats.server.OnCWHResponseListener;
import com.group8.chesswithhats.util.CurrentFriendView;
import com.group8.chesswithhats.util.FriendRequestView;

import java.util.ArrayList;
import java.util.HashMap;

/*
    @author Philip Rodriguez
 */
public class ManageFriendsActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    private FirebaseDatabase firebaseDatabase;

    private HashMap<DatabaseReference, Object> listeners;

    private Button btnAddAFriend;
    private LinearLayout llFriendRequests;
    private LinearLayout llCurrentFriends;
    private TextView txtNoCurrentFriends;
    private TextView txtNoCurrentFriendRequests;

    private ArrayList<String> allUsernames;
    private ArrayAdapter<String> autocompleteAdapter;
    private Query usernameQuery;
    private ChildEventListener usernameListener;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_friends);
        this.firebaseDatabase = FirebaseDatabase.getInstance();
        this.firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                checkSignedInStatus();
            }
        };
        firebaseAuth.addAuthStateListener(firebaseAuthListener);

        listeners = new HashMap<>();

        allUsernames = new ArrayList<String>();
        autocompleteAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, allUsernames);

        initComponents();
    }

    private void checkSignedInStatus()
    {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null)
        {
            // Kick them out of this activity!
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkSignedInStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Remove all firebase listeners!
        firebaseAuth.removeAuthStateListener(firebaseAuthListener);
        for (DatabaseReference reference : listeners.keySet())
        {
            Object listener = listeners.get(reference);
            if (listener instanceof ValueEventListener)
            {
                reference.removeEventListener((ValueEventListener) listener);
            }
            else if (listener instanceof ChildEventListener)
            {
                reference.removeEventListener((ChildEventListener)listener);
            }
        }

        usernameQuery.removeEventListener(usernameListener);
    }

    private void initComponents()
    {
        btnAddAFriend = findViewById(R.id.managefriends_btnAddAFriend);
        txtNoCurrentFriendRequests = findViewById(R.id.managefriends_txtNoCurrentFriendRequests);
        txtNoCurrentFriends = findViewById(R.id.managefriends_txtNoCurrentFriends);
        llCurrentFriends = findViewById(R.id.managefriends_llCurrentFriends);
        llFriendRequests = findViewById(R.id.managefriends_llFriendRequests);

        btnAddAFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ManageFriendsActivity.this);
                AlertDialog dialog = builder.create();
                dialog.setTitle("Add a Friend");
                final AutoCompleteTextView friendUsername = new AutoCompleteTextView(ManageFriendsActivity.this);
                friendUsername.setInputType(InputType.TYPE_CLASS_TEXT);
                friendUsername.setHint("Friend's Username");
                friendUsername.setAdapter(autocompleteAdapter);
                dialog.setView(friendUsername);

                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Send Request", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CWHRequest cwhRequest = new CWHRequest(firebaseAuth.getCurrentUser(), CWHRequest.RequestType.FRIEND_REQUEST, new OnCWHResponseListener() {
                            @Override
                            public void onCWHResponse(CWHResponse response) {
                                // Whether we succeed or fail, just show the response!
                                Toast.makeText(ManageFriendsActivity.this, response.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                        cwhRequest.getExtras().put("friend", friendUsername.getText().toString());
                        cwhRequest.sendRequest(ManageFriendsActivity.this);
                    }
                });

                dialog.show();
            }
        });

        initFirebaseListeners();
    }

    private void initFirebaseListeners()
    {
        DatabaseReference friendInvitationsReference = firebaseDatabase.getReference().child("users").child(firebaseAuth.getCurrentUser().getUid()).child("friend_invitations");
        ChildEventListener friendInvitationsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                System.out.println("ON CHILD ADDED FRIEND INVITATIONS: " + dataSnapshot);

                String senderUID = dataSnapshot.getKey();
                String senderUsername = dataSnapshot.getValue(String.class);

                Intent intent = new Intent(ManageFriendsActivity.this, ManageFriendsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(ManageFriendsActivity.this, 0, intent, 0);

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ManageFriendsActivity.this, NotificationChannel.DEFAULT_CHANNEL_ID)
                        .setSmallIcon(R.drawable.launcher_icon)
                        .setContentTitle("Friend Request!")
                        .setContentText(senderUsername + " sent you a friend request!")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ManageFriendsActivity.this);

// notificationId is a unique int for each notification that you must define
                notificationManager.notify(senderUsername.hashCode(), mBuilder.build());



                FriendRequestView friendRequestView = new FriendRequestView(ManageFriendsActivity.this, senderUID, senderUsername, firebaseAuth);
                llFriendRequests.addView(friendRequestView);
                txtNoCurrentFriendRequests.setVisibility(View.GONE);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                System.out.println("ON CHILD REMOVED FRIEND INVITATIONS: " + dataSnapshot);

                String senderUID = dataSnapshot.getKey();
                String senderUsername = dataSnapshot.getValue(String.class);

                for (int v = 0; v < llFriendRequests.getChildCount(); v++)
                {
                    if (llFriendRequests.getChildAt(v) instanceof FriendRequestView)
                    {
                        FriendRequestView friendRequestView = (FriendRequestView)llFriendRequests.getChildAt(v);
                        if (friendRequestView.getUID().equals(senderUID))
                        {
                            // Remove this one!
                            llFriendRequests.removeView(friendRequestView);

                            if (llFriendRequests.getChildCount() == 1)
                            {
                                txtNoCurrentFriendRequests.setVisibility(View.VISIBLE);
                            }

                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        friendInvitationsReference.addChildEventListener(friendInvitationsListener);
        listeners.put(friendInvitationsReference, friendInvitationsListener);

        DatabaseReference friendsReference = firebaseDatabase.getReference().child("users").child(firebaseAuth.getCurrentUser().getUid()).child("friends");
        ChildEventListener friendsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                System.out.println("ON CHILD ADDED FRIENDS: " + dataSnapshot);

                String uid = dataSnapshot.getKey();
                String username = dataSnapshot.getValue(String.class);

                CurrentFriendView currentFriendView = new CurrentFriendView(ManageFriendsActivity.this, username, uid);
                llCurrentFriends.addView(currentFriendView);
                txtNoCurrentFriends.setVisibility(View.GONE);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                System.out.println("ON CHILD REMOVED FRIENDS: " + dataSnapshot);

                String friendUID = dataSnapshot.getKey();
                String friendUsername = dataSnapshot.getValue(String.class);

                for (int v = 0; v < llCurrentFriends.getChildCount(); v++)
                {
                    if (llCurrentFriends.getChildAt(v) instanceof CurrentFriendView)
                    {
                        CurrentFriendView currentFriendView = (CurrentFriendView)llCurrentFriends.getChildAt(v);
                        if (currentFriendView.getUid().equals(friendUID))
                        {
                            // Remove this one!
                            llCurrentFriends.removeView(currentFriendView);

                            if (llCurrentFriends.getChildCount() == 1)
                            {
                                txtNoCurrentFriends.setVisibility(View.VISIBLE);
                            }

                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        friendsReference.addChildEventListener(friendsListener);
        listeners.put(friendsReference, friendsListener);

        DatabaseReference usersReference = firebaseDatabase.getReference().child("users");
        usernameQuery = usersReference.orderByChild("username");
        usernameListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                autocompleteAdapter.add(dataSnapshot.child("username").getValue(String.class));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                autocompleteAdapter.remove(dataSnapshot.child("username").getValue(String.class));
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        usernameQuery.addChildEventListener(usernameListener);
    }
}
