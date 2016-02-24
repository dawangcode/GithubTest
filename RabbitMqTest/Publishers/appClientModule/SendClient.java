import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

public class SendClient {
	 
	private static final String QUEUE_NAME = "BusessAsyncQueue";
	private static final String EXCHANGE_NAME = "exchange";
	private static final String ROUTING_KEY = "rKey";
	private static ConnectionFactory factory = new ConnectionFactory();
	private static Connection connection;
	private static Channel channel;
	private static Integer sumCount = 100;
	
	public static void SendMsg() throws IOException, TimeoutException, InterruptedException{
		
		System.out.println("生产者发送" + sumCount + "条消息");

		factory.setHost("127.0.0.1");
		factory.setPort(5672);
		factory.setVirtualHost("NGEAI_host");
		factory.setUsername("NGEAI_admin");
		factory.setPassword("NEWGRAND");
		connection = factory.newConnection();
		channel = connection.createChannel();
		channel.exchangeDeclare(EXCHANGE_NAME, "direct", true);
		channel.queueDeclare(QUEUE_NAME, true, false, false, null);
		channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);

		BasicPublish();

		channel.close();
		connection.close();
	}

	private static void BasicPublish() throws IOException, InterruptedException {

		Date startTime = new Date();

		int count = 1;
		while (count <= sumCount) {
			//channel.confirmSelect();
			//channel.txSelect();
			channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, MessageProperties.PERSISTENT_TEXT_PLAIN,
					Integer.toString(count).getBytes());
			//channel.txCommit();
			//channel.waitForConfirmsOrDie();
			count++;
		}

		Date entTime = new Date();
		long times = entTime.getTime() - startTime.getTime();
		System.out.println("时差：" + Long.toString(times));


	}
	
}
