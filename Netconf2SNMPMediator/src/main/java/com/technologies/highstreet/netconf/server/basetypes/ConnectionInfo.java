package com.technologies.highstreet.netconf.server.basetypes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionInfo {

	private final String regex = "([0-9]*\\.[0-9]*\\.[0-9]*\\.[0-9]*)\\:([0-9]*)";
	private final String ipAddress;
	private final int port;
	public String getIpAddress() {return this.ipAddress;}
	public int getPort() {return this.port;}
	
	
	@Override
	public String toString() {
		return ipAddress+":"+port;
	}
	public ConnectionInfo(String s) throws Exception
	{
		final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
		final Matcher matcher = pattern.matcher(s);
		if (matcher.find()) {
		    this.ipAddress=matcher.group(1);
		    this.port=Integer.parseInt(matcher.group(2));
		}
		else
			throw new Exception("cannot parse remoteAddress");
	}
}
