package com.group8.chesswithhats.util;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.group8.chesswithhats.GameActivity;
import com.group8.chesswithhats.R;

public class CurrentGameView extends FrameLayout {

    private final String gameID;
    private final String opponent;
    private final String gameType;

    private View subView;

    public CurrentGameView(final Context context, final String gameID, final String opponent, String gameType) {
        super(context);
        this.gameID = gameID;
        this.opponent = opponent;
        this.gameType = gameType;
        this.subView = inflate(context, R.layout.current_game_list_item, null);

        TextView txtOpponent = this.subView.findViewById(R.id.cg_txtOpponent);
        TextView txtGameType = this.subView.findViewById(R.id.cg_txtGameType);

        txtOpponent.setText(opponent);
        txtGameType.setText(gameType);

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

    public String getGameType() {
        return gameType;
    }
}
