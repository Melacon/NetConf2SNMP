package com.technologies.highstreet.mediatorserver.data;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.*;

import com.technologies.highstreet.mediatorlib.data.MediatorServerProperties;
import com.technologies.highstreet.mediatorlib.data.PortRange;
import com.technologies.highstreet.mediatorlib.netconf.server.types.MediatorConfig;
import com.technologies.highstreet.mediatorserver.files.MediatorCoreFiles;
import com.technologies.highstreet.mediatorserver.files.MediatorFiles;


public class ServerSideMediatorConfig extends MediatorConfig implements IJSONable{


	public static class ConfigStatusCollection extends JSONableCollection<ConfigStatus>
	{

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

	}
	public static class ConfigStatus implements IJSONable
	{
		public static final int STATUS_OKAY = 1;
		public static final int STATUS_CORRUPTED = 2;
		public static final int STATUS_LOCKED = 3;
		public static final int STATUS_REPAIRED = 4;
		public final String Name;
		public final int Status;

		public ConfigStatus(String name,int status)
		{
			this.Name = name;
			this.Status = status;
		}

		@Override
		public String toJSON() {
			return String.format("{\"Name\":\"%s\",\"Status\":%d}",this.Name,this.Status);
		}
	}

	//public static final String JSONKEY_NAME = "Name";
	public static final String JSONKEY_DEVICETYPE = "DeviceType";
	//public static final String JSONKEY_DEVICEIP = "DeviceIp";
	//public static final String JSONKEY_DEVICEPORT = "DevicePort";
	public static final String JSONKEY_TRAPPORT = "TrapPort";
	//public static final String JSONKEY_NEXMLFILE = "NeXMLFile";
	//public static final String JSONKEY_NETCONFPORT = "NcPort";
	//public static final String JSONKEY_ISNCCONNECTED = "IsNCConnected";
	//public static final String JSONKEY_ISNECONNECTED = "IsNeConnected";
	//public static final String JSONKEY_ODLCONFIG = "ODLConfig";
	//private static final int DEFAULT_DEVICEPORT = 161;

	private static String mHostIp="";
	public static void SetHostIp(String ip)
	{ServerSideMediatorConfig.mHostIp=ip;}
	public static String GetHostIP() {
		return ServerSideMediatorConfig.mHostIp;
	}

	//protected String mName;
	protected long mDeviceType;
	//protected String mDeviceIP;
	//protected int mDevicePort;
	protected int mTrapPort;
	//protected String mNeXMLFilename;
	//protected boolean mIsNetconfConnected;
	protected boolean mIsNetworkElementConnected;
	//protected String mFilename;
	//protected int mNetconfPort;
	//protected ODLConfigCollection mODLConfigs;



	public void setDeviceType(long type)
	{this.mDeviceType=type;}
	public long getDeviceType()
	{return this.mDeviceType;}
	public int getTrapPort()
	{return this.mTrapPort;	}
	public void setTrapPort(int port)
	{this.mTrapPort=port;}
	
	private ServerSideMediatorConfig()
	{
		super();
		this.mDeviceType=0;
		this.mTrapPort=0;
	}
	
	public ServerSideMediatorConfig(String filename) throws IOException {
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
		    this.mDeviceType = o.getLong(JSONKEY_DEVICETYPE);
		    this.mDeviceIP = o.getString(JSONKEY_DEVICEIP);
		    this.mTrapPort = o.getInt(JSONKEY_TRAPPORT);
		    this.mNeXMLFilename = o.getString(JSONKEY_NEXMLFILE);
		    this.mNetconfPort=o.getInt(JSONKEY_NETCONFPORT);
		    this.mIsNetconfConnected = o.getBoolean(JSONKEY_ISNCCONNECTED);
		    try {    this.mIsNetworkElementConnected = o.getBoolean(JSONKEY_ISNECONNECTED);}
		    catch(Exception err) {this.mIsNetworkElementConnected=false;}
		    try{this.mDevicePort = o.getInt(JSONKEY_DEVICEPORT);}
			catch(Exception e){this.mDevicePort=DEFAULT_DEVICEPORT;}

		    this.mODLConfigs=ODLConfigCollection.FromJSON(o.getJSONArray(JSONKEY_ODLCONFIG));

		} finally {
		    br.close();
		}
	}
	@Override
	protected void addJsonVars(List<String> jsonItems) {
		super.addJsonVars(jsonItems);
		jsonItems.add(String.format("\"%s\":%d",JSONKEY_DEVICETYPE,this.mDeviceType));
		jsonItems.add(String.format("\"%s\":%d",JSONKEY_TRAPPORT,this.mTrapPort));
	}
	/*
	@Override
	public String toJSON()
	{
		StringBuilder sb=new StringBuilder();
		sb.append("{");
		sb.append(String.format("\"%s\":\"%s\",",JSONKEY_NAME,this.mName));
		sb.append(String.format("\"%s\":%d,",JSONKEY_DEVICETYPE,this.mDeviceType));
		sb.append(String.format("\"%s\":\"%s\",",JSONKEY_DEVICEIP,this.mDeviceIP));
		sb.append(String.format("\"%s\":%d,",JSONKEY_DEVICEPORT,this.mDevicePort));
		sb.append(String.format("\"%s\":%d,",JSONKEY_TRAPPORT,this.mTrapPort));
		sb.append(String.format("\"%s\":\"%s\",",JSONKEY_NEXMLFILE,this.mNeXMLFilename));
		sb.append(String.format("\"%s\":%d,",JSONKEY_NETCONFPORT,this.mNetconfPort));
		sb.append(String.format("\"%s\":%s,",JSONKEY_ISNCCONNECTED,this.mIsNetconfConnected?"true":"false"));
		sb.append(String.format("\"%s\":%s,",JSONKEY_ISNECONNECTED,this.mIsNetworkElementConnected?"true":"false"));
		sb.append(String.format("\"%s\":%s",JSONKEY_ODLCONFIG,this.mODLConfigs.toJSON()));
		sb.append("}");
		return sb.toString();
	}
	*/
	public ServerSideMediatorConfig save() throws Exception
	{
		if(this.mFilename==null)
			throw new Exception("no filename specified");
		BufferedWriter w=new BufferedWriter(new FileWriter(this.mFilename));
		w.write(this.toJSON());
		w.flush();
		w.close();
		return this;
	}
	public ServerSideMediatorConfig saveTo(String filename) throws Exception
	{
		this.mFilename=filename;
		return this.save();
	}
	public static ServerSideMediatorConfig Create(String filename) throws Exception {
		return ServerSideMediatorConfig.Empty().saveTo(filename);
	}

	private static ServerSideMediatorConfig Empty() {
		return new ServerSideMediatorConfig();
	}
	
	private static Object getJSON(final String s) throws Exception
	{
		Object r=null;
		try {
			r=new JSONObject(s);
		}
		catch(Exception err){
			try {
			r=new JSONArray(s);
			}
			catch(Exception errInner)
			{}
		}
		if(r==null)
			throw new Exception("unable to parse json");
		return r;
	}
	public static List<ServerSideMediatorConfig> FromJSON(String json) throws Exception {
		List<ServerSideMediatorConfig> configList=new ArrayList<ServerSideMediatorConfig>();
		ServerSideMediatorConfig cfg;
		Object obj=getJSON(json);
		if(obj instanceof JSONObject)
		{
			cfg=getConfig((JSONObject)obj);
			configList.add(cfg);
		}
		else if(obj instanceof JSONArray)
		{
			JSONArray a = (JSONArray)obj;
			for(int i=0;i<a.length();i++)
				configList.add(getConfig(a.getJSONObject(i)));
		}
		return configList;
	}

	private static ServerSideMediatorConfig getConfig(JSONObject o) throws Exception{
		ServerSideMediatorConfig cfg;
		cfg=new ServerSideMediatorConfig();
		cfg.mName=o.getString(JSONKEY_NAME);
		cfg.mDeviceType = o.getLong(JSONKEY_DEVICETYPE);
		cfg.mDeviceIP = o.getString(JSONKEY_DEVICEIP);
		cfg.mTrapPort = o.getInt(JSONKEY_TRAPPORT);
		cfg.mNeXMLFilename = o.getString(JSONKEY_NEXMLFILE);
		cfg.mNetconfPort=o.getInt(JSONKEY_NETCONFPORT);
		cfg.mODLConfigs=ODLConfigCollection.FromJSON(o.getJSONArray(JSONKEY_ODLCONFIG));
		
		//not neccessary options
		try{cfg.mDevicePort = o.getInt(JSONKEY_DEVICEPORT);}
		catch(Exception e){cfg.mDevicePort=DEFAULT_DEVICEPORT;}
		try{cfg.mIsNetconfConnected = o.getBoolean(JSONKEY_ISNCCONNECTED);}
		catch(Exception e){cfg.mIsNetconfConnected=false;}
		try{cfg.mIsNetworkElementConnected = o.getBoolean(JSONKEY_ISNECONNECTED);}
		catch(Exception e){cfg.mIsNetworkElementConnected=false;}
		return cfg;
	}
	public static boolean IsNetconfPortAvailable(int port)
	{
		try {
			MediatorConfigCollection configs=MediatorFiles.GetConfigs();
			PortRange pr = MediatorServerProperties.getInstance().getNetconfPortRange();
			if(configs!=null && configs.size()>0)
			{
				for(ServerSideMediatorConfig c:configs)
					pr.AddException(c.mNetconfPort);
			}
			return pr.IsAvailable(port);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	public static boolean IsSNMPPortAvailable(int port)
	{
		try {
			MediatorConfigCollection configs=MediatorFiles.GetConfigs();
			PortRange pr = MediatorServerProperties.getInstance().getSNMPPortRange();
			if(configs!=null && configs.size()>0)
			{
				for(ServerSideMediatorConfig c:configs)
					pr.AddException(c.mTrapPort);
			}
			return pr.IsAvailable(port);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	public static boolean IsNetconfPortInRange(int port) {
		return MediatorServerProperties.getInstance().getNetconfPortRange().IsInRange(port);
	}
	public static PortRange getNetconfPortRange()
	{
		return MediatorServerProperties.getInstance().getNetconfPortRange();
	}
	public static PortRange getSNMPPortRange()
	{
		return MediatorServerProperties.getInstance().getSNMPPortRange();
	}
	public static boolean IsSNMPPortInRange(int port) {
		return MediatorServerProperties.getInstance().getSNMPPortRange().IsInRange(port);
	}
	public static int[] FindAvailableNetconfPorts(int limit) {
		try {
			MediatorConfigCollection configs=MediatorFiles.GetConfigs();
			PortRange pr = MediatorServerProperties.getInstance().getNetconfPortRange();
			if(configs!=null && configs.size()>0)
			{
				for(ServerSideMediatorConfig c:configs)
					pr.AddException(c.mNetconfPort);
			}
			return pr.getFree(limit);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new int[0];
	}
	public static int[] FindAvailableSNMPPorts(int limit) {
		try {
			MediatorConfigCollection configs=MediatorFiles.GetConfigs();
			PortRange pr = MediatorServerProperties.getInstance().getSNMPPortRange();
			if(configs!=null && configs.size()>0)
			{
				for(ServerSideMediatorConfig c:configs)
					pr.AddException(c.mTrapPort);
			}
			return pr.getFree(limit);

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
	public boolean hasODLConfig() {
		return this.getFirstODLConfig()!=null;
	}





}
