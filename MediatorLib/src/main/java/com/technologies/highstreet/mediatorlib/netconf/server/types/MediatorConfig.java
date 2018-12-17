package com.technologies.highstreet.mediatorlib.netconf.server.types;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.*;

public class MediatorConfig {

	private static final Log LOG  = LogFactory.getLog(MediatorConfig.class);

	public static class ODLConfig {
		private static final String JSONKEY_PROTOCOL = "Protocol";
		private static final String JSONKEY_TRUSTINSEC="Trustall";
		private static final String JSONKEY_SERVER = "Server";
		private static final String JSONKEY_PORT = "Port";
		private static final String JSONKEY_USER = "User";
		private static final String JSONKEY_PASSWORD = "Password";
		private static final boolean USESECUREACCESS_DEFAULT = false; // http or https
		private static final boolean TRUSTALLCERTS_DEFAULT = false;// trust all certificates indicator
	
																		
		public final String Server;
		public final int Port;
		public final String User;
		public final String Password;
		public final boolean Https;
		public final boolean TrustAll;
		
		public ODLConfig(String server, int port, String user, String passwd) {
			this(server, port, user, passwd, USESECUREACCESS_DEFAULT,TRUSTALLCERTS_DEFAULT);
		}

		public ODLConfig(String server, int port, String user, String passwd, boolean https,boolean trustall) {
			this.Server = server;
			this.Port = port;
			this.User = user;
			this.Password = passwd;
			this.Https = https;
			this.TrustAll=trustall;
		}

		public String toJSON() {
			return String.format("{\"%s\":\"%s\",\"%s\":%s,\"%s\":\"%s\",\"%s\":%d,\"%s\":\"%s\",\"%s\":\"%s\"}", 
					JSONKEY_PROTOCOL,this.Https?"https":"http",
					JSONKEY_TRUSTINSEC,this.TrustAll?"true":"false",
					JSONKEY_SERVER, this.Server,
					JSONKEY_PORT, this.Port, 
					JSONKEY_USER, this.User, 
					JSONKEY_PASSWORD, this.Password);
		}

		public static ODLConfig FromJSON(JSONObject o) {
			return new ODLConfig(
					o.getString(JSONKEY_SERVER), 
					o.getInt(JSONKEY_PORT), 
					o.getString(JSONKEY_USER),
					o.getString(JSONKEY_PASSWORD),
					o.has(JSONKEY_PROTOCOL)?o.getString(JSONKEY_PROTOCOL).equals("https"):USESECUREACCESS_DEFAULT,
					o.has(JSONKEY_TRUSTINSEC)?o.getBoolean(JSONKEY_TRUSTINSEC):TRUSTALLCERTS_DEFAULT);
		}

		public String GetHostURL() {
			return String.format("http%s://%s:%d/", this.Https ? "s" : "", this.Server, this.Port);
		}
	}

	public static class ODLConfigCollection extends ArrayList<ODLConfig> {

		/**
		 *
		 */
		private static final long serialVersionUID = 6595311705783838862L;

		public static ODLConfigCollection FromJSON(JSONArray a) {
			ODLConfigCollection c = new ODLConfigCollection();
			for (int i = 0; i < a.length(); i++) {
				c.add(ODLConfig.FromJSON(a.getJSONObject(i)));

			}
			return c;
		}

		public String toJSON() {
			if (this.size() <= 0)
				return "[]";
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			if (this.size() > 0)
				sb.append(this.get(0).toJSON());
			for (int i = 1; i < this.size(); i++)
				sb.append("," + this.get(i).toJSON() + "");
			sb.append("]");
			return sb.toString();
		}

	}

	public static final String JSONKEY_NAME = "Name";
	public static final String JSONKEY_DEVICEIP = "DeviceIp";
	public static final String JSONKEY_NEXMLFILE = "NeXMLFile";
	public static final String JSONKEY_NETCONFPORT = "NcPort";
	public static final String JSONKEY_NETCONFUSERNAME = "NcUsername";
	public static final String JSONKEY_NETCONFPASSWORD = "NcPassword";
	public static final String JSONKEY_ISNCCONNECTED = "IsNCConnected";
	public static final String JSONKEY_ISNECONNECTED = "IsNeConnected";
	public static final String JSONKEY_ODLCONFIG = "ODLConfig";
	public static final String JSONKEY_DEVICEPORT = "DevicePort";

	public static final String DEFAULT_NETCONF_USERNAME = "admin";
	public static final String DEFAULT_NETCONF_PASSWORD = "admin";
	
	protected static final int DEFAULT_DEVICEPORT = 161; // snmp
	protected static final int DEFAULT_PINGPORT = 23; // telnet

	protected String mName;
	protected String mDeviceIP;
	protected String mNeXMLFilename;
	protected boolean mIsNetconfConnected;
	protected boolean mIsNeConnected;
	protected String mFilename;
	protected int mNetconfPort;
	protected ODLConfigCollection mODLConfigs;
	protected String mLogFilename;
	protected int mDevicePort;
	protected String mWalkfilename;
	private String mXMLNeModelGeneratedFilename;
	protected int mAlternativePingPort;

	private String mNetconfUsername;
	private String mNetconfPassword;
	private String mHostkeyFilename;

	public void setName(String name) {
		this.mName = name;
	}

	public String getName() {
		return this.mName;
	}

	public String getWalkFilename() {
		return this.mWalkfilename;
	}

	public void setWalkFilename(String s) {
		this.mWalkfilename = s;
	}

	public String getXMLNeModelGeneratedFilename() {
		return this.mXMLNeModelGeneratedFilename;
	}

	public void setXMLNeModelGeneratedFilename(String s) {
		this.mXMLNeModelGeneratedFilename = s;
	}

	public String getXMLNeModelTemplateFilename() {
		return String.format("nemodel/%s_nemodel.template.xml", this.mName);
	}

	public void setDeviceIp(String ip) {
		this.mDeviceIP = ip;
	}

	public String getDeviceIp() {
		return this.mDeviceIP;
	}

	public int getDevicePort() {
		return this.mDevicePort;
	}

	public void setDevicePort(int port) {
		this.mDevicePort = port;
	}

	public void setNeXMLFilenae(String fn) {
		this.mNeXMLFilename = fn;
	}

	public String getNeXMLFilename() {
		return this.mNeXMLFilename;
	}

	public void setNetconfPort(int port) {
		this.mNetconfPort = port;
	}

	public int getNetconfPort() {
		return this.mNetconfPort;
	}

	public void setODLConfig(ODLConfigCollection cfg) {
		this.mODLConfigs = cfg;
	}

	public ODLConfigCollection getODLConfig() {
		return this.mODLConfigs;
	}

	public void setIsNetconfConnected(boolean isconnected) {
		this.mIsNetconfConnected = isconnected;
	}

	public boolean isNetconfConnected() {
		return this.mIsNetconfConnected;
	}

	public void setIsNeConnected(boolean isconnected) {
		this.mIsNeConnected = isconnected;
	}

	public boolean isNeConnected() {
		return this.mIsNeConnected;
	}

	public int getAlternateivePingPort() {
		return this.mAlternativePingPort;
	}

	public void setAlternateivePingPort(int port) {
		this.mAlternativePingPort = port;
	}
	public String getLogFilename() {
		return this.mLogFilename;
	}
	public String getNetconfUsername() {
		return this.mNetconfUsername;
	}
	public String getNetconfPassword() {
		return this.mNetconfPassword;
	}
	public String getHostKeyFilename() {
		return this.mHostkeyFilename;
	}
	protected MediatorConfig() {
		this.mFilename = null;
		this.mName = "";
		this.mDeviceIP = "";
		this.mDevicePort = DEFAULT_DEVICEPORT;
		this.mNeXMLFilename = "";
		this.mNetconfPort = 0;
		this.mIsNetconfConnected = false;
		this.mIsNeConnected = false;
		this.mAlternativePingPort = DEFAULT_PINGPORT;
		this.mODLConfigs = new ODLConfigCollection();
		this.mNetconfUsername = DEFAULT_NETCONF_USERNAME;
		this.mNetconfPassword = DEFAULT_NETCONF_PASSWORD;
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
			org.json.JSONObject o = new JSONObject(content);
			this.mFilename = filename;
			this.mLogFilename = this.mFilename.replace(".config", ".log");
			this.mHostkeyFilename = this.mFilename.substring(0, this.mFilename.lastIndexOf("/")+1)+"hostkey.ser";
			this.mName = o.getString(JSONKEY_NAME);
			this.mWalkfilename = String.format("mediators/%s/%s.walk", this.mName, this.mName);
			this.mXMLNeModelGeneratedFilename = String.format("mediators/%s/%s_nemodel.gen.xml", this.mName,
					this.mName);
			this.mDeviceIP = o.getString(JSONKEY_DEVICEIP);
			this.mNeXMLFilename = o.getString(JSONKEY_NEXMLFILE);
			this.mNetconfPort = o.getInt(JSONKEY_NETCONFPORT);
			this.mODLConfigs = ODLConfigCollection.FromJSON(o.getJSONArray(JSONKEY_ODLCONFIG));
			// optional items
			try {
				this.mDevicePort = o.getInt(JSONKEY_DEVICEPORT);
			} catch (Exception err) {
				this.mDevicePort = DEFAULT_DEVICEPORT;
			}
			try {
				this.mIsNetconfConnected = o.getBoolean(JSONKEY_ISNCCONNECTED);
			} catch (Exception err) {
			}
			try {
				this.mIsNeConnected = o.getBoolean(JSONKEY_ISNECONNECTED);
			} catch (Exception err) {
			}
			try {
				this.mNetconfUsername = o.getString(JSONKEY_NETCONFUSERNAME);
			} catch (Exception err) {
				
			}
			if(this.mNetconfUsername==null)
				this.mNetconfUsername=DEFAULT_NETCONF_USERNAME;
			try {
				this.mNetconfPassword = o.getString(JSONKEY_NETCONFPASSWORD);
			} catch (Exception err) {
			
			}
			if(this.mNetconfPassword==null)
				this.mNetconfPassword = DEFAULT_NETCONF_PASSWORD;
			this.initVars(o);
		} finally {
			br.close();
		}
	}

	protected void initVars(JSONObject o) {

	}

	protected void addJsonVars(List<String> jsonItems) {
	}

	public String toJSON()
	{
		return this.toJSON(true);
	}
	public String toJSON(boolean withpasswd) {
		List<String> jsonItems = new ArrayList<String>();
		jsonItems.add(String.format("\"%s\":\"%s\"", JSONKEY_NAME, this.mName));
		jsonItems.add(String.format("\"%s\":\"%s\"", JSONKEY_DEVICEIP, this.mDeviceIP));
		jsonItems.add(String.format("\"%s\":%d", JSONKEY_DEVICEPORT, this.mDevicePort));
		jsonItems.add(String.format("\"%s\":\"%s\"", JSONKEY_NEXMLFILE, this.mNeXMLFilename));
		jsonItems.add(String.format("\"%s\":%d", JSONKEY_NETCONFPORT, this.mNetconfPort));
		jsonItems.add(String.format("\"%s\":\"%s\"", JSONKEY_NETCONFUSERNAME, this.mNetconfUsername));
		jsonItems.add(String.format("\"%s\":\"%s\"", JSONKEY_NETCONFPASSWORD, withpasswd?this.mNetconfPassword:"***"));
		jsonItems.add(String.format("\"%s\":%s", JSONKEY_ODLCONFIG, this.mODLConfigs.toJSON()));
		jsonItems.add(String.format("\"%s\":%s", JSONKEY_ISNCCONNECTED, this.mIsNetconfConnected ? "true" : "false"));
		jsonItems.add(String.format("\"%s\":%s", JSONKEY_ISNECONNECTED, this.mIsNeConnected ? "true" : "false"));
		this.addJsonVars(jsonItems);
		return "{" + String.join(",", jsonItems) + "}";
	}

	public MediatorConfig save() throws Exception {
		if (this.mFilename == null)
			throw new Exception("no filename specified");
		LOG.trace(this.toJSON(false));
		FileWriter fw=new FileWriter(this.mFilename);
		BufferedWriter w = new BufferedWriter(fw);
		w.write(this.toJSON());
		w.flush();
		fw.close();
		w.close();
		
		return this;
	}

	public MediatorConfig saveTo(String filename) throws Exception {
		this.mFilename = filename;
		return this.save();
	}

	/*
	 * public static MediatorConfig FromJSON(String json) throws Exception {
	 * MediatorConfig cfg; try { org.json.JSONObject o = new JSONObject(json); cfg =
	 * new MediatorConfig(); cfg.mName = o.getString(JSONKEY_NAME);
	 * cfg.mWalkfilename=String.format("mediators/%s/%s.walk", cfg.mName,cfg.mName);
	 * cfg.mXMLNeModelGeneratedFilename =
	 * String.format("mediators/%s/%s_nemodel.gen.xml", cfg.mName, cfg.mName);
	 * cfg.mDeviceIP = o.getString(JSONKEY_DEVICEIP); cfg.mNeXMLFilename =
	 * o.getString(JSONKEY_NEXMLFILE); cfg.mNetconfPort =
	 * o.getInt(JSONKEY_NETCONFPORT); cfg.mODLConfigs =
	 * ODLConfigCollection.FromJSON(o.getJSONArray(JSONKEY_ODLCONFIG)); // not
	 * neccessary options try { cfg.mDevicePort = o.getInt(JSONKEY_DEVICEPORT); }
	 * catch (Exception e) { cfg.mDevicePort = DEFAULT_DEVICEPORT; } try {
	 * cfg.mIsNetconfConnected = o.getBoolean(JSONKEY_ISNCCONNECTED); } catch
	 * (Exception e) { cfg.mIsNetconfConnected = false; } try { cfg.mIsNeConnected =
	 * o.getBoolean(JSONKEY_ISNECONNECTED); } catch (Exception e) {
	 * cfg.mIsNeConnected = false; } } catch (Exception err) { throw new
	 * Exception("invalid json"); } return cfg; }
	 */
	public ODLConfig getFirstODLConfig() {
		if (this.mODLConfigs != null && this.mODLConfigs.size() > 0)
			return this.mODLConfigs.get(0);
		return null;
	}

	public static void ValidateName(String name) throws Exception {
		if (name == null)
			throw new Exception("name is null");
		if (name.trim() == "")
			throw new Exception("name is empty");
		if (name.startsWith("."))
			throw new Exception("name cannot start with a '.'");
		if (name.split(" ").length > 1)
			throw new Exception("name with whitespaces are not allowed");

	}

	public String getCurrentPID() {
		String s = ManagementFactory.getRuntimeMXBean().getName();
		if (s.contains("@"))
			s = s.substring(0, s.indexOf("@"));
		return s;
	}

	public void writePIDFile() {
		try {
			String s = this.getCurrentPID();
			if (s != null) {
				BufferedWriter w = new BufferedWriter(new FileWriter(this.mFilename.replace(".config", ".pid")));
				w.write(s);
				w.flush();
				w.close();
			}
		} catch (Exception e) {

		}

	}

	public void deletePIDFile() {
		File f = new File(this.mFilename.replace(".config", ".pid"));
		if (f.exists())
			f.delete();
	}

	
}
