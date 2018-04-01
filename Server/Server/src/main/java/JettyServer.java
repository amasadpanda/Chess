import com.google.gson.GsonBuilder;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JettyServer {
    private Server server;
    private final String serverName;
    private final int httpPort;
    private final int httpsPort;
    private final String keyStorePath;
    private final String keyStorePassword;
    private final String keyManagerPassword;
    private static final Logger logger = Logger.getLogger(JettyServer.class.getName());

    /**
     * Sets up a Jetty-based enqueuer HTTP/HTTPS server accessible only on
     * http[s]://[Server IP Address]:[Port]/[serverName] for which requests will be validated by the given request
     * validator and for which any output will be sent to the given output handler. Note httpPort and httpsPort cannot
     * be the same!
     *
     * @param serverName is the name of the sever and thus the only accessible path to the server.
     * @param httpPort is the port to listen for plain, unencrypted HTTP connections on, or -1 to not accept HTTP
     *                connections.
     * @param httpsPort is the port to listen for encrypted HTTPS connections on, or -1 to not accept HTTPS connections.
     * @param keyStorePath is the path to the keystore file to use for HTTPS, or null if httpsPort is -1.
     * @param keyStorePassword is the keystore's password, or null if httpsPort is -1.
     * @param keyManagerPassword is the keystore manager password, or null if httpsPort is -1.
     */
    public JettyServer(String serverName, int httpPort, int httpsPort, String keyStorePath, String keyStorePassword, String keyManagerPassword)
    {
        this.serverName = serverName;
        this.httpPort = httpPort;
        this.httpsPort = httpsPort;
        this.keyStorePath = keyStorePath;
        this.keyStorePassword = keyStorePassword;
        this.keyManagerPassword = keyManagerPassword;
    }

    public synchronized void start() throws Exception {
        if (server == null)
        {
            // Set the server and the number of threads is should have to work with
            server = new Server(new QueuedThreadPool(128, 8));

            // Set up the server connector, including what port the server will run on
            if (httpPort != -1) {
                ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory());
                connector.setPort(httpPort);
                server.addConnector(connector);
            }

            if (httpsPort != -1) {
                SslContextFactory sslContextFactory = new SslContextFactory();
                sslContextFactory.setKeyStorePath(keyStorePath);
                sslContextFactory.setKeyStorePassword(keyStorePassword);
                sslContextFactory.setKeyManagerPassword(keyManagerPassword);
                ServerConnector sslConnector = new ServerConnector(server, sslContextFactory);
                sslConnector.setPort(httpsPort);
                server.addConnector(sslConnector);
            }

            // Set up a context handler to handle only requests directed to the server name
            ContextHandler contextHandler = new ContextHandler("/" + serverName);
            contextHandler.setHandler(new AbstractHandler() {
                @Override
                public void handle(String target, Request baseRequest, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
                        logger.log(Level.INFO, "Handling request from " + baseRequest.getRemoteAddr());

                        int contentLength = baseRequest.getContentLength();
                        if (contentLength > 0)
                        {
                            //Load request content!
                            logger.log(Level.INFO, "Request content length is " + contentLength);

                            byte[] messageBytes = new byte[contentLength];
                            BufferedInputStream bufferedInputStream = new BufferedInputStream(httpServletRequest.getInputStream());
                            int numBytesRead = bufferedInputStream.read(messageBytes, 0, contentLength);

                            // Sanity check the bytes read
                            if (numBytesRead == contentLength)
                            {
                                //Everything is ok, so we may pass off the request to the producer...
                                logger.log(Level.INFO,"Request acceptable.");
                                String messageContents = new String(messageBytes, Charset.forName("UTF-8"));
                                logger.log(Level.INFO,"Message contains: " + messageContents);

                                CWHRequest reconstructedRequest = new GsonBuilder().create().fromJson(messageContents, CWHRequest.class);
                                logger.log(Level.INFO, reconstructedRequest.getAuthID());
                                logger.log(Level.INFO, reconstructedRequest.getRequestType().toString());
                                logger.log(Level.INFO, "Hashmap extras = " + reconstructedRequest.getExtras());

                                logger.log(Level.INFO, "Validating the authID...");
                                try {
                                    logger.log(Level.INFO, FireEater.tokenToUID(reconstructedRequest.getAuthID()));
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                httpServletResponse.setContentType("application/json; charset=utf-8");

                                // Construct a CWHResponse and respond!
                                CWHResponse cwhResponse = new CWHResponse("Request acceptable!!!", true);
                                String response = cwhResponse.getJSON();

                                httpServletResponse.setContentLength(response.getBytes(Charset.forName("UTF-8")).length);
                                httpServletResponse.getOutputStream().write(response.getBytes(Charset.forName("UTF-8")));
                                httpServletResponse.getOutputStream().flush();
                                httpServletResponse.getOutputStream().close();
                            }
                            else {
                                //Something went wrong...
                                logger.log(Level.INFO,"Request content-length mismatch! Responding with that information...");
                                httpServletResponse.setContentType("text/html");
                                String response = "Invalid request! Content-length mismatch! " + new Date().toString();
                                httpServletResponse.setContentLength(response.getBytes(Charset.forName("UTF-8")).length);
                                httpServletResponse.getOutputStream().write(response.getBytes(Charset.forName("UTF-8")));
                                httpServletResponse.getOutputStream().flush();
                                httpServletResponse.getOutputStream().close();
                            }
                        }
                        else
                        {
                            // Invalid request!
                            logger.log(Level.INFO,"Request was invalid! Responding with that information...");
                            httpServletResponse.setContentType("text/html");
                            String response = "Invalid request! " + new Date().toString();
                            httpServletResponse.setContentLength(response.getBytes(Charset.forName("UTF-8")).length);
                            httpServletResponse.getOutputStream().write(response.getBytes(Charset.forName("UTF-8")));
                            httpServletResponse.getOutputStream().flush();
                            httpServletResponse.getOutputStream().close();
                        }

                }
            });

            server.setHandler(contextHandler);

            server.start();
            server.dumpStdErr();
        }
        else
        {
            //Already started
        }
    }

    public synchronized void stop() throws Exception {
        if (server != null)
        {
            server.stop();
            server = null;
        }
        else
        {
            //Server already stopped!
        }
    }
}
