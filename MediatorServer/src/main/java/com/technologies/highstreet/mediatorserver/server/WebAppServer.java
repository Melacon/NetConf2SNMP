package com.technologies.highstreet.mediatorserver.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import com.technologies.highstreet.mediatorserver.data.MediatorConfig;
import com.technologies.highstreet.mediatorserver.files.MediatorCoreFiles;

public class WebAppServer {

	public static final String PROPFILE = "/etc/mediatorserver.conf";

	public static boolean CLIMODE = false;

	private static org.apache.commons.logging.Log LOG = LogFactory.getLog(WebAppServer.class);
	private static MyProperties properties;

	private static void setLog(String filename, Level lvl) {
		BasicConfigurator.configure();
		Logger.getRootLogger().getLoggerRepository().resetConfiguration();

		if (CLIMODE) {
			ConsoleAppender console = new ConsoleAppender(); // create appender
			// configure the appender
			// String PATTERN = "%d [%p|%c|%C{1}] %m%n";
			String PATTERN = "%d [%p|%C{1}] %m%n";
			console.setLayout(new PatternLayout(PATTERN));
			console.setThreshold(lvl);
			console.activateOptions();
			// add appender to any Logger (here is root)
			Logger.getRootLogger().addAppender(console);
		}
		RollingFileAppender fa = new RollingFileAppender();
		fa.setName("FileLogger");
		fa.setFile(filename);
		fa.setLayout(new PatternLayout("%d %-5p [%c] %m%n"));
		fa.setThreshold(lvl);
		fa.setMaximumFileSize(2000000);
		fa.setAppend(true);

		fa.activateOptions();
		// add appender to any Logger (here is root)
		Logger.getRootLogger().addAppender(fa);
		// repeat with all other desired appenders
	}

	public static void main(String[] args) {

		int propIdx=0;
		try {
			if (args.length > 0)
			{
				if(args[0]!=null && args[0].equals("--cli"))
				{
					CLIMODE=true;propIdx++;
				}
			}
			if(args.length>propIdx)
				properties = MyProperties.Instantiate(args[propIdx]);
			else
				properties = MyProperties.Instantiate();
		} catch (IOException e1) {
			LOG.error("error in config file: " + e1.getMessage());
			return;
		}

		setLog(properties.getLogfilename(),properties.getLogLevel());
		MediatorConfig.SetHostIp(properties.getHostIp());
		MediatorCoreFiles.SetHome(properties.getHome());
		LOG.info("starting server for host=" + properties.getHostIp() + ":" + properties.getPort() + "...");

		Server server = new Server(properties.getPort());
		ServletContextHandler handler = new ServletContextHandler(server, "/api");

		handler.addServlet(TaskServlet.class, "/");

		ResourceHandler resource_handler = new ResourceHandler();
		resource_handler.setDirectoriesListed(true);
		resource_handler.setWelcomeFiles(new String[] { "index.html" });
		resource_handler.setResourceBase("www/");
		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { resource_handler, handler });
		server.setHandler(handlers);
		server.setStopTimeout(3000);
		try {
			server.start();
			cliOutput("started");
			if (CLIMODE) {
				BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
				String command;

				while (true) {
					command = buffer.readLine();
					if (command != null) {
						command = command.toLowerCase();
					} else {
						command = "<null>";
					}

					if (command.equals("quit")) {
						cliOutput("stopping server...");
						server.join();
						server.stop();
						server.destroy();
						cliOutput("good bye");
					}
				}
			} else {
				while (true)
					Thread.sleep(1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static String cliOutput(String msg) {
		if(CLIMODE)
			System.out.println(msg);
		LOG.info(msg);
		return msg;
	}

	public static String cliOutput(String format, Object... args) {
		return cliOutput(String.format(format, args));
	}
}
