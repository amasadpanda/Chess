package com.group8.chesswithhats;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class NewGameActivity extends AppCompatActivity {

    private Spinner spnGameType;
    private Spinner spnOpponent;
    private Button btnCreateGame;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);
        initComponents();
    }

    private void initComponents()
    {
        spnGameType = findViewById(R.id.newgame_spnGameType);
        spnOpponent = findViewById(R.id.newgame_spnOpponent);
        btnCreateGame = findViewById(R.id.newgame_btnCreateGame);

        ArrayAdapter<CharSequence> gameTypeAdapter = ArrayAdapter.createFromResource(this, R.array.newgame_gameTypes, android.R.layout.simple_spinner_item);
        gameTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnGameType.setAdapter(gameTypeAdapter);

        ArrayAdapter<CharSequence> opponentAdapter = ArrayAdapter.createFromResource(this, R.array.newgame_opponentTypes, android.R.layout.simple_spinner_item);
        opponentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnOpponent.setAdapter(opponentAdapter);

        btnCreateGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send the request and when it succeeds, close the activity
            }
        });
    }
}
