package com.group8.chesswithhats;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

/*
    @author Philip Rodriguez
 */
public class ManageAccountActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener firebaseAuthListener;

    FirebaseDatabase firebaseDatabase;
    HashMap<DatabaseReference, Object> listeners;

    TextView txtEmail;
    TextView txtUsername;
    Button btnEditPassword;
    TextView txtRank;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_account);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                checkSignedInStatus();
            }
        };
        firebaseAuth.addAuthStateListener(firebaseAuthListener);

        firebaseDatabase = FirebaseDatabase.getInstance();
        listeners = new HashMap<>();

        initComponents();
    }

    private void checkSignedInStatus()
    {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null)
        {
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkSignedInStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        firebaseAuth.removeAuthStateListener(firebaseAuthListener);

        for (DatabaseReference reference : listeners.keySet())
        {
            Object listener = listeners.get(reference);
            if (listener instanceof ValueEventListener)
            {
                reference.removeEventListener((ValueEventListener)listener);
            }
            else if (listener instanceof ChildEventListener)
            {
                reference.removeEventListener((ChildEventListener) listener);
            }
        }
    }

    private void initComponents()
    {
        txtEmail = findViewById(R.id.manageaccount_txtEmail);
        txtUsername = findViewById(R.id.manageaccount_txtUsername);
        btnEditPassword = findViewById(R.id.manageaccount_btnEditPassword);
        txtRank = findViewById(R.id.manageaccount_txtRank);

        txtEmail.setText("Email: " + firebaseAuth.getCurrentUser().getEmail());
        txtUsername.setText("Username: " + firebaseAuth.getCurrentUser().getDisplayName());

        btnEditPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.sendPasswordResetEmail(firebaseAuth.getCurrentUser().getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(ManageAccountActivity.this, "A password reset email has been sent!", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Toast.makeText(ManageAccountActivity.this, "Failed to send a password reset email!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        DatabaseReference rankReference = firebaseDatabase.getReference().child("users").child(firebaseAuth.getCurrentUser().getUid()).child("rank");
        ValueEventListener rankListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer rank = dataSnapshot.getValue(Integer.class);
                txtRank.setText("Rank: " + rank);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        rankReference.addValueEventListener(rankListener);
        listeners.put(rankReference, rankListener);
    }
}
