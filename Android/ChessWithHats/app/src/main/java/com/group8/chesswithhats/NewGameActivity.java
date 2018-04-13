package com.group8.chesswithhats;

import android.os.Bundle;
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
import com.group8.chesswithhats.server.CWHRequest;
import com.group8.chesswithhats.server.CWHResponse;
import com.group8.chesswithhats.server.OnCWHResponseListener;

/*
    @author Philip Rodriguez
 */
public class NewGameActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private Spinner spnGameType;
    private Spinner spnOpponent;
    private Button btnCreateGame;

    private AutoCompleteTextView edtUsername;

    private OnCWHResponseListener generalListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);
        firebaseAuth = FirebaseAuth.getInstance();
        initComponents();
    }

    private void initComponents()
    {
        spnGameType = findViewById(R.id.newgame_spnGameType);
        spnOpponent = findViewById(R.id.newgame_spnOpponent);
        btnCreateGame = findViewById(R.id.newgame_btnCreateGame);
        edtUsername = findViewById(R.id.newgame_edtUsername);

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

        generalListener = new OnCWHResponseListener() {
            @Override
            public void onCWHResponse(CWHResponse response) {
                if (response.isSuccess()) {
                    NewGameActivity.this.finish();
                } else {
                    Toast.makeText(NewGameActivity.this, "Failed to create game! Are you connected to the internet?", Toast.LENGTH_LONG).show();
                }
            }
        };

        btnCreateGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spnOpponent.getSelectedItem().toString().equals(getResources().getStringArray(R.array.newgame_opponentTypes)[0]))
                {
                    //another user
                    // Send the request and when it succeeds, close the activity
                    CWHRequest cwhRequest = new CWHRequest(firebaseAuth.getCurrentUser(), CWHRequest.RequestType.GAME_CREATION, generalListener);
                    cwhRequest.getExtras().put("friend", edtUsername.getText().toString());
                    cwhRequest.getExtras().put("gametype", spnGameType.getSelectedItem().toString());
                    cwhRequest.sendRequest(NewGameActivity.this);
                }
                else if (spnOpponent.getSelectedItem().toString().equals(getResources().getStringArray(R.array.newgame_opponentTypes)[1])) {
                    //computer
                    CWHRequest cwhRequest = new CWHRequest(firebaseAuth.getCurrentUser(), CWHRequest.RequestType.COMPUTER_GAME, generalListener);
                    cwhRequest.getExtras().put("gametype", spnGameType.getSelectedItem().toString());
                    cwhRequest.sendRequest(NewGameActivity.this);
                } else if (spnOpponent.getSelectedItem().toString().equals(getResources().getStringArray(R.array.newgame_opponentTypes)[2])) {
                    //random
                    CWHRequest cwhRequest = new CWHRequest(firebaseAuth.getCurrentUser(), CWHRequest.RequestType.MATCHMAKING_REQUEST, generalListener);
                    cwhRequest.getExtras().put("gametype", spnGameType.getSelectedItem().toString());
                    cwhRequest.sendRequest(NewGameActivity.this);
                } else if (spnOpponent.getSelectedItem().toString().equals(getResources().getStringArray(R.array.newgame_opponentTypes)[3])) {
                    //ranked
                    CWHRequest cwhRequest = new CWHRequest(firebaseAuth.getCurrentUser(), CWHRequest.RequestType.MATCHMAKING_REQUEST, generalListener);
                    cwhRequest.getExtras().put("gametype", "Ranked " + spnGameType.getSelectedItem().toString());
                    cwhRequest.sendRequest(NewGameActivity.this);
                }
            }
        });
    }
}
