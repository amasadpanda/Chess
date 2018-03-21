import com.sun.net.httpserver.*;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Executors;

public class EnqueuerHttpsServer {
    private HttpsServer httpsServer;

    KeyManagerFactory kmf;
    TrustManagerFactory tmf;

    private int port;
    private Producer producer;
    private String serverName;

    /**
     * Creates a new HTTP server that will send all valid requests through to RabbitMQ via the producer passed to it, and
     * will automatically ban users violating the maximums passed to it.
     *
     * @param serverName is the name and path of this exact server. For example, if the server's name is hello, you could
     *                   point to the server by http://[IP ADDRESS]:[PORT]/hello
     * @param producer encapsulates where valid messages get sent in terms of RabbitMQ.
     * @param maxBytesPerSecond represents the maximum number of content bytes an IP address can send per second before
     *                          getting temporarily banned.
     * @param maxRequestsPerSecond represents the maximum number of individual requests an IP address can send per
     *                             second before getting temporarily banned.
     * @param port represents the port number to host the HTTP server on.
     * @param keystoreFile is the keystore file containing the key to use with SSL.
     * @param storepass is the password for the keystore file.
     * @param keyalias is the name of the key to use.
     * @param keypass is the password for the selected key to use.
     */
    public EnqueuerHttpsServer(String serverName, Producer producer, int maxBytesPerSecond, int maxRequestsPerSecond, int port, File keystoreFile, String storepass, String keyalias, String keypass) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        // Set up basic information
        this.serverName = serverName;
        this.port = port;
        this.producer = producer;

        // Set up SSL information

        //Load keystore file
        FileInputStream fis = new FileInputStream(keystoreFile);
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(fis, storepass.toCharArray());

//        //Load certificate information
//        Certificate cert = keyStore.getCertificate(keyalias);
//        System.out.println("Using certificate: " + cert.toString());

        //Set up kmf
        kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore, keypass.toCharArray());

        //Set up tmf
        tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(keyStore);
    }

    public void start() throws IOException, NoSuchAlgorithmException, KeyManagementException {
        if (httpsServer == null)
        {
            //Create the http server with a max backlog of 32.
            httpsServer = HttpsServer.create(new InetSocketAddress(port), 32);

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                @Override
                public void configure(HttpsParameters httpsParameters) {
                    System.out.println("Running HTTPS Configurator...");
                    try {
                        // initialise the SSL context
                        SSLContext c = getSSLContext();
                        SSLEngine engine = c.createSSLEngine();

                        // set the SSL parameters
                        SSLParameters sslParameters = c.getDefaultSSLParameters();
                        sslParameters.setCipherSuites(engine.getEnabledCipherSuites());
                        sslParameters.setProtocols(engine.getEnabledProtocols());
                        sslParameters.setNeedClientAuth(false);
                        httpsParameters.setSSLParameters(sslParameters);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.out.println("Failed to create HTTPS server");
                    }
                }
            });

            httpsServer.createContext("/" + serverName, new RequestHandler(256)); //TODO: change this 256 business to something better
            httpsServer.setExecutor(Executors.newFixedThreadPool(50)); //TODO: revisit optimal number of threads...
            httpsServer.start();
        }
        else
        {
            // Already started!
        }
    }

    public void stop()
    {
        if (httpsServer != null)
        {
            httpsServer.stop(3);
        }
        else
        {
            // Already stopped!
        }
    }

    private static class RequestHandler implements HttpHandler {

        int maxContentLength;

        public RequestHandler(int maxContentLength)
        {
            this.maxContentLength = maxContentLength;
        }

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            HttpsExchange httpsExchange = (HttpsExchange)httpExchange;
            System.out.println("Handling request on " + Thread.currentThread().getName() + "!");
            System.out.println("Remote Address: " + httpsExchange.getRemoteAddress());
            System.out.println("Protocol: " + httpsExchange.getProtocol());
            System.out.println("Request Method: " + httpsExchange.getRequestMethod());
            System.out.println("Headers: " + httpsExchange.getRequestHeaders().entrySet());

            System.out.println("Doing LOTS of sleeping work...");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // This is where you can reject the connection if they're banned or whatever...

            //Now actually read that request body!
            String contentSizeString = httpsExchange.getRequestHeaders().getFirst("Content-Length");
            if (contentSizeString != null && contentSizeString.matches("[0-9]*"))
            {
                // Ok, there should be something to read!
                int contentLength = Integer.parseInt(contentSizeString);
                if (contentLength > maxContentLength)
                {
                    // Too much content! abort!
                    System.out.println("Aborting connection because length too long: " + contentLength);
                    httpsExchange.getResponseBody().close();
                    return;
                }

                byte[] bytesRead = new byte[contentLength];
                BufferedInputStream inputStream = new BufferedInputStream(httpsExchange.getRequestBody());
                for(int i = 0; i < bytesRead.length; i++)
                {
                    int read = inputStream.read();
                    if (read < 0 || read > 255)
                    {
                        //Invalid byte!
                        System.out.println("Encountered invalid byte at " + i + ": " + read + ", aborting...");
                        httpsExchange.getResponseBody().close();
                        return;
                    }
                    else {
                        bytesRead[i] = (byte) read;
                    }
                }
                System.out.println("Client content as string is: " + new String(bytesRead, Charset.forName("UTF-8")));

                // Send a reasonable response
                String response = "Accepted request!";
                httpsExchange.sendResponseHeaders(200, response.getBytes(Charset.forName("UTF-8")).length);
                httpsExchange.getResponseBody().write(response.getBytes(Charset.forName("UTF-8")));
                httpsExchange.getResponseBody().flush();
                httpsExchange.getResponseBody().close();
            }
            else
            {
                //No content-length! abort!
                System.out.println("No content-length header found or it was not an integer, aborting!");
                // Send a reasonable response
                String response = "Invalid format! Is this a web browser? Get lost! " + new Date().toString();
                httpsExchange.sendResponseHeaders(200, response.getBytes(Charset.forName("UTF-8")).length);
                httpsExchange.getResponseBody().write(response.getBytes(Charset.forName("UTF-8")));
                httpsExchange.getResponseBody().flush();
                httpsExchange.getResponseBody().close();
                return;
            }
        }
    }
}
