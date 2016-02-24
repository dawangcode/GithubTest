import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class ReadServer {
	private final static String QUEUE_NAME = "BusessAsyncQueue";
	private static ConnectionFactory factory = new ConnectionFactory();
	private static Connection connection;
	private static Channel channel;
	private static long sleepMillis = 0;
	
	private final static Consumer consumer = new DefaultConsumer(channel) {
		@Override
		public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
				byte[] body) throws IOException {
			
			try {
				Thread.sleep(sleepMillis);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			String message = new String(body, "UTF-8");
			System.out.println(" [x] Received '" + message + "'");
			channel.basicAck(envelope.getDeliveryTag(), false);
		}
	};
	
	public static void ReadMsg() throws IOException, TimeoutException{
		factory.setHost("127.0.0.1");
		factory.setPort(5672);
		factory.setVirtualHost("NGEAI_host");
		factory.setUsername("NGEAI_admin");
		factory.setPassword("NEWGRAND");
		connection = factory.newConnection();
		channel = connection.createChannel();
		channel.queueDeclare(QUEUE_NAME, true, false, false, null);
		channel.basicQos(0, 1, false);
		channel.basicConsume(QUEUE_NAME, false, consumer);
		System.out.println("消费者开始接受消息");
		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
	}
}
