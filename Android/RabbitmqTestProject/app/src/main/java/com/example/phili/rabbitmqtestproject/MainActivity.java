package com.example.phili.rabbitmqtestproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    Button sendButton;
    EditText messageContent;

    Producer p;
    RequestPusher rp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendButton = (Button) findViewById(R.id.sendButton);
        messageContent = (EditText) findViewById(R.id.messageContent);

        useRequestPublisher();
    }

    //Set up request publisher and pipe messages to enqueuer that way
    private void useRequestPublisher()
    {
        rp = new RequestPusher("https://192.168.1.14:1357/enqueuer1/", this);
        new Thread(rp).start();
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rp.pushRequest(messageContent.getText().toString().getBytes(Charset.forName("UTF-8")));
                messageContent.setText("");
            }
        });
    }

    //Set up producer and pipe messages thru that way
    private void useRabbitMQ()
    {
        p = new Producer("mainQueue");
        new Thread(p).start();
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                p.enqueueMessage(messageContent.getText().toString());
                messageContent.setText("");
            }
        });
    }
}
