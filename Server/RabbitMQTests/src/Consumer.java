import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeoutException;

public class Consumer {
    private final String hostAddress;
    private final String queueName;
    private Connection connection;
    private Channel channel;

    public Consumer(String hostAddress, String queueName)
    {
        this.hostAddress = hostAddress;
        this.queueName = queueName;
    }

    public void connect() throws IOException, TimeoutException {
        if (connection == null)
        {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(hostAddress);
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(queueName, false, false, false, null);

            DefaultConsumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    super.handleDelivery(consumerTag, envelope, properties, body);
                    System.out.println(System.currentTimeMillis() + " Got message: " + new String(body, Charset.forName("UTF-8")));
                }
            };
            channel.basicConsume(queueName, true, consumer);
            System.out.println("Consumer is connected waiting to consume messages!");
        }
        else
        {
            //Already connected
        }
    }

    public void disconnect() throws IOException, TimeoutException {
        if (connection != null && channel != null)
        {
            channel.close();
            connection.close();
            channel = null;
            connection = null;
        }
    }
}
