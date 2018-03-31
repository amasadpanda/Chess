package com.example.phili.rabbitmqtestproject;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.HashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by phili on 3/25/2018.
 */


/*
 * This class encapsulates sending an HTTPS request. To send a request, GUI devs just need to
 * create a new request object, pass in relevant stuff and set hashmap stuff as needed,
 * and then call sendRequest.
 */
public class Request extends AsyncTask<Context, Void, Request.Response> {

    //TODO: maybe a better way to deal with this lol
    public static final String httpsAddress = "https://10.32.251.100:1357/enqueuer1/";

    // TODO: all possible values need to eb implemented
    public static enum RequestType {
        CREATE_ACCOUNT, MAKE_MOVE
    }

    @Expose
    private final long creationTimeStamp;

    @Expose
    private final String authID;

    /*
     * TODO: What are all possible values of this?
     */
    @Expose
    private final RequestType requestType;

    @Expose
    private final HashMap<String, String> requestData;

    private final RequestCompletionListener requestCompletionListener;

    public Request(String authID, RequestType requestType, RequestCompletionListener requestCompletionListener)
    {
        this.authID = authID;
        this.creationTimeStamp = System.currentTimeMillis();
        this.requestType = requestType;
        this.requestData = new HashMap<String, String>();
        this.requestCompletionListener = requestCompletionListener;
    }

    /*
     * This is where they can modify the internal data pairs...
     */
    public HashMap<String, String> getRequestData() {
        return requestData;
    }


    /*
     * This method is responsible solely for creating/constructing and
     * actually sending off the message!
     */
    @Override
    protected Response doInBackground(Context... mainContext) {
        // Send the message to our enqueuer!
        HttpsURLConnection connection = null;
        try
        {
            // Open basic connection
            URL url = new URL(httpsAddress);

            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            BufferedInputStream inputStream = new BufferedInputStream(mainContext[0].getAssets().open("ChessWithHatsCert.cer"));
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

            // Let's build our request...
            String messageContentString = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(this);
            byte[] messageContent = messageContentString.getBytes(Charset.forName("UTF-8"));

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
            return new Response(response.toString(), true); //TODO: change to Gson unwrap!
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
        return null;
    }

    /*
     * Leave it to the person sending a request to deal with the response!
     */
    @Override
    protected void onPostExecute(Request.Response response)
    {
        requestCompletionListener.requestCompleted(response);
    }

    /*
     * Literally just calls execute!
     */
    public void sendRequest(Context mainContext)
    {
        this.execute(mainContext);
    }

    static class Response
    {
        private final String message;
        private final boolean success;
        public Response(String message, boolean success)
        {
            this.message = message;
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public boolean isSuccess() {
            return success;
        }
    }
}
