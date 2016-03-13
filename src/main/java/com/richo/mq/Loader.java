package com.richo.mq;

import com.google.common.util.concurrent.RateLimiter;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class Loader implements Node
{
	private final String consumeQueue;
	private final String produceQueue;
	private final String exchangeName;
	private final String routingKey;
	private final long messagesPerSecond;
	private final AtomicLong sentMessages;
	private final LongAdder receivedMessages;

	public Loader(String[] args)
	{
		produceQueue = args[1];
		consumeQueue = args[2];
		messagesPerSecond = Long.parseLong(args[3]);
		exchangeName = consumeQueue + "-to-" + produceQueue + "-exchange";
		routingKey = consumeQueue + "-to-" + produceQueue + "-route";
		sentMessages = new AtomicLong();
		receivedMessages = new LongAdder();
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
				receivedMessages.increment();
			}
		});


		final RateLimiter rateLimiter;
		if (messagesPerSecond > 0)
		{
			rateLimiter = RateLimiter.create(messagesPerSecond);
		}
		else
		{
			rateLimiter = RateLimiter.create(1);
		}
		System.out.println("Starting produce loop");
		while(true)
		{
			if (messagesPerSecond > 0)
			{
				rateLimiter.acquire();
			}
			channel.basicPublish(exchangeName, routingKey, null, ("Message " + sentMessages.incrementAndGet()).getBytes());
		}
	}
}
