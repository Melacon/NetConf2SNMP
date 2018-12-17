package com.technologies.highstreet.mediatorserver.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.logging.LogFactory;

public class JmxWebSocketBridgeServer implements NotificationListener {

	private static org.apache.commons.logging.Log LOG = LogFactory.getLog(JmxWebSocketBridgeServer.class);

	private static NotificationFilter notificationFilter = new NotificationFilter() {

		@Override
		public boolean isNotificationEnabled(Notification notification) {

			return true;
		}
	};
	private final List<JMXConnector> connections;

	public JmxWebSocketBridgeServer()
	{
		this.connections=new ArrayList<JMXConnector>();
		try {
			this.startJMXSession(6001);
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}
	}
	private static JMXServiceURL createConnectionURL(String host, int port) throws MalformedURLException
	{
	    return new JMXServiceURL("rmi", "", 0, "/jndi/rmi://" + host + ":" + port + "/jmxrmi");
	}
	private void startJMXSession(int port) throws IOException
	{
		String hostname="192.168.178.89";
		//JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://:"+port+"/jmxrmi");
		//JMXServiceURL url = new JMXServiceURL(hostname+":"+port);
		JMXConnector jmxc = JMXConnectorFactory.connect(createConnectionURL(hostname, port), null);
		jmxc.addConnectionNotificationListener(this, notificationFilter, jmxc);
		this.connections.add(jmxc);
		jmxc.connect();
	}

	@Override
	public void handleNotification(Notification arg0, Object arg1) {

		LOG.debug("handle notification "+arg0.toString());

	}
}
