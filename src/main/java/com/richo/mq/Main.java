package com.richo.mq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;

public class Main
{
	public static void main(String[] args) throws Exception
	{
		if (args.length != 3)
		{
			System.out.println("Expected arguments: name consumeQueue produceQueue");
			System.exit(1);
		}
		final String name = args[0];
		final String consumeQueue = args[1];
		final String produceQueue = args[2];
		final String exchangeName = consumeQueue + "-to-" + produceQueue + "-exchange";
		final String routingKey = consumeQueue + "-to-" + produceQueue + "-route";


		ConnectionFactory factory = new ConnectionFactory();
		factory.setUri("amqp://guest:guest@rabbit-mq:5672");
		Connection conn = factory.newConnection();

		createQueue(conn, consumeQueue);
		createQueue(conn, produceQueue);

		final Channel channel = conn.createChannel();


		channel.exchangeDeclare(exchangeName, "direct", true);
		final String queueName = channel.queueDeclare(produceQueue, false, false, false, null).getQueue();
		channel.queueBind(queueName, exchangeName, routingKey);

		channel.basicConsume(consumeQueue, true, new DefaultConsumer(channel)
		{
			@Override
			public void handleDelivery(String consumerTag,
									   Envelope envelope,
									   AMQP.BasicProperties properties,
									   byte[] body) throws IOException
			{
				String message = new String(body, "UTF-8");
				System.out.println("Took a message from " + consumeQueue + " and put it in " + produceQueue);
				channel.basicPublish(exchangeName, routingKey, null, (message + ", " + name).getBytes());
			}
		});
	}

	private static void createQueue(Connection conn, String queueName) throws Exception
	{
		final Channel channel = conn.createChannel();

		channel.exchangeDeclare("exchange", "direct", true);
		channel.queueDeclare(queueName, false, false, false, null);
		channel.close();
	}
}
