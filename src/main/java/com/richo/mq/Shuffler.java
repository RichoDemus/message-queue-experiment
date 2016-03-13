package com.richo.mq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;

public class Shuffler implements Node
{

	private final String consumeQueue;
	private final String produceQueue;
	private final String name;
	private final String exchangeName;
	private final String routingKey;

	public Shuffler(String[] args)
	{
		consumeQueue = args[1];
		produceQueue = args[2];
		name = consumeQueue + "->" + produceQueue;
		exchangeName = consumeQueue + "-to-" + produceQueue + "-exchange";
		routingKey = consumeQueue + "-to-" + produceQueue + "-route";
	}

	@Override
	public void start() throws Exception
	{
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
				channel.basicPublish(exchangeName, routingKey, null, (message + ", " + name).getBytes());
			}
		});
	}
}
