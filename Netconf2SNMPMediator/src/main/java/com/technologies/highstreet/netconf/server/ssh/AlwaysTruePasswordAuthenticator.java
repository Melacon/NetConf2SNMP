package com.technologies.highstreet.netconf.server.ssh;

import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;

/**
 * {@link PasswordAuthenticator} that always allow acces
 *
 * @author Julio Carlos Barrera
 *
 */
public class AlwaysTruePasswordAuthenticator implements  PasswordAuthenticator {

	@Override
	public boolean authenticate(String username, String password, ServerSession session) {
		return true;
	}

}
