package com.example.phili.requestexampleapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void doSend(View view)
    {
        new CWHRequest("blahblahmyauthid", CWHRequest.RequestType.LOGIN_NOTIFICATION, new OnCWHResponseListener() {
            @Override
            public void onCWHResponse(CWHResponse response) {
                System.out.println("Server responded!");
                System.out.println("Message: " + response.getMessage());
                System.out.println("Success Status: " + response.isSuccess());
            }
        }).sendRequest(this);
    }
}
