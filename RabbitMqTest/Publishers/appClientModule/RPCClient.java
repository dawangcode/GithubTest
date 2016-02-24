import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;
import java.util.UUID;

public class RPCClient {

	public static void Send() throws Exception {

		// ����MQ����
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("127.0.0.1");
		factory.setPort(5672);
		factory.setVirtualHost("NGEAI_host");
		factory.setUsername("NGEAI_admin");
		factory.setPassword("NEWGRAND");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		// ����response��ʱ���� �������Ǹ��ݶ������ֻ�ȡ��Ϣ��
		String responseQueueName = channel.queueDeclare().getQueue();

		// ����UUID����� ������Ϣ
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

		// ����һ�������� �󶨵���ʱ���� �ȴ�������Ӧ��
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
