package com.technologies.highstreet.mediatorserver.data;

import java.io.IOException;
import java.util.List;

import com.technologies.highstreet.mediatorlib.data.MediatorStatusFile;
import com.technologies.highstreet.mediatorlib.netconf.server.types.MediatorStatus;
import com.technologies.highstreet.mediatorlib.netconf.server.types.MediatorStatus.ODLConnectionList;

public class ServerSideMediatorConfigExtended extends ServerSideMediatorConfig{

	public static final String JSONKEY_PID = "pid";
	public static final String JSONKEY_ISLOCKED = "islocked";
	public static final String JSONKEY_AUTORUN = "autorun";
	public static final String JSONKEY_FWSTATUS = "fwactive";
	public static final String JSONKEY_NCCONNECTIONS = "ncconnections";

	private boolean mIsLocked;
	private long mPID;
	private boolean mAutorun;
	private boolean mFirewallActive;
	private ODLConnectionList mNcConnections;
	
	public ServerSideMediatorConfigExtended(String filename) throws IOException {
		super(filename);
	}
	public ServerSideMediatorConfigExtended(String filename,String statusFilename,int pid,boolean locked,boolean auto,boolean fwactive) throws IOException {
		this(filename);
		this.mPID=pid;
		this.mIsLocked=locked;
		this.mAutorun=auto;
		this.mFirewallActive=fwactive;
		this.setPID(pid);
		if(statusFilename!=null)
		{
			MediatorStatusFile statusFile=new MediatorStatusFile(statusFilename);
			MediatorStatus status=statusFile.getStatus();
			if(status!=null)
			{
				this.setPID(status.getPID());
				this.mNcConnections=status.getConnections();
				this.setIsNetconfConnected(this.mNcConnections!=null && this.mNcConnections.size()>0);
				this.setIsNeConnected(status.getIsNetworkElementConnected());
			}
		}
	}
	public void setPID(long l)
	{
		this.mPID=l;
		if(this.mPID<=0)
		{
			this.mIsNetconfConnected=false;
			this.mIsNetworkElementConnected=false;
		}
	}
	public long getPID(){return this.mPID;}
	public void setIsLocked(boolean locked)
	{this.mIsLocked=locked;}
	public boolean IsLocked()
	{return this.mIsLocked;}
	public void setAutorun(boolean auto)
	{this.mAutorun=auto;}
	public boolean IsAutorun()
	{return this.mAutorun;}
	public void setFirewallActive(boolean active)
	{this.mFirewallActive=active;}
	public boolean IsFirewallActive()
	{return this.mFirewallActive;}

	@Override
	protected void addJsonVars(List<String> jsonItems) {
		super.addJsonVars(jsonItems);
		jsonItems.add(String.format("\"%s\":%d", JSONKEY_PID, this.mPID));
		jsonItems.add(String.format("\"%s\":%s", JSONKEY_ISLOCKED, this.mIsLocked ? "true" : "false"));
		jsonItems.add(String.format("\"%s\":%s", JSONKEY_AUTORUN, this.mAutorun ? "true" : "false"));
		jsonItems.add(String.format("\"%s\":%s", JSONKEY_FWSTATUS, this.mFirewallActive ? "true" : "false"));
		jsonItems.add(String.format("\"%s\":%s", JSONKEY_NCCONNECTIONS,this.mNcConnections==null?"[]":this.mNcConnections.toJSON().toString()));
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
		sb.append(String.format("\"%s\":%s,",JSONKEY_ODLCONFIG,this.mODLConfigs.toJSON()));
		sb.append(String.format("\"%s\":%s,",JSONKEY_ISNCCONNECTED,this.mIsNetconfConnected?"true":"false"));
		sb.append(String.format("\"%s\":%s,",JSONKEY_ISNECONNECTED,this.mIsNetworkElementConnected?"true":"false"));
		sb.append(String.format("\"%s\":%d,",JSONKEY_PID,this.mPID));
		sb.append(String.format("\"%s\":%s,",JSONKEY_ISLOCKED,this.mIsLocked?"true":"false"));
		sb.append(String.format("\"%s\":%s,",JSONKEY_AUTORUN,this.mAutorun?"true":"false"));
		sb.append(String.format("\"%s\":%s",JSONKEY_FWSTATUS,this.mFirewallActive?"true":"false"));
		sb.append("}");
		return sb.toString();
	}
	*/
}
