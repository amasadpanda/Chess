package com.example.phili.rabbitmqtestproject;

/**
 * Created by phili on 3/8/2018.
 */

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

/**
 * Literally exists to push responseless requests to the backend
 *
 * @author Philip Rodriguez
 */
public class RequestPusher implements Runnable {
    private final BlockingQueue<byte[]> messagesToSend;
    private final String enqueuerHTTPAddress;
    private final Context mainContext;

    public RequestPusher(String enqueuerHTTPAddress, Context mainContext)
    {
        messagesToSend = new LinkedBlockingQueue<byte[]>();
        this.mainContext = mainContext;
        this.enqueuerHTTPAddress = enqueuerHTTPAddress;
    }

    public void pushRequest(byte[] message)
    {
        messagesToSend.add(message);
    }

    @Override
    public void run() {
        while(true)
        {
            try {
                // Take out a message, once available (blocks until available)
                byte[] messageContent = messagesToSend.take();
                System.out.println("Sending a message to the server...");

                // Send the message to our enqueuer!
                HttpsURLConnection connection = null;
                try
                {
                    // Open basic connection
                    URL url = new URL(enqueuerHTTPAddress);

                    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                    BufferedInputStream inputStream = new BufferedInputStream(mainContext.getAssets().open("ChessWithHatsCert.cer"));
                    Certificate certificate = certificateFactory.generateCertificate(inputStream);
                    inputStream.close();
                    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    keyStore.load(null, null);
                    keyStore.setCertificateEntry("certificate", certificate);
                    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    trustManagerFactory.init(keyStore);
                    SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                    sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

                    connection = (HttpsURLConnection)url.openConnection();
                    connection.setSSLSocketFactory(sslContext.getSocketFactory());
                    connection.setHostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String s, SSLSession sslSession) {
                            return true; //TODO: must change hostname verification! Only for testing...
                        }
                    });

                    // Request method to POST
                    connection.setRequestMethod("POST");

                    // Tell the server we're sending arbitrary binary data
                    connection.setRequestProperty("Content-Type", "application/octet-stream");

                    // Tell the server the length of our request in bytes
                    connection.setRequestProperty("Content-Length", Integer.toString(messageContent.length));

                    // Not entirely sure what this practically affects...
                    connection.setUseCaches(false);

                    // Set this to true since we will be outputting content (bytes) to the server
                    connection.setDoOutput(true);

                    // Send some actual data!
                    BufferedOutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
                    outputStream.write(messageContent);
                    outputStream.flush();
                    outputStream.close();

                    // Get an OK response
                    BufferedReader inputReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while((responseLine = inputReader.readLine()) != null)
                    {
                        response.append(responseLine);
                        response.append("\n");
                    }
                    System.out.println("Got response from server: " + response.toString());
                    inputReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (CertificateException e) {
                    e.printStackTrace();
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null)
                        connection.disconnect();
                }


            } catch (InterruptedException e) {
                System.err.println("RequestPublisher loop exception occurred! " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
