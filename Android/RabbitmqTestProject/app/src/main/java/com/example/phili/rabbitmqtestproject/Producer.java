package com.example.phili.rabbitmqtestproject;

/**
 * Created by phili on 3/8/2018.
 */

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeoutException;

public class Producer implements Runnable {
    private final String queueName;
    private Connection connection;
    private Channel channel;
    private final Queue<String> messagesToSend;

    public Producer(String queueName)
    {
        this.queueName = queueName;
        this.messagesToSend = new LinkedList<String>();
    }

    private synchronized void connect() throws IOException, TimeoutException {
        if (connection == null)
        {
            //Do the thing!
            ConnectionFactory factory = new ConnectionFactory();
            factory.setUsername("sopwyin54");
            factory.setPassword("soprod");
            factory.setHost("192.168.56.1");
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(queueName, false, false, false, null);
        }
        else
        {
            //Already connected!
        }
    }

    private synchronized void disconnect() throws IOException, TimeoutException {
        if (connection != null && channel != null)
        {
            //Do the disconnecting!
            channel.close();
            connection.close();
            channel = null;
            connection = null;
        }
        else
        {
            //Already not connected!
        }
    }

    public void enqueueMessage(String message)
    {
        synchronized (messagesToSend) {
            this.messagesToSend.add(message);
            this.messagesToSend.notify();
        }
    }

    private void sendMessage(String message) throws IOException {
        channel.basicPublish("", queueName, null, message.getBytes(Charset.forName("UTF-8")));
    }

    @Override
    public void run() {
        System.out.println("Thread starting!");
        try {
            connect();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        while (!Thread.interrupted())
        {
            synchronized (messagesToSend)
            {
                try {
                    messagesToSend.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    sendMessage(messagesToSend.poll());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        System.out.println("Thread exiting!");
    }
}
