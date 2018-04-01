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
        CWHRequest request = new CWHRequest("blahblahmyauthid", CWHRequest.RequestType.MAKE_MOVE, new OnCWHResponseListener() {
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
