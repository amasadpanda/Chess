package com.group8.chesswithhats.util;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.TextView;

import com.group8.chesswithhats.R;

/*
 * So, Android decided to deprecate ProgressDialog. I'm going to make my own, then, because having
 * a loading dialog that blocks user input beats having literally no loading indication, and I don't
 * have the dang time to make custom loading UI integrations on a case by case basis for this.
 *
 * @author Philip Rodriguez
 */
public class LoadingDialog {

    private final AlertDialog dialog;
    private final String message;

    public LoadingDialog(Context context, String title, String message) {
        dialog = new AlertDialog.Builder(context).setView(R.layout.loading_dialog).create();
        dialog.setTitle(title);
        this.message = message;
        dialog.setCancelable(false);
    }

    public void show()
    {
        dialog.show();
        ((TextView)dialog.findViewById(R.id.pd_txtMessage)).setText(message);
    }

    public void dismiss()
    {
        dialog.dismiss();
    }
}
