package com.example.phili.requestexampleapplication;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth.getInstance().signInWithEmailAndPassword("philipjamesrodriguez@gmail.com", "mypassword").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    System.out.println("Signed in as " + task.getResult().getUser().getEmail());
                }
                else
                {
                    System.out.println("Failed to sign in");
                }
            }
        });
    }

    public void doSend(View view)
    {
        CWHRequest request = new CWHRequest(FirebaseAuth.getInstance().getCurrentUser(), CWHRequest.RequestType.MAKE_MOVE, new OnCWHResponseListener() {
            @Override
            public void onCWHResponse(CWHResponse response) {
                System.out.println("Server responded!");
                System.out.println("Message: " + response.getMessage());
                System.out.println("Success Status: " + response.isSuccess());
            }
        });
        request.getExtras().put("extra1", "smoe value 1");
        request.getExtras().put("extra2", "smoe value 2");
        request.sendRequest(this);
    }
}
