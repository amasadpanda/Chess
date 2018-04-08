package com.group8.chesswithhats;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.group8.chesswithhats.server.CWHRequest;
import com.group8.chesswithhats.server.CWHResponse;
import com.group8.chesswithhats.server.OnCWHResponseListener;

/*
 * @author Philip Rodriguez
 */
public class HomeActivity extends Activity {

    private FirebaseAuth firebaseAuth;

    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    private TextView txtExampleText;
    private Button btnSendTestRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        // If they sign out on us, handle it.
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                checkSignedInStatus();
            }
        };
        firebaseAuth.addAuthStateListener(firebaseAuthListener);

        // Initialize GUI components
        initComponents();
    }

    private void initComponents()
    {
        txtExampleText = findViewById(R.id.home_txtExampleText);
        txtExampleText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // sign them out
                Toast.makeText(HomeActivity.this, "signing out...", Toast.LENGTH_SHORT).show();
                firebaseAuth.signOut();
            }
        });

        btnSendTestRequest = findViewById(R.id.home_btnSendTestRequest);
        btnSendTestRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CWHRequest request = new CWHRequest(firebaseAuth.getCurrentUser(), CWHRequest.RequestType.MATCHMAKING_REQUEST, new OnCWHResponseListener() {
                    @Override
                    public void onCWHResponse(CWHResponse response) {
                        System.out.println(response);
                    }
                });
                request.sendRequest(HomeActivity.this);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkSignedInStatus();
    }

    private void checkSignedInStatus()
    {
        // Make sure we are signed in. If not, kick out of home activity!
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null)
        {
            // We are not signed in! Kick them out!
            // Remove the listener so we do not keep opening many login activities!
            firebaseAuth.removeAuthStateListener(firebaseAuthListener);

            // Notify the user of the sign out
            Toast.makeText(this, "You have been signed out.", Toast.LENGTH_LONG).show();

            // End this home activity...
            finish();

            // Go back to login activity.
            Intent loginActivityIntent = new Intent(this, LoginActivity.class);
            startActivity(loginActivityIntent);
        }
        else
        {
            // TODO: you can remove this else branch completely
            txtExampleText.setText("Signed in as " + firebaseAuth.getCurrentUser().getEmail());
            Toast.makeText(this, "signed in as " + firebaseAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
        }
    }
}
