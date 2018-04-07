package com.group8.chesswithhats;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.group8.chesswithhats.server.CWHRequest;
import com.group8.chesswithhats.server.CWHResponse;
import com.group8.chesswithhats.server.OnCWHResponseListener;
import com.group8.chesswithhats.util.LoadingDialog;

/*
 * @author Philip Rodriguez
 */
public class CreateAccountActivity extends Activity {

    private EditText edtEmailAddress;
    private EditText edtUsername;
    private EditText edtPassword;
    private Button btnCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Initialize GUI components
        initComponents();
    }

    private void initComponents()
    {
        edtEmailAddress = findViewById(R.id.createaccount_edtEmailAddress);
        edtUsername = findViewById(R.id.createaccount_edtUsername);
        edtPassword = findViewById(R.id.createaccount_edtPassword);
        btnCreateAccount = findViewById(R.id.createaccount_btnCreateAccount);

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doAccountCreation();
            }
        });
    }

    private void doAccountCreation()
    {
        // Create and show a loading dialog...
        final LoadingDialog loadingDialog = new LoadingDialog(this, "Loading", "Creating account...");
        loadingDialog.show();

        // Now send a request (null user since no currently signed in user to pass in!)
        CWHRequest cwhRequest = new CWHRequest(null, CWHRequest.RequestType.CREATE_ACCOUNT, new OnCWHResponseListener() {
            @Override
            public void onCWHResponse(CWHResponse response) {
                if (response.isSuccess())
                {
                    // Since we succeeded, kill the create account activity to get back to the login activity!
                    Toast.makeText(CreateAccountActivity.this, "Account created!", Toast.LENGTH_LONG).show();
                    CreateAccountActivity.this.finish();
                }
                else
                {
                    // Since something went wrong, tell the user!
                    Toast.makeText(CreateAccountActivity.this, "Failed to create account:\n" + response.getMessage(), Toast.LENGTH_LONG).show();
                }

                // Dismiss the loading dialog, regardless of the result!
                loadingDialog.dismiss();
            }
        });
        cwhRequest.getExtras().put("email", edtEmailAddress.getText().toString());
        cwhRequest.getExtras().put("username", edtUsername.getText().toString());
        cwhRequest.getExtras().put("password", edtPassword.getText().toString());
        cwhRequest.sendRequest(this);
    }
}
