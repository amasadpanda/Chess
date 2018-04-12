package com.group8.chesswithhats.util;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.group8.chesswithhats.GameActivity;
import com.group8.chesswithhats.R;

public class CurrentGameView extends FrameLayout {

    private final String gameID;
    private final String opponent;

    private View subView;

    public CurrentGameView(final Context context, final String gameID, final String opponent) {
        super(context);
        this.gameID = gameID;
        this.opponent = opponent;
        this.subView = inflate(context, R.layout.current_game_list_item, null);

        TextView txtOpponent = this.subView.findViewById(R.id.cg_txtOpponent);
        final TextView txtGameType = this.subView.findViewById(R.id.cg_txtGameType);

        // Set opponent text
        txtOpponent.setText(opponent);

        // Set the game type text
        FirebaseDatabase.getInstance().getReference().child("games").child(gameID).child("gametype").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                txtGameType.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to fetch game type
                txtGameType.setText(" ");
            }
        });

        this.subView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // We need to open the relevant game activity
                Intent gameActivityIntent = new Intent(context, GameActivity.class);
                gameActivityIntent.putExtra("gameid", gameID);
                context.startActivity(gameActivityIntent);
            }
        });

        addView(this.subView);
    }

    public String getGameID() {
        return gameID;
    }

    public String getOpponent() {
        return opponent;
    }
}
