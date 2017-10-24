package com.technologies.highstreet.mediatorserver.data;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.*;

import com.technologies.highstreet.mediatorserver.files.MediatorCoreFiles;
import com.technologies.highstreet.mediatorserver.files.MediatorFiles;
import com.technologies.highstreet.mediatorserver.server.MyProperties;

public class MediatorConfig  implements IJSONable{


	public static class ODLConfig implements IJSONable
	{
		private static final String JSONKEY_SERVER = "Server";
		private static final String JSONKEY_PORT = "Port";
		private static final String JSONKEY_USER = "User";
		private static final String JSONKEY_PASSWORD = "Password";
		private static final boolean USESECUREACCESS_DEFAULT = false; //http or https
		public final String Server;
		public final int Port;
		public final String User;
		public final String Password;
		public final boolean UseSecure;
		public ODLConfig(String server,int port,String user,String passwd)
		{
			this(server,port,user,passwd,USESECUREACCESS_DEFAULT);
		}
		public ODLConfig(String server,int port,String user,String passwd,boolean secure)
		{
			this.Server=server;
			this.Port=port;
			this.User=user;
			this.Password=passwd;
			this.UseSecure=secure;
		}
		@Override
		public String toJSON()
		{
			return String.format("{\"%s\":\"%s\",\"%s\":%d,\"%s\":\"%s\",\"%s\":\"%s\"}",
					JSONKEY_SERVER,this.Server,JSONKEY_PORT,this.Port,JSONKEY_USER,this.User,JSONKEY_PASSWORD,this.Password);
		}
		public static ODLConfig FromJSON(JSONObject o) {
			return new ODLConfig(o.getString(JSONKEY_SERVER), o.getInt(JSONKEY_PORT),o.getString(JSONKEY_USER), o.getString(JSONKEY_PASSWORD));
		}
		public String GetHostURL() {
			return String.format("http%s://%s:%d/",this.UseSecure?"s":"",this.Server,this.Port);
		}
	}
	public static class ODLConfigCollection extends JSONableCollection<ODLConfig>
	{

		/**
		 *
		 */
		private static final long serialVersionUID = 6595311705783838862L;

		public static ODLConfigCollection FromJSON(JSONArray a) {
			ODLConfigCollection c=new ODLConfigCollection();
			for(int i=0;i<a.length();i++)
			{
				c.add(ODLConfig.FromJSON(a.getJSONObject(i)));

			}
			return c;
		}

	}
	public static final String JSONKEY_NAME = "Name";
	public static final String JSONKEY_DEVICETYPE = "DeviceType";
	public static final String JSONKEY_DEVICEIP = "DeviceIp";
	public static final String JSONKEY_TRAPPORT = "TrapPort";
	public static final String JSONKEY_NEXMLFILE = "NeXMLFile";
	public static final String JSONKEY_NETCONFPORT = "NcPort";
	public static final String JSONKEY_ISNCCONNECTED = "IsNCConnected";
	public static final String JSONKEY_ISNECONNECTED = "IsNeConnected";
	public static final String JSONKEY_ODLCONFIG = "ODLConfig";

	private static String mHostIp="";
	public static void SetHostIp(String ip)
	{MediatorConfig.mHostIp=ip;}
	public static String GetHostIP() {
		return MediatorConfig.mHostIp;
	}

	protected String mName;
	protected int mDeviceType;
	protected String mDeviceIP;
	protected int mTrapPort;
	protected String mNeXMLFilename;
	protected boolean mIsNetconfConnected;
	protected boolean mIsNetworkElementConnected;
	protected String mFilename;
	protected int mNetconfPort;
	protected ODLConfigCollection mODLConfigs;



	public void setName(String name)
	{this.mName=name;	}
	public String getName()
	{return this.mName;}
	public void setDeviceType(int type)
	{this.mDeviceType=type;}
	public int getDeviceType()
	{return this.mDeviceType;}
	public void setDeviceIp(String ip)
	{this.mDeviceIP=ip;}
	public String getDeviceIp()
	{return this.mDeviceIP;	}
	public void setNeXMLFilename(String fn)
	{this.mNeXMLFilename=fn;}
	public String getNeXMLFilename()
	{return this.mNeXMLFilename;}
	public void setNetconfPort(int port)
	{this.mNetconfPort=port;}
	public int getNetconfPort()
	{return this.mNetconfPort;}
	public int getTrapPort()
	{return this.mTrapPort;	}
	public void setTrapPort(int port)
	{this.mTrapPort=port;}
	public void setODLConfig(ODLConfigCollection cfg)
	{this.mODLConfigs=cfg;}
	public ODLConfigCollection getODLConfig()
	{return this.mODLConfigs;}


	private MediatorConfig()
	{
		this.mFilename=null;
		this.mName="";
		this.mDeviceType=0;
		this.mDeviceIP="";
		this.mTrapPort=0;
		this.mNeXMLFilename="";
		this.mNetconfPort=0;
		this.mIsNetconfConnected=false;
		this.mODLConfigs=new ODLConfigCollection();
	}
	public MediatorConfig(String filename) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
		try {
		    StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        sb.append(System.lineSeparator());
		        line = br.readLine();
		    }
		    String content = sb.toString();
		    org.json.JSONObject o=new JSONObject(content);
		    this.mFilename=filename;
		    this.mName=o.getString(JSONKEY_NAME);
		    this.mDeviceType = o.getInt(JSONKEY_DEVICETYPE);
		    this.mDeviceIP = o.getString(JSONKEY_DEVICEIP);
		    this.mTrapPort = o.getInt(JSONKEY_TRAPPORT);
		    this.mNeXMLFilename = o.getString(JSONKEY_NEXMLFILE);
		    this.mNetconfPort=o.getInt(JSONKEY_NETCONFPORT);
		    this.mIsNetconfConnected = o.getBoolean(JSONKEY_ISNCCONNECTED);
		    try {    this.mIsNetworkElementConnected = o.getBoolean(JSONKEY_ISNECONNECTED);}
		    catch(Exception err) {this.mIsNetworkElementConnected=false;}
		    this.mODLConfigs=ODLConfigCollection.FromJSON(o.getJSONArray(JSONKEY_ODLCONFIG));

		} finally {
		    br.close();
		}
	}
	@Override
	public String toJSON()
	{
		StringBuilder sb=new StringBuilder();
		sb.append("{");
		sb.append(String.format("\"%s\":\"%s\",",JSONKEY_NAME,this.mName));
		sb.append(String.format("\"%s\":%d,",JSONKEY_DEVICETYPE,this.mDeviceType));
		sb.append(String.format("\"%s\":\"%s\",",JSONKEY_DEVICEIP,this.mDeviceIP));
		sb.append(String.format("\"%s\":%d,",JSONKEY_TRAPPORT,this.mTrapPort));
		sb.append(String.format("\"%s\":\"%s\",",JSONKEY_NEXMLFILE,this.mNeXMLFilename));
		sb.append(String.format("\"%s\":%d,",JSONKEY_NETCONFPORT,this.mNetconfPort));
		sb.append(String.format("\"%s\":%s,",JSONKEY_ISNCCONNECTED,this.mIsNetconfConnected?"true":"false"));
		sb.append(String.format("\"%s\":%s,",JSONKEY_ISNECONNECTED,this.mIsNetworkElementConnected?"true":"false"));
		sb.append(String.format("\"%s\":%s",JSONKEY_ODLCONFIG,this.mODLConfigs.toJSON()));
		sb.append("}");
		return sb.toString();
	}
	public MediatorConfig save() throws Exception
	{
		if(this.mFilename==null)
			throw new Exception("no filename specified");
		BufferedWriter w=new BufferedWriter(new FileWriter(this.mFilename));
		w.write(this.toJSON());
		w.flush();
		w.close();
		return this;
	}
	public MediatorConfig saveTo(String filename) throws Exception
	{
		this.mFilename=filename;
		return this.save();
	}
	public static MediatorConfig Create(String filename) throws Exception {
		return MediatorConfig.Empty().saveTo(filename);
	}

	private static MediatorConfig Empty() {
		return new MediatorConfig();
	}
	public static MediatorConfig FromJSON(String json) throws Exception {
		MediatorConfig cfg;
		try
		{
		org.json.JSONObject o=new JSONObject(json);
		cfg=new MediatorConfig();
		cfg.mName=o.getString(JSONKEY_NAME);
		cfg.mDeviceType = o.getInt(JSONKEY_DEVICETYPE);
		cfg.mDeviceIP = o.getString(JSONKEY_DEVICEIP);
		cfg.mTrapPort = o.getInt(JSONKEY_TRAPPORT);
		cfg.mNeXMLFilename = o.getString(JSONKEY_NEXMLFILE);
		cfg.mNetconfPort=o.getInt(JSONKEY_NETCONFPORT);
		cfg.mODLConfigs=ODLConfigCollection.FromJSON(o.getJSONArray(JSONKEY_ODLCONFIG));
		//not neccessary options
		try{cfg.mIsNetconfConnected = o.getBoolean(JSONKEY_ISNCCONNECTED);}
		catch(Exception e){cfg.mIsNetconfConnected=false;}
		try{cfg.mIsNetworkElementConnected = o.getBoolean(JSONKEY_ISNECONNECTED);}
		catch(Exception e){cfg.mIsNetworkElementConnected=false;}
		}
		catch(Exception err)
		{
			throw new Exception("invalid json");
		}
		return cfg;
	}

	public static boolean IsNetconfPortAvailable(int port)
	{
		try {
			MediatorConfigCollection configs=MediatorFiles.GetConfigs();
			if(configs!=null && configs.size()>0)
			{
				for(MediatorConfig c:configs)
					MyProperties.getInstance().getNetconfPortRange().AddException(c.mNetconfPort);
			}
			return MyProperties.getInstance().getNetconfPortRange().IsAvailable(port);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	public static boolean IsSNMPPortAvailable(int port)
	{
		try {
			MediatorConfigCollection configs=MediatorFiles.GetConfigs();
			if(configs!=null && configs.size()>0)
			{
				for(MediatorConfig c:configs)
					MyProperties.getInstance().getSNMPPortRange().AddException(c.mTrapPort);
			}
			return MyProperties.getInstance().getSNMPPortRange().IsAvailable(port);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	public static boolean IsNetconfPortInRange(int port) {
		return MyProperties.getInstance().getNetconfPortRange().IsInRange(port);
	}
	public static boolean IsSNMPPortInRange(int port) {
		return MyProperties.getInstance().getSNMPPortRange().IsInRange(port);
	}
	public static int[] FindAvailableNetconfPorts(int limit) {
		try {
			MediatorConfigCollection configs=MediatorFiles.GetConfigs();
			if(configs!=null && configs.size()>0)
			{
				for(MediatorConfig c:configs)
					MyProperties.getInstance().getNetconfPortRange().AddException(c.mNetconfPort);
			}
			return MyProperties.getInstance().getNetconfPortRange().getFree(limit);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new int[0];
	}
	public static int[] FindAvailableSNMPPorts(int limit) {
		try {
			MediatorConfigCollection configs=MediatorFiles.GetConfigs();
			if(configs!=null && configs.size()>0)
			{
				for(MediatorConfig c:configs)
					MyProperties.getInstance().getSNMPPortRange().AddException(c.mTrapPort);
			}
			return MyProperties.getInstance().getSNMPPortRange().getFree(limit);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new int[0];
	}
	public ODLConfig getFirstODLConfig() {
		if(this.mODLConfigs!=null && this.mODLConfigs.size()>0)
			return this.mODLConfigs.get(0);
		return null;
	}
	public static void ValidateName(String name) throws Exception{
		if(name==null)
			throw new Exception("name is null");
		if(name.trim()=="")
			throw new Exception("name is empty");
		if(name.startsWith("."))
			throw new Exception("name cannot start with a '.'");
		if(name.split(" ").length>1)
			throw new Exception("name with whitespaces are not allowed");

	}
	/*
	 * create relative path for xmlNeFile
	 * abc.xml => <nepath>/abc.xml
	 *
	 */
	public void checkXMLNePath(String homeDir, String nemodelpath) {
		if(this.mNeXMLFilename.contains(nemodelpath))
			return;
		String DS = MediatorCoreFiles.DS;
		if(nemodelpath.startsWith(DS))
			nemodelpath=nemodelpath.substring(1);
		if(!nemodelpath.endsWith(DS))
			nemodelpath+=DS;
		if(this.mNeXMLFilename.startsWith(DS))
			this.mNeXMLFilename=this.mNeXMLFilename.substring(1);
		this.mNeXMLFilename=nemodelpath+this.mNeXMLFilename;
	}





}