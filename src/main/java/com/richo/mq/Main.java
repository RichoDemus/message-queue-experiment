package com.richo.mq;

public class Main
{
	public static void main(String[] args) throws Exception
	{
		if (args.length != 3)
		{
			System.out.println("Expected arguments: type (SHUFFLE) consumeQueue produceQueue");
			System.exit(1);
		}
		final Type type = Type.valueOf(args[0]);

		final Node node;
		switch (type)
		{
			case SHUFFLE:
				node = new Shuffler(args);
				break;
			default:
				System.out.println("Unhandled type: " + type);
				System.exit(1);
				return;
		}

		node.start();
	}
}
