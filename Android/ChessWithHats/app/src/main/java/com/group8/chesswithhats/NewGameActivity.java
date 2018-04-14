package com.group8.chesswithhats;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.group8.chesswithhats.server.CWHRequest;
import com.group8.chesswithhats.server.CWHResponse;
import com.group8.chesswithhats.server.OnCWHResponseListener;
import com.group8.chesswithhats.util.LoadingDialog;

import java.util.ArrayList;
import java.util.HashMap;

/*
    @author Philip Rodriguez
 */
public class NewGameActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    private FirebaseDatabase firebaseDatabase;
    private HashMap<DatabaseReference, Object> listeners;

    private Spinner spnGameType;
    private Spinner spnOpponent;
    private Button btnCreateGame;

    private AutoCompleteTextView edtUsername;

    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);
        firebaseDatabase = FirebaseDatabase.getInstance();
        listeners = new HashMap<>();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                checkSignedInStatus();
            }
        };
        firebaseAuth.addAuthStateListener(firebaseAuthListener);
        initComponents();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkSignedInStatus();
    }

    private void checkSignedInStatus()
    {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null)
        {
            // Kick the user from this activity if they are not signed in.
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        firebaseAuth.removeAuthStateListener(firebaseAuthListener);

        for (DatabaseReference reference : listeners.keySet())
        {
            Object listener = listeners.get(reference);
            if (listener instanceof ValueEventListener)
            {
                reference.removeEventListener((ValueEventListener)listener);
            }
            else if (listener instanceof ChildEventListener)
            {
                reference.removeEventListener((ChildEventListener) listener);
            }
        }
    }

    private void initComponents()
    {
        spnGameType = findViewById(R.id.newgame_spnGameType);
        spnOpponent = findViewById(R.id.newgame_spnOpponent);
        btnCreateGame = findViewById(R.id.newgame_btnCreateGame);
        edtUsername = findViewById(R.id.newgame_edtUsername);
        loadingDialog = new LoadingDialog(this, "Loading", "Handling game request...");

        ArrayAdapter<CharSequence> gameTypeAdapter = ArrayAdapter.createFromResource(this, R.array.newgame_gameTypes, android.R.layout.simple_spinner_item);
        gameTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnGameType.setAdapter(gameTypeAdapter);

        ArrayAdapter<CharSequence> opponentAdapter = ArrayAdapter.createFromResource(this, R.array.newgame_opponentTypes, android.R.layout.simple_spinner_item);
        opponentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnOpponent.setAdapter(opponentAdapter);

        spnOpponent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spnOpponent.getSelectedItem().toString().equals(getResources().getStringArray(R.array.newgame_opponentTypes)[0]))
                {
                    //another user selected
                    edtUsername.setVisibility(View.VISIBLE);
                }
                else {
                    edtUsername.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnCreateGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog.show();
                if (spnOpponent.getSelectedItem().toString().equals(getResources().getStringArray(R.array.newgame_opponentTypes)[0]))
                {
                    //another user
                    // Send the request and when it succeeds, close the activity
                    CWHRequest cwhRequest = new CWHRequest(firebaseAuth.getCurrentUser(), CWHRequest.RequestType.GAME_CREATION, new OnCWHResponseListener() {
                        @Override
                        public void onCWHResponse(CWHResponse response) {
                            if (response.isSuccess()) {
                                NewGameActivity.this.finish();
                                Toast.makeText(NewGameActivity.this, edtUsername.getText().toString() + " has been invited to play!", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(NewGameActivity.this, "Failed to create game! Are you connected to the internet?", Toast.LENGTH_LONG).show();
                            }
                            loadingDialog.dismiss();
                        }
                    });
                    cwhRequest.getExtras().put("friend", edtUsername.getText().toString());
                    cwhRequest.getExtras().put("gametype", spnGameType.getSelectedItem().toString());
                    cwhRequest.sendRequest(NewGameActivity.this);
                }
                else if (spnOpponent.getSelectedItem().toString().equals(getResources().getStringArray(R.array.newgame_opponentTypes)[1])) {
                    //computer
                    CWHRequest cwhRequest = new CWHRequest(firebaseAuth.getCurrentUser(), CWHRequest.RequestType.COMPUTER_GAME, new OnCWHResponseListener() {
                        @Override
                        public void onCWHResponse(CWHResponse response) {
                            if (response.isSuccess()) {
                                NewGameActivity.this.finish();
                                Toast.makeText(NewGameActivity.this, "A game against the computer has been created!", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(NewGameActivity.this, "Failed to create game! Are you connected to the internet?", Toast.LENGTH_LONG).show();
                            }
                            loadingDialog.dismiss();
                        }
                    });
                    cwhRequest.getExtras().put("gametype", spnGameType.getSelectedItem().toString());
                    cwhRequest.sendRequest(NewGameActivity.this);
                } else if (spnOpponent.getSelectedItem().toString().equals(getResources().getStringArray(R.array.newgame_opponentTypes)[2])) {
                    //random
                    CWHRequest cwhRequest = new CWHRequest(firebaseAuth.getCurrentUser(), CWHRequest.RequestType.MATCHMAKING_REQUEST, new OnCWHResponseListener() {
                        @Override
                        public void onCWHResponse(CWHResponse response) {
                            if (response.isSuccess()) {
                                NewGameActivity.this.finish();
                                Toast.makeText(NewGameActivity.this, "You have been added to the matchmaking pool!", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(NewGameActivity.this, "Failed to create game! Are you connected to the internet?", Toast.LENGTH_LONG).show();
                            }
                            loadingDialog.dismiss();
                        }
                    });
                    cwhRequest.getExtras().put("gametype", spnGameType.getSelectedItem().toString());
                    cwhRequest.sendRequest(NewGameActivity.this);
                } else if (spnOpponent.getSelectedItem().toString().equals(getResources().getStringArray(R.array.newgame_opponentTypes)[3])) {
                    //ranked
                    CWHRequest cwhRequest = new CWHRequest(firebaseAuth.getCurrentUser(), CWHRequest.RequestType.MATCHMAKING_REQUEST, new OnCWHResponseListener() {
                        @Override
                        public void onCWHResponse(CWHResponse response) {
                            if (response.isSuccess()) {
                                NewGameActivity.this.finish();
                                Toast.makeText(NewGameActivity.this, "You have been added to the matchmaking pool!", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(NewGameActivity.this, "Failed to create game! Are you connected to the internet?", Toast.LENGTH_LONG).show();
                            }
                            loadingDialog.dismiss();
                        }
                    });
                    cwhRequest.getExtras().put("gametype", "Ranked " + spnGameType.getSelectedItem().toString());
                    cwhRequest.sendRequest(NewGameActivity.this);
                }
            }
        });

        final ArrayList<String> friends = new ArrayList<String>();
        final ArrayAdapter<String> autocompleteAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, friends);

        DatabaseReference friendsReference = firebaseDatabase.getReference().child("users").child(firebaseAuth.getCurrentUser().getUid()).child("friends");
        ChildEventListener friendsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                autocompleteAdapter.add(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                autocompleteAdapter.remove(dataSnapshot.getValue(String.class));
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
        
        edtUsername.setAdapter(autocompleteAdapter);
    }
}
