package com.group8.chesswithhats.server;


import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

/*
 * @author Philip Rodriguez
 */
public class CWHRequest extends AsyncTask<Context, Void, CWHResponse> {
    //public static final String serverURL = "https://philiprodriguez.ddns.net:1235/chessWithHats/";
    public static String serverURL = null;

    // Blocking call that ensures that we know the server URL!
    public static void ensureServerURLSet()
    {
        // If already set,
        if (serverURL != null)
            return;

        final Semaphore setMutex = new Semaphore(0);
        FirebaseDatabase.getInstance().getReference().child("serverURL").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                serverURL = dataSnapshot.getValue(String.class);
                setMutex.release();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        try {
            setMutex.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public enum RequestType {
        MAKE_MOVE, ACCEPT_FRIEND, DENY_FRIEND, ACCEPT_GAME, DENY_GAME, FRIEND_REQUEST, GAME_CREATION,
        MATCHMAKING_REQUEST, LEAVE_GAME, CREATE_ACCOUNT, COMPUTER_GAME
    }

    private Semaphore authIDSet;

    @Expose
    private String authID;

    @Expose
    private RequestType requestType;

    @Expose
    private HashMap<String, String> extras;

    private OnCWHResponseListener onCWHResponseListener;

    public CWHRequest(FirebaseUser user, RequestType requestType, OnCWHResponseListener onCWHResponseListener)
    {
        authIDSet = new Semaphore(0);
        if (user != null) {
            user.getIdToken(false).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {
                    authID = task.getResult().getToken();
                    authIDSet.release();
                }
            });
        }
        else
        {
            authID = "";
            authIDSet.release();
        }
        this.requestType = requestType;
        this.extras = new HashMap<String, String>();
        this.onCWHResponseListener = onCWHResponseListener;
    }

    public String getAuthID() {
        return authID;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public HashMap<String, String> getExtras() {
        return extras;
    }

    public void put(String key, String value) { extras.put(key, value); }

    @Override
    protected CWHResponse doInBackground(Context... contexts) {
        HttpsURLConnection connection = null;
        try
        {
            // Ensure server url exists
            ensureServerURLSet();

            // Wait to ensure the authID is assigned
            if (authID == null) {
                authIDSet.acquire();
            }

            // Open basic connection
            URL url = new URL(serverURL);

            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            BufferedInputStream inputStream = new BufferedInputStream(contexts[0].getAssets().open("ChessWithHatsCert.cer"));
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

            // If we can't connect in 5 whole seconds, give up!
            connection.setConnectTimeout(5000);

            connection.setSSLSocketFactory(sslContext.getSocketFactory());
            connection.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    // Because we are using a self-signed certificate. If actually pushing this
                    // product to a real launch, we'd want to use a CA and all. But, at least this
                    // gives us encrypted channels between client and server.
                    return true;
                }
            });

            // Request method to POST
            connection.setRequestMethod("POST");

            // Tell the server we're sending arbitrary binary data
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            // Make message content
            byte[] messageContent = getJSON().getBytes(Charset.forName("UTF-8"));

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

            // Get a response
            inputStream = new BufferedInputStream(connection.getInputStream());
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int nextByte;
            while((nextByte = inputStream.read()) != -1)
            {
                byteArrayOutputStream.write(nextByte);
            }
            inputStream.close();
            String response = new String(byteArrayOutputStream.toByteArray(), Charset.forName("UTF-8"));
            CWHResponse cwhResponse = new GsonBuilder().create().fromJson(response, CWHResponse.class);
            return cwhResponse;
        } catch (Exception exc) {
            exc.printStackTrace();
            return null;
        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }

    @Override
    protected void onPostExecute(CWHResponse cwhResponse) {
        super.onPostExecute(cwhResponse);
        if (cwhResponse == null)
            this.onCWHResponseListener.onCWHResponse(new CWHResponse("Connection Failure! Are you connected to the internet?", false));
        else
            this.onCWHResponseListener.onCWHResponse(cwhResponse);
    }

    private String getJSON()
    {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(this);
    }

    public void sendRequest(Context context)
    {
        this.execute(context);
    }
}