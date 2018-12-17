package com.technologies.highstreet.netconf2snmpmediator.test;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.log4j.Level;

import com.technologies.highstreet.netconf.server.basetypes.HtLogger;
import com.technologies.highstreet.netconf.server.basetypes.TimeSpan;
import com.technologies.highstreet.netconf2snmpmediator.server.Config;
import com.technologies.highstreet.netconf2snmpmediator.server.networkelement.SNMPConnector;

public class SnmpInitTest {


	public static void main(String[] args)
	{
		HtLogger.initConsole(Level.DEBUG);
		System.out.println("Test binding port for snmp lib");
		System.out.println("==============================");

		if(args.length==0)
		{
			System.out.println("usage: TestSnmpApp [remoteip] [localport] [localip] [lazyBind]");
			System.out.println("  remoteip  ... ip address of the device");
			System.out.println("  localport ... port on your machine to listen to | def: 161");
			System.out.println("  localip   ... interface ip to listen to | def: 0.0.0.0");
			System.out.println("  lazyBind  ... 0=no 1=yes | def: no");
		}
		int argIdx=0;
		Config cfg = Config.TestConfig();
		int trapport = 10161;
		String remoteip = "127.0.0.1";
		if(args.length>0)
			remoteip=args[argIdx++];
		int remoteport = 162;
		if(args.length>1)
			trapport=Integer.parseInt(args[argIdx++]);
		long start=System.currentTimeMillis();

		System.out.println("starting with "+start);
		SNMPConnector connector = new SNMPConnector(cfg, trapport, remoteip, remoteport);
		try {

			if(args.length>2)
				connector.setBindingIpAddress(InetAddress.getByName(args[2]));
			if(args.length>3)
				connector.setReuseAddress(Integer.parseInt(args[3])==1);

			connector.startListen();
			long end=System.currentTimeMillis();
		TimeSpan dif=TimeSpan.ofMillis(end-start);

		System.out.println("ended with "+end);
		System.out.println("=========================");
		System.out.println("result:"+dif.toString());


		connector.shutdown();
		} catch (IOException e) {
			System.out.println("Error: "+e.getMessage());
		}

	}
}
