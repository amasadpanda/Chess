package com.group8.chesswithhats.util;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.group8.chesswithhats.R;

public class CurrentFriendView extends FrameLayout {

    private final Context context;
    private final String username;
    private final String uid;

    private View subView;

    public CurrentFriendView(Context context, String username, String uid)
    {
        super(context);
        this.context = context;
        this.username = username;
        this.uid = uid;
        this.subView = inflate(context, R.layout.current_friend_list_item, null);

        TextView txtUsername = this.subView.findViewById(R.id.cf_txtUsername);
        txtUsername.setText(username);

        addView(subView);
    }

    public String getUsername() {
        return username;
    }

    public String getUid() {
        return uid;
    }
}
