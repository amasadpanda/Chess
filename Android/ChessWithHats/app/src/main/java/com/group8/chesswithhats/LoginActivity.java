package com.group8.chesswithhats;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.group8.chesswithhats.util.LoadingDialog;

/*
 * @author Philip Rodriguez
 */
public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private EditText edtEmailAddress;
    private EditText edtPassword;
    private Button btnCreateAccount;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize the FirebaseAuth instance
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        initComponents();
    }

    private void initComponents()
    {
        // Assign UI component objects
        edtEmailAddress = findViewById(R.id.login_edtEmailAddress);
        edtPassword = findViewById(R.id.login_edtPassword);
        btnLogin = findViewById(R.id.login_btnLogin);
        btnCreateAccount = findViewById(R.id.login_btnCreateAccount);

        // Set listeners and whatnot
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // They pressed the actual button!
                doLogin();
            }
        });

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the create account activity!
                Intent createAccountActivityIntent = new Intent(LoginActivity.this, CreateAccountActivity.class);
                startActivity(createAccountActivityIntent);
            }
        });

        edtPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO)
                {
                    // They pressed the keyboard done button thing
                    doLogin();
                    return true;
                }
                return false;
            }
        });

    }

    /*
     * Uses the current field information to attempt to log in.
     */
    private void doLogin()
    {
        String email = edtEmailAddress.getText().toString();
        String password = edtPassword.getText().toString();

        // Here we can put constraints on the input text!
        if (email.length() < 1 || password.length() < 1)
        {
            Toast.makeText(this, "Please enter an email and password.", Toast.LENGTH_LONG).show();
            return;
        }

        // Block the user from trying again while we wait for callbacks
        final LoadingDialog loadingDialog = new LoadingDialog(this, "Loading", "Logging in...");
        loadingDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    // Login succeeded! :)   Now, we can open the home activity.
                    onSuccessfulLogin();
                }
                else
                {
                    // Login failed
                    Toast.makeText(LoginActivity.this, "Login failed! Please try again...", Toast.LENGTH_LONG).show();
                }

                // Re-enable ability to login!
                loadingDialog.dismiss();
            }
        });
    }

    private void onSuccessfulLogin()
    {
        // Basically, now that we are logged in we can move to the home activity!
        Intent homeActivityIntent = new Intent(LoginActivity.this, HomeActivity.class);

        // Close this login activity before moving on to the home activity.
        LoginActivity.this.finish();

        // Start the home activity!
        startActivity(homeActivityIntent);
    }


    @Override
    protected void onStart() {
        super.onStart();

        // Check if the user is signed in
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null)
        {
            // They are already signed in!
            onSuccessfulLogin();
        }
    }
}
