package com.technologies.highstreet.netconf.server.ssh;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;

public class UserPasswordAuthenticator implements PasswordAuthenticator {

	private static final Log LOG = LogFactory.getLog(UserPasswordAuthenticator.class);
	private final String username;
	private final String password;

	public UserPasswordAuthenticator(String username, String passwd) {
		this.username = username;
		this.password = passwd;
	}

	@Override
	public boolean authenticate(String username, String password, ServerSession session) {
		boolean b= this.username.equals(username) && this.password.equals(password); 
		
		if(!b)
			LOG.warn("someone tries to login with wrong credentials. username="+username+" passwd="+anon(password));
		return b;
	}

	private String anon(String x) {
		
		String y="";
		for(int i=0;i<x.length();i++)
			y+="*";
		return y;
	}

}
