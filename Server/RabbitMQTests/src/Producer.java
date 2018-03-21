import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author Philip Rodriguez
 */
public class Producer {
    private final String hostAddress;
    private final String queueName;
    private Connection connection;
    private Channel channel;

    public Producer(String hostAddress, String queueName)
    {
        this.hostAddress = hostAddress;
        this.queueName = queueName;
    }

    public synchronized void connect() throws IOException, TimeoutException {
        if (connection == null)
        {
            //Do the thing!
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(hostAddress);
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(queueName, false, false, false, null);
        }
        else
        {
            //Already connected!
        }
    }

    public synchronized void disconnect() throws IOException, TimeoutException {
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

    public synchronized void sendMessage(byte[] message) throws IOException {
        channel.basicPublish("", queueName, null, message);
    }
}
