package com.technologies.highstreet.netconf2snmpmediator.server;

import java.io.IOException;
import java.util.List;
import org.json.JSONObject;
import com.technologies.highstreet.mediatorlib.netconf.server.types.MediatorConfig;

public class SNMPMediatorConfig extends MediatorConfig{

	public static final String JSONKEY_DEVICETYPE = "DeviceType";
	public static final String JSONKEY_TRAPPORT = "TrapPort";
	public static final String JSONKEY_ALTPINGPORT = "PingPort";
	public static final String JSONKEY_TRAPHOSTIP = "TrapHostIp";
	public static final String JSONKEY_SETTRAPHOSTONSTARTUP = "SetTrapHost";

	protected long mDeviceType;
	protected int mTrapPort;
	protected boolean mSetTrapHostOnStartup;
	protected String mTrapHostIp;
	private String mXMLNeModelGeneratedFilename;

	public void setDeviceType(int type) {
		this.mDeviceType = type;
	}

	public long getDeviceType() {
		return this.mDeviceType;
	}
	public int getTrapPort() {
		return this.mTrapPort;
	}

	public void setTrapPort(int port) {
		this.mTrapPort = port;
	}
	public void setConfigureTrapHostOnStartup(boolean set)
	{this.mSetTrapHostOnStartup=set;}
	public boolean getConfigureTrapHostOnStartup()
	{return this.mSetTrapHostOnStartup;}
	public void setTrapHostIp(String ip)
	{this.mTrapHostIp=ip;}
	public String getTrapHost()
	{return this.mTrapHostIp;}


	public SNMPMediatorConfig(String filename) throws IOException {
		super(filename);
	}

	@Override
	protected void initVars(JSONObject o) {
		super.initVars(o);
		this.mDeviceType = o.getLong(JSONKEY_DEVICETYPE);
		this.mTrapPort = o.getInt(JSONKEY_TRAPPORT);
		this.mXMLNeModelGeneratedFilename = String.format("mediators/%s/%s_nemodel.gen.xml", this.mName, this.mName);
		try {
			this.mAlternativePingPort = o.getInt(JSONKEY_ALTPINGPORT);
		} catch (Exception err) {
			this.mAlternativePingPort = DEFAULT_PINGPORT;
		}
		try {
			this.mSetTrapHostOnStartup = o.getBoolean(JSONKEY_SETTRAPHOSTONSTARTUP);
			this.mTrapHostIp = o.getString(JSONKEY_TRAPHOSTIP);
		} catch (Exception err) {
		}
	}
	@Override
	protected void addJsonVars(List<String> jsonItems) {
		super.addJsonVars(jsonItems);
		jsonItems.add(String.format("\"%s\":%d", JSONKEY_DEVICETYPE, this.mDeviceType));
		jsonItems.add(String.format("\"%s\":%d", JSONKEY_TRAPPORT, this.mTrapPort));
		jsonItems.add(String.format("\"%s\":%d", JSONKEY_ALTPINGPORT, this.mAlternativePingPort));
		jsonItems.add(String.format("\"%s\":%s", JSONKEY_SETTRAPHOSTONSTARTUP, this.mSetTrapHostOnStartup ? "true" : "false"));
		jsonItems.add(String.format("\"%s\":\"%s\"", JSONKEY_TRAPHOSTIP, this.mTrapHostIp ));
	}

	@Override
	public String toString() {
		return "SNMPMediatorConfig [mDeviceType=" + mDeviceType + ", mTrapPort=" + mTrapPort
				+ ", mSetTrapHostOnStartup=" + mSetTrapHostOnStartup + ", mTrapHostIp=" + mTrapHostIp
				+ ", mXMLNeModelGeneratedFilename=" + mXMLNeModelGeneratedFilename + ", mName=" + mName + ", mDeviceIP="
				+ mDeviceIP + ", mNeXMLFilename=" + mNeXMLFilename + ", mIsNetconfConnected=" + mIsNetconfConnected
				+ ", mIsNeConnected=" + mIsNeConnected + ", mFilename=" + mFilename + ", mNetconfPort=" + mNetconfPort
				+ ", mODLConfigs=" + mODLConfigs + ", mLogFilename=" + mLogFilename + ", mDevicePort=" + mDevicePort
				+ ", mWalkfilename=" + mWalkfilename + ", mAlternativePingPort=" + mAlternativePingPort + "]";
	}

	

}
