package com.group8.chesswithhats;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.group8.chesswithhats.server.CWHRequest;
import com.group8.chesswithhats.server.CWHResponse;
import com.group8.chesswithhats.server.OnCWHResponseListener;
import com.group8.chesswithhats.util.CurrentGameView;
import com.group8.chesswithhats.util.GameInviteView;

/*
 * @author Philip Rodriguez
 */
public class HomeActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;

    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    private LinearLayout llGameInvites;
    private LinearLayout llCurrentGames;
    private NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        // If they sign out on us, handle it.
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                checkSignedInStatus();
            }
        };
        firebaseAuth.addAuthStateListener(firebaseAuthListener);

        // Initialize GUI components
        initComponents();

        // Initialize Firebase Listeners
        initFirebaseListeners();
    }

    private void initComponents()
    {
        llGameInvites = findViewById(R.id.home_llGameInvites);
        llCurrentGames = findViewById(R.id.home_llCurrentGames);
        navigationView = findViewById(R.id.home_navigationView);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getTitle().equals(getResources().getString(R.string.home_newGame)))
                {
                    CWHRequest request = new CWHRequest(firebaseAuth.getCurrentUser(), CWHRequest.RequestType.GAME_CREATION, new OnCWHResponseListener() {
                        @Override
                        public void onCWHResponse(CWHResponse response) {
                            System.out.println(response);
                        }
                    });
                    request.getExtras().put("friend", "timothy94");
                    request.getExtras().put("gametype", "Chess960");
                    request.sendRequest(HomeActivity.this);
                }
                else if (item.getTitle().equals(getResources().getString(R.string.home_manageFriends)))
                {

                }
                else if (item.getTitle().equals(getResources().getString(R.string.home_manageAccount)))
                {

                }
                else if (item.getTitle().equals(getResources().getString(R.string.home_signOut)))
                {
                    firebaseAuth.signOut();
                }
                else
                {
                    // Lol, shouldn't be possible
                }
                return true;
            }
        });
    }

    private void initFirebaseListeners()
    {
        firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        // Sanity check that we're signed in....
        if (currentUser == null)
        {
            // This shouldn't have been able to happen...
            System.out.println("CURRENT USER WAS NULL!!!!!");
            firebaseAuth.signOut();
        }
        else
        {
            databaseReference.child("users").child(currentUser.getUid()).child("game_invitations").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    try {
                        // We need to add to our linear layout!
                        final String gameID = dataSnapshot.getKey();
                        String inviterUsername = dataSnapshot.getValue(String.class).split(";")[0];
                        String gameType = dataSnapshot.getValue(String.class).split(";")[1];

                        GameInviteView newChild = new GameInviteView(HomeActivity.this, gameID, inviterUsername, gameType, firebaseAuth);
                        llGameInvites.addView(newChild);
                    }
                    catch (Exception exc)
                    {
                        System.out.println("Attempted to add malformed child: " + dataSnapshot);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    // lol, that shouldn't happen!
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    // We need to find and remove the relevant child!
                    String gameID = dataSnapshot.getKey();
                    for (int v = 0; v < llGameInvites.getChildCount(); v++)
                    {
                        GameInviteView view = (GameInviteView) llGameInvites.getChildAt(v);
                        if (view.getCorrespondingGameID().equals(gameID))
                        {
                            // This is the one to remove!
                            llGameInvites.removeView(view);
                            break;
                        }
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    // also shouldn't happen
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // also shouldn't happen
                }
            });

            databaseReference.child("users").child(currentUser.getUid()).child("current_games").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    System.out.println("ON CHILD ADDED CALLED: " + dataSnapshot);
                    try {
                        // We need to add to our linear layout!
                        final String gameID = dataSnapshot.getKey();
                        final String opponent = dataSnapshot.getValue(String.class);
                        final String gameType = "NOT YET IMPLEMENTED";

                        CurrentGameView newChild = new CurrentGameView(HomeActivity.this, gameID, opponent, gameType);
                        llCurrentGames.addView(newChild);
                    }
                    catch (Exception exc)
                    {
                        System.out.println("Attempted to add malformed child: " + dataSnapshot);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    // lol, that shouldn't happen!
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    // We need to find and remove the relevant child!
                    System.out.println("ON CHILD REMOVED: " + dataSnapshot);
                    String gameID = dataSnapshot.getKey();
                    for (int v = 0; v < llCurrentGames.getChildCount(); v++)
                    {
                        GameInviteView view = (GameInviteView) llCurrentGames.getChildAt(v);
                        if (view.getCorrespondingGameID().equals(gameID))
                        {
                            // This is the one to remove!
                            llCurrentGames.removeView(view);
                            break;
                        }
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    // also shouldn't happen
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // also shouldn't happen
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkSignedInStatus();
    }

    private void checkSignedInStatus()
    {
        // Make sure we are signed in. If not, kick out of home activity!
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null)
        {
            // We are not signed in! Kick them out!
            // Remove the listener so we do not keep opening many login activities!
            firebaseAuth.removeAuthStateListener(firebaseAuthListener);

            // Notify the user of the sign out
            Toast.makeText(this, "You have been signed out.", Toast.LENGTH_LONG).show();

            // End this home activity...
            finish();

            // Go back to login activity.
            Intent loginActivityIntent = new Intent(this, LoginActivity.class);
            startActivity(loginActivityIntent);
        }
        else
        {
            // TODO: you could remove this else branch completely
            Toast.makeText(this, "Signed in as " + firebaseAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
        }
    }
}
