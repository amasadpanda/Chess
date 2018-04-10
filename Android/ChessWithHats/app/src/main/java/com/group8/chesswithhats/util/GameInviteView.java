package com.group8.chesswithhats.util;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.group8.chesswithhats.R;
import com.group8.chesswithhats.server.CWHRequest;
import com.group8.chesswithhats.server.CWHResponse;
import com.group8.chesswithhats.server.OnCWHResponseListener;

public class GameInviteView extends FrameLayout {
    private final Context context;
    private final FirebaseAuth firebaseAuth;
    private final String correspondingGameID;
    private final String senderUsername;
    private final String gameType;

    private View subView;

    public GameInviteView(Context context, final String correspondingGameID, String senderUsername, String gameType, FirebaseAuth firebaseAuth)
    {
        super(context);
        this.context = context;
        this.correspondingGameID = correspondingGameID;
        this.senderUsername = senderUsername;
        this.gameType = gameType;
        this.firebaseAuth = firebaseAuth;
        this.subView = inflate(context, R.layout.game_invite_list_item, null);

        TextView txtUsername = subView.findViewById(R.id.gi_txtUsername);
        TextView txtGameType = subView.findViewById(R.id.gi_txtGameType);
        Button btnAcceptInvite = subView.findViewById(R.id.gi_btnAcceptInvite);
        Button btnRejectInvite = subView.findViewById(R.id.gi_btnRejectInvite);

        txtUsername.setText(senderUsername);
        txtGameType.setText(gameType);

        btnAcceptInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CWHRequest cwhRequest = new CWHRequest(GameInviteView.this.firebaseAuth.getCurrentUser(), CWHRequest.RequestType.ACCEPT_GAME, new OnCWHResponseListener() {
                    @Override
                    public void onCWHResponse(CWHResponse response) {
                        if (response.isSuccess())
                        {
                            // Good
                            Toast.makeText(GameInviteView.this.context, "Invite accepted!", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            // Bad!
                            Toast.makeText(GameInviteView.this.context, "Failed to accept invite! Are you connected to the internet?", Toast.LENGTH_LONG).show();
                            System.out.println(response.getMessage());
                        }
                        setEnabled();
                    }
                });
                cwhRequest.getExtras().put("gameid", correspondingGameID);
                setDisabled();
                cwhRequest.sendRequest(GameInviteView.this.context);
            }
        });

        btnRejectInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CWHRequest cwhRequest = new CWHRequest(GameInviteView.this.firebaseAuth.getCurrentUser(), CWHRequest.RequestType.DENY_GAME, new OnCWHResponseListener() {
                    @Override
                    public void onCWHResponse(CWHResponse response) {
                        if (response.isSuccess())
                        {
                            // Good
                            Toast.makeText(GameInviteView.this.context, "Invite rejected!", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            // Bad!
                            Toast.makeText(GameInviteView.this.context, "Failed to reject invite! Are you connected to the internet?", Toast.LENGTH_LONG).show();
                            System.out.println(response.getMessage());
                        }
                        setEnabled();
                    }
                });
                cwhRequest.getExtras().put("gameid", correspondingGameID);
                setDisabled();
                cwhRequest.sendRequest(GameInviteView.this.context);
            }
        });

        this.addView(subView);
    }

    public void setDisabled()
    {
        subView.setBackgroundColor(context.getResources().getColor(R.color.colorBusyBackground));
        Button btnAcceptInvite = subView.findViewById(R.id.gi_btnAcceptInvite);
        Button btnRejectInvite = subView.findViewById(R.id.gi_btnRejectInvite);
        btnAcceptInvite.setEnabled(false);
        btnRejectInvite.setEnabled(false);
    }

    public void setEnabled()
    {
        subView.setBackgroundColor(Color.TRANSPARENT);
        Button btnAcceptInvite = subView.findViewById(R.id.gi_btnAcceptInvite);
        Button btnRejectInvite = subView.findViewById(R.id.gi_btnRejectInvite);
        btnAcceptInvite.setEnabled(true);
        btnRejectInvite.setEnabled(true);
    }

    public String getCorrespondingGameID() {
        return correspondingGameID;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public String getGameType() {
        return gameType;
    }
}
