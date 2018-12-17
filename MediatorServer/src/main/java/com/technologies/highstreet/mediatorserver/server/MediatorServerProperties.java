package com.technologies.highstreet.mediatorserver.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Level;

import com.technologies.highstreet.mediatorserver.data.PortRange;

public class MediatorServerProperties {

	public static final String PROPFILE = "/etc/mediatorserver.conf";

	private static MediatorServerProperties mObj;
	public static final String DEFAULT_LOGLEVEL = "WARN";
	public static final String DEFAULT_MEDIATORLOGLEVEL = "WARN";
	private static final String DEFAULT_MEMORYSETTING = "-Xmx256m -Xms128m";
	private static final int DEFAULT_MEDIATORDEVICEPING_TIMEOUT = 2000; //[ms]
	private static final int DEFAULT_MEDIATORSNMP_LATENCY = 2000; //[ms]

	public static final String DS="/";
	private static final String JSONKEY_HOMEDIR = "home";
	private static final String JSONKEY_HOSTIP = "host";
	private static final String JSONKEY_HOSTPORT = "port";
	private static final String JSONKEY_PORTRANGE_NETCONF = "ncrange";
	private static final String JSONKEY_PORTRANGE_SNMP = "snmprange";
	private static final String JSONKEY_PORTRANGE_JMX = "jmxrange";
	private static final Object JSONKEY_LOGLEVEL = "loglevel";
	private static final Object JSONKEY_LOGFILE = "logfile";
	private static final Object JSONKEY_MED_LOGLEVEL = "mediator-loglevel";
	private static final Object JSONKEY_MED_DEVICEPING_TIMEOUT = "mediator-devicepingtimeout";
	private static final Object JSONKEY_MED_SNMPLATENCY = "mediator-snmplatency";
	private static final Object JSONKEY_MED_MEMORY = "mediator-memory";


	public static MediatorServerProperties Instantiate() throws IOException
	{
		if(mObj==null)
			mObj=new MediatorServerProperties(PROPFILE);
		return mObj;
	}
	public static MediatorServerProperties Instantiate(String filename) throws IOException
	{
		if(mObj==null)
			mObj=new MediatorServerProperties(filename);
		return mObj;
	}
	public static MediatorServerProperties getInstance()
	{
		return mObj;
	}

	private final String mHomeDirectory;
	private final PortRange mNetconfPortRange;
	private final PortRange mSNMPPortRange;
	private final PortRange mJmxPortRange;
	private final String mHostIp;
	private final String mLogfilename;
	private final int mPort;
	private Level mLogLevel;
	private final String mMediatorJavaMemoryParams;
	private final Level mMediatorLogLevel;
	private final int mMediatorDevicePingTimeout;
	private final int mMediatorSnmpLatency;


	private static String strclean(String s)
	{
		return s.replace("\"", "");
	}
	private MediatorServerProperties(String filename) throws IOException
	{
		Properties defaultProps = new Properties();
		FileInputStream in = new FileInputStream(filename);
		defaultProps.load(in);
		in.close();

		this.mHomeDirectory=defaultProps.getProperty("home");
		int ncrangemin=Integer.parseInt(defaultProps.getProperty("ncrangemin"));
		int ncrangemax=Integer.parseInt(defaultProps.getProperty("ncrangemax"));
		this.mNetconfPortRange = new PortRange(ncrangemin, ncrangemax);
		int snmprangemin=Integer.parseInt(defaultProps.getProperty("snmprangemin"));
		int snmprangemax=Integer.parseInt(defaultProps.getProperty("snmprangemax"));
		this.mSNMPPortRange = new PortRange(snmprangemin, snmprangemax);
		this.mHostIp=defaultProps.getProperty("host");
		this.mPort=Integer.parseInt(defaultProps.getProperty("port"));
		this.mLogfilename=defaultProps.getProperty("logfile");
		this.mLogLevel = Level.toLevel(defaultProps.getProperty("loglevel",DEFAULT_LOGLEVEL), Level.WARN);
		int jmxrangemin=Integer.parseInt(defaultProps.getProperty("jmxrangemin"));
		int jmxrangemax=Integer.parseInt(defaultProps.getProperty("jmxrangemax"));
		this.mJmxPortRange = new PortRange(jmxrangemin, jmxrangemax);
		//=====global mediator sepecific configurations
		this.mMediatorJavaMemoryParams = strclean(defaultProps.getProperty("MediatorMemory",DEFAULT_MEMORYSETTING));
		this.mMediatorLogLevel = Level.toLevel(defaultProps.getProperty("MediatorLogLevel",DEFAULT_MEDIATORLOGLEVEL), Level.WARN);
		this.mMediatorDevicePingTimeout=Integer.parseInt(defaultProps.getProperty("MediatorDevicePingTimeout",String.format("%d",DEFAULT_MEDIATORDEVICEPING_TIMEOUT)));
		this.mMediatorSnmpLatency=Integer.parseInt(defaultProps.getProperty("MediatorSnmpLatency",String.format("%d",DEFAULT_MEDIATORSNMP_LATENCY)));


	}

	public String getHostIp() {
		return this.mHostIp;
	}
	public String getHome()
	{	return this.mHomeDirectory;}
	public String getAbsolutePath(String rel)
	{
		String s=this.mHomeDirectory;
		if(!s.endsWith(DS) && !rel.startsWith(DS))
			s+=DS;
		return s+rel;
	}
	public int getPort() {
		return this.mPort;
	}

	public String getLogfilename() {
		return this.mLogfilename;
	}
	public PortRange getNetconfPortRange() {
		return this.mNetconfPortRange;
	}
	public PortRange getSNMPPortRange() {
		return this.mSNMPPortRange;
	}
	public Level getLogLevel() {
		return this.mLogLevel;
	}
	public PortRange getJmxPortRange() {
		return this.mJmxPortRange;
	}

	public String getMediatorJavaMemoryParameters()
	{return this.mMediatorJavaMemoryParams;}
	public int getMediatorDevicePingTimeout()
	{return this.mMediatorDevicePingTimeout;}
	public int getMediatorSnmpLatency()
	{return this.mMediatorSnmpLatency;}
	public Level getMediatorLogLevel()
	{return this.mMediatorLogLevel;}
	public String toJSON() {

		return String.format("{\"%s\":\"%s\","//home
				+ "\"%s\":\"%s\","//host
				+ "\"%s\":%d,"//port
				+ "\"%s\":%s,"//range nc
				+ "\"%s\":%s,"//range snmp
				+ "\"%s\":%s,"//range jmx
				+ "\"%s\":\"%s\","//loglevel
				+ "\"%s\":\"%s\","//logfile
				+ "\"%s\":\"%s\","//med-loglevel
				+ "\"%s\":%d,"//med-deviceping
				+ "\"%s\":%d,"//med-snmplatency
				+ "\"%s\":\"%s\""//med-memory
				+ "}",
				JSONKEY_HOMEDIR,this.mHomeDirectory,
				JSONKEY_HOSTIP,this.mHostIp,
				JSONKEY_HOSTPORT,this.mPort,
				JSONKEY_PORTRANGE_NETCONF,this.mNetconfPortRange.toJSON(),
				JSONKEY_PORTRANGE_SNMP,this.mSNMPPortRange.toJSON(),
				JSONKEY_PORTRANGE_JMX,this.mJmxPortRange.toJSON(),
				JSONKEY_LOGLEVEL,this.mLogLevel.toString(),
				JSONKEY_LOGFILE,this.mLogfilename,
				JSONKEY_MED_LOGLEVEL,this.mMediatorLogLevel.toString(),
				JSONKEY_MED_DEVICEPING_TIMEOUT,this.mMediatorDevicePingTimeout,
				JSONKEY_MED_SNMPLATENCY,this.mMediatorSnmpLatency,
				JSONKEY_MED_MEMORY,this.mMediatorJavaMemoryParams
			);
	}



}
