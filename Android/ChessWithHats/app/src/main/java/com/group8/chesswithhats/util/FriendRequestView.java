package com.group8.chesswithhats.util;

import android.content.Context;
import android.support.annotation.NonNull;
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

public class FriendRequestView extends FrameLayout {
    private final Context context;
    private final FirebaseAuth firebaseAuth;
    private final String username;
    private final String UID;

    private View subView;

    public FriendRequestView(@NonNull final Context context, String UID, String username, final FirebaseAuth firebaseAuth) {
        super(context);
        this.context = context;
        this.UID = UID;
        this.username = username;
        this.firebaseAuth = firebaseAuth;
        this.subView = inflate(context, R.layout.friend_request_list_item, null);

        final TextView txtUsername = this.subView.findViewById(R.id.fr_txtUsername);
        Button btnAccept = this.subView.findViewById(R.id.fr_btnAccept);
        Button btnReject = this.subView.findViewById(R.id.fr_btnReject);

        txtUsername.setText(username);
        btnAccept.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Accept the friend request from user with uid UID.

            }
        });

        btnReject.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reject the friend request from user with uid UID.
            }
        });
    }

    public String getUID() {
        return UID;
    }

    public String getUsername() {
        return username;
    }
}
