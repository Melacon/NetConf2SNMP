package com.technologies.highstreet.netconf2snmpmediator.test;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.technologies.highstreet.netconf.server.basetypes.TimeSpan;

public class SimplePortBindTest {

	public static void main(String[] args)
	{

		String ip="0.0.0.0";
		int port=10161;

		if(args.length>0)
			ip=args[0];
		if(args.length>1)
			port=Integer.parseInt(args[1]);
		System.out.println("try to bind "+ip+":"+port);
		long start=System.currentTimeMillis();
		try {
			DatagramSocket socket = new DatagramSocket(port, InetAddress.getByName(ip));

			socket.close();
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
		}

		long end=System.currentTimeMillis();
		TimeSpan ts = TimeSpan.ofMillis(end-start);
		System.out.println("dur="+ts.toString());
	}
}
