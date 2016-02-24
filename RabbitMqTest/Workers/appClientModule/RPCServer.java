import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;

public class RPCServer {

	public static void ReadAndResponse() throws Exception {
		
		//建立MQ连接
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("127.0.0.1");
		factory.setPort(5672);
		factory.setVirtualHost("NGEAI_host");
		factory.setUsername("NGEAI_admin");
		factory.setPassword("NEWGRAND");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		long millis = 10;
		Thread.sleep(millis);
		
		//处理request.queue队列里面的消息
		channel.basicQos(1);
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume("rpc.request.queue", false, consumer);

		while (true) {
			System.out.println(" [x] start...");
			System.out.println(" [.] waiting request");
			
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			BasicProperties requestProps = delivery.getProperties();
			BasicProperties responseProps = new BasicProperties.Builder().correlationId(requestProps.getCorrelationId()).build();
			String requestMessage = new String(delivery.getBody(), "UTF-8");
			String responseMessage = requestMessage + "返回";
			System.out.println(" [x] get request...");
			System.out.println(" [x] get request corrId:" + requestProps.getCorrelationId());
			System.out.println(" [x] get request message:" + requestMessage);
			System.out.println(" [x] get request responseQueueName:" + requestProps.getReplyTo());
			
			Thread.sleep(millis);
			
			channel.basicPublish("", requestProps.getReplyTo(), responseProps, responseMessage.getBytes("UTF-8"));
			System.out.println(" [x] set response...");
			System.out.println(" [x] set response corrId:" + responseProps.getCorrelationId());
			System.out.println(" [x] set response message:" + responseMessage);
			System.out.println(" [x] set response RoutingKey:" + requestProps.getReplyTo());
			channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
		}
	}
}
