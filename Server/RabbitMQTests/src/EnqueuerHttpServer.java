import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.concurrent.Executors;

public class EnqueuerHttpServer {
    private HttpServer httpServer;
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
     */
    public EnqueuerHttpServer(String serverName, Producer producer, int maxBytesPerSecond, int maxRequestsPerSecond, int port)
    {
        this.serverName = serverName;
        this.port = port;
        this.producer = producer;
    }

    public void start() throws IOException {
        if (httpServer == null)
        {
            //Create the http server with a max backlog of 32.
            httpServer = HttpServer.create(new InetSocketAddress(port), 32);
            httpServer.createContext("/" + serverName, new RequestHandler(256)); //TODO: change this 256 business to something better
            httpServer.setExecutor(Executors.newFixedThreadPool(4)); //TODO: revisit optimal number of threads...
            httpServer.start();
        }
        else
        {
            // Already started!
        }
    }

    public void stop()
    {
        if (httpServer != null)
        {
            httpServer.stop(3);
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
            System.out.println("Handling request on " + Thread.currentThread().getName() + "!");
            System.out.println("Remote Address: " + httpExchange.getRemoteAddress());
            System.out.println("Protocol: " + httpExchange.getProtocol());
            System.out.println("Request Method: " + httpExchange.getRequestMethod());
            System.out.println("Headers: " + httpExchange.getRequestHeaders().entrySet());

            // This is where you can reject the connection if they're banned or whatever...

            //Now actually read that request body!
            String contentSizeString = httpExchange.getRequestHeaders().getFirst("Content-Length");
            if (contentSizeString != null && contentSizeString.matches("[0-9]*"))
            {
                // Ok, there should be something to read!
                int contentLength = Integer.parseInt(contentSizeString);
                if (contentLength > maxContentLength)
                {
                    // Too much content! abort!
                    System.out.println("Aborting connection because length too long: " + contentLength);
                    httpExchange.getResponseBody().close();
                    return;
                }

                byte[] bytesRead = new byte[contentLength];
                BufferedInputStream inputStream = new BufferedInputStream(httpExchange.getRequestBody());
                for(int i = 0; i < bytesRead.length; i++)
                {
                    int read = inputStream.read();
                    if (read < 0 || read > 255)
                    {
                        //Invalid byte!
                        System.out.println("Encountered invalid byte at " + i + ": " + read + ", aborting...");
                        httpExchange.getResponseBody().close();
                        return;
                    }
                    else {
                        bytesRead[i] = (byte) read;
                    }
                }
                System.out.println("Client content as string is: " + new String(bytesRead, Charset.forName("UTF-8")));

                // Send a reasonable response
                String response = "Accepted request!";
                httpExchange.sendResponseHeaders(200, response.getBytes(Charset.forName("UTF-8")).length);
                httpExchange.getResponseBody().write(response.getBytes(Charset.forName("UTF-8")));
                httpExchange.getResponseBody().flush();
                httpExchange.getResponseBody().close();
            }
            else
            {
                //No content-length! abort!
                System.out.println("No content-length header found or it was not an integer, aborting!");
                // Send a reasonable response
                String response = "Invalid format! Is this a web browser? Get lost!";
                httpExchange.sendResponseHeaders(200, response.getBytes(Charset.forName("UTF-8")).length);
                httpExchange.getResponseBody().write(response.getBytes(Charset.forName("UTF-8")));
                httpExchange.getResponseBody().flush();
                httpExchange.getResponseBody().close();
                return;
            }
        }
    }


}
