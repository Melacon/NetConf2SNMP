package mediatorserver;

import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.apache.log4j.lf5.LogLevel;

import com.technologies.highstreet.mediatorserver.server.JmxWebSocketBridgeServer;

public class JMXServerTest {

	private static void initLOG()
	{
		ConsoleAppender console = new ConsoleAppender(); // create appender
		// configure the appender
		// String PATTERN = "%d [%p|%c|%C{1}] %m%n";
		String PATTERN = "%d [%p|%C{1}] %m%n";
		console.setLayout(new PatternLayout(PATTERN));
		console.setThreshold(Priority.DEBUG);
		console.activateOptions();
		// add appender to any Logger (here is root)
		Logger.getRootLogger().addAppender(console);

	}
	public static void main(String[] args)
	{
		initLOG();
		JmxWebSocketBridgeServer server = new JmxWebSocketBridgeServer();
		try {
			System.out.println("any key to exit...");
			System.in.read();
		} catch (IOException e) {

		}
		System.out.println("closed");
	}
}
