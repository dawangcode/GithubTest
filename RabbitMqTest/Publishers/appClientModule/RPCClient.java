import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;
import java.util.UUID;

public class RPCClient {

	public static void Send() throws Exception {

		// 建立MQ连接
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("127.0.0.1");
		factory.setPort(5672);
		factory.setVirtualHost("NGEAI_host");
		factory.setUsername("NGEAI_admin");
		factory.setPassword("NEWGRAND");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		// 申请response临时队列 消费者是根据队列名字获取消息的
		String responseQueueName = channel.queueDeclare().getQueue();

		// 申请UUID随机数 生产消息
		String corrId = UUID.randomUUID().toString();
		String requestMessage = UUID.randomUUID().toString();
		
		System.out.println(" [.] set request corrId:" + corrId);
		System.out.println(" [.] set request message:" + requestMessage);
		System.out.println(" [.] set request responseQueueName:" + responseQueueName);
		long millis = 10;
		Thread.sleep(millis);
		System.out.println(" [.] begin request...");

		BasicProperties requestProps = new BasicProperties.Builder().correlationId(corrId).replyTo(responseQueueName)
				.build();

		channel.exchangeDeclare("rpc.request.exchange", "direct", false);
		channel.queueDeclare("rpc.request.queue", false, false, false, null);
		channel.queueBind("rpc.request.queue", "rpc.request.exchange", "rpc.request.routingKey");
		channel.basicPublish("rpc.request.exchange", "rpc.request.routingKey", requestProps, requestMessage.getBytes("UTF-8"));

		// 申请一个消费者 绑定到临时队列 等待服务器应答
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(responseQueueName, true, consumer);
		while (true) {
			System.out.println(" [.] waiting response");
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			if (delivery.getProperties().getCorrelationId().equals(corrId)) {
				String responseMessage = new String(delivery.getBody(), "UTF-8");
				System.out.println(" [.] get response...");
				System.out.println(" [.] get response:" + responseMessage);
				channel.close();
				connection.close();
				return;
			}
		}

	}

}
