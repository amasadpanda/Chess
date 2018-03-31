package com.example.phili.requestexampleapplication;


import android.content.Context;
import android.os.AsyncTask;

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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

public class CWHRequest extends AsyncTask<Context, Void, CWHResponse> {
    public static final String serverURL = "https://philiprodriguez.ddns.net:1235/chessWithHats/";

    public enum RequestType {
        MAKE_MOVE, LOGIN, ACCEPT_INVITE, FRIEND_REQUEST, GAME_CREATION, MATCHMAKING_REQUEST
    }

    @Expose
    private String authID;

    @Expose
    private RequestType requestType;

    @Expose
    private HashMap<String, String> extras;

    private OnCWHResponseListener onCWHResponseListener;

    public CWHRequest(String authID, RequestType requestType, OnCWHResponseListener onCWHResponseListener)
    {
        this.authID = authID;
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

    @Override
    protected CWHResponse doInBackground(Context... contexts) {
        HttpsURLConnection connection = null;
        try
        {
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
            System.out.println("Got response from server: " + response);
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
            this.onCWHResponseListener.onCWHResponse(new CWHResponse("Connection Failure!", false));
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
