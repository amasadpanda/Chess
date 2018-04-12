package com.group8.chesswithhats;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class GameActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;

    private String gameID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameID = getIntent().getStringExtra("gameid");
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_game);
    }

    // Check if the user is signed in. If not, close the activity
    private void checkLoginStatus()
    {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null)
        {
            // We are not signed in!
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkLoginStatus();
    }
}
