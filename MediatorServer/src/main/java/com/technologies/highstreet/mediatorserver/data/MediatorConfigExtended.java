package com.technologies.highstreet.mediatorserver.data;

import java.io.IOException;

public class MediatorConfigExtended extends MediatorConfig{

	public static final String JSONKEY_PID = "pid";
	public static final String JSONKEY_ISLOCKED = "islocked";
	public static final String JSONKEY_AUTORUN = "autorun";
	public static final String JSONKEY_FWSTATUS = "fwactive";

	private boolean mIsLocked;
	private int mPID;
	private boolean mAutorun;
	private boolean mFirewallActive;

	public MediatorConfigExtended(String filename) throws IOException {
		super(filename);
	}
	public MediatorConfigExtended(String filename,int pid,boolean locked,boolean auto,boolean fwactive) throws IOException {
		this(filename);
		this.mPID=pid;
		this.mIsLocked=locked;
		this.mAutorun=auto;
		this.mFirewallActive=fwactive;
		this.setPID(pid);
	}
	public void setPID(int pid)
	{
		this.mPID=pid;
		if(this.mPID<=0)
		{
			this.mIsNetconfConnected=false;
			this.mIsNetworkElementConnected=false;
		}
	}
	public int getPID(){return this.mPID;}
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
}
