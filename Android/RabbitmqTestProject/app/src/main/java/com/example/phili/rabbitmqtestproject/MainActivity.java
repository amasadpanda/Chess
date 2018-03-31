package com.example.phili.rabbitmqtestproject;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.FirebaseApp;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    private Context mainContext;

    Button sendButton;
    EditText messageContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mainContext = this;
        setContentView(R.layout.activity_main);
        sendButton = (Button) findViewById(R.id.sendButton);
        messageContent = (EditText) findViewById(R.id.messageContent);

        useRequestClass();
    }

    private void testFirebaseUI()
    {
        FirebaseApp.initializeApp(this);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());
                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(), 123);
            }
        });
    }

    //Set up request publisher and pipe messages to enqueuer that way
    private void useRequestClass()
    {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long start = System.currentTimeMillis();
                for(int i = 0; i < 10000; i++)
                {
                    Request request = new Request("authid", Request.RequestType.MAKE_MOVE, new RequestCompletionListener() {
                        @Override
                        public void requestCompleted(Request.Response response) {
                            System.out.println("Request completed! " + response.getMessage());
                        }
                    });
                    request.getRequestData().put("date", new Date().toString());
                    request.getRequestData().put("millis", Long.toString(System.currentTimeMillis()));
                    request.sendRequest(mainContext);
                }
                long end = System.currentTimeMillis();
                System.out.println("Ran in " + (end-start) + "ms");
            }
        });
    }
}
