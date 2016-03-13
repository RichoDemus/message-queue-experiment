package com.richo.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public interface Node
{
	void start() throws Exception;

	default void createQueue(Connection conn, String queueName) throws Exception
	{
		final Channel channel = conn.createChannel();

		channel.exchangeDeclare("exchange", "direct", true);
		channel.queueDeclare(queueName, false, false, false, null);
		channel.close();
	}
}
