package com.technologies.highstreet.mediatorlib.netconf.server.types;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Container for all dynamic mediator states
 * @author herbert
 *
 */
public class MediatorStatus {

	public static class ODLConnection
	{
		public final String remoteIp;
		public ODLConnection(String ip)
		{
			this.remoteIp=ip;
		}
		public ODLConnection(JSONObject o) {
			this(o.getString(JSONKEY_CONNECTION_IP));
		}
	}
	public static class ODLConnectionList extends ArrayList<ODLConnection>
	{
		/**
		 *
		 */
		private static final long serialVersionUID = -2213192134852070885L;

		public int indexOf(String ip)
		{
			int idx=-1;
			for(int i=0; i<this.size();i++)
			{
				if(this.get(i).remoteIp.equals(ip))
				{
					idx=i;
					break;
				}
			}
			return idx;
		}
		public boolean contains(String ip)
		{return this.indexOf(ip)>=0;}
		public void remove(String ip)
		{
			int idx=this.indexOf(ip);
			if(idx>=0)
				this.remove(idx);
		}
		public JSONArray toJSON()
		{
			JSONArray a=new JSONArray();
			for(ODLConnection c:this)
			{
				JSONObject o=new JSONObject();
				o.put(JSONKEY_CONNECTION_IP, c.remoteIp);
				a.put(o);
			}
			return a;
		}
	}
	private static final String JSONKEY_CONNECTIONS = "connections";
	private static final String JSONKEY_CONNECTION_IP = "ip";
	private static final String JSONKEY_ISNECONNECTED = "isNeConnected";
	private static final String JSONKEY_ISNCCONNECTED = "isNcConnected";
	private static final String JSONKEY_PID = "pid";
	private static final String JSONKEY_STARTED = "started";


	private final ODLConnectionList connections;
	public void addConnection(String ip)
	{
		if(!this.connections.contains(ip))
			this.connections.add(new ODLConnection(ip));
	}
	public void addConnection(ODLConnection c)
	{
		if(!this.connections.contains(c))
			this.connections.add(c);
	}
	public void removeConnection(String ip)
	{
		this.connections.remove(ip);
	}
	private boolean isNeConnected;
	private boolean isNcConnected;
	private long pid;
	private String started;
	public MediatorStatus()
	{
		this.connections=new ODLConnectionList();
	}
	public String toJSON()
	{
		JSONObject o=new JSONObject();
		o.put(JSONKEY_ISNCCONNECTED, this.isNcConnected);
		o.put(JSONKEY_ISNECONNECTED, this.isNeConnected);
		o.put(JSONKEY_PID, this.pid);
		o.put(JSONKEY_CONNECTIONS, this.connections.toJSON());
		o.put(JSONKEY_STARTED, this.started);
		return o.toString();
	}
	public static MediatorStatus FromJSON(String json) {
		JSONObject o=new JSONObject(json);
		MediatorStatus obj=new MediatorStatus();
		JSONArray a = o.getJSONArray(JSONKEY_CONNECTIONS);
		for(int i=0;i<a.length();i++)
		{
			obj.addConnection(new ODLConnection(a.getJSONObject(i)));
		}
		obj.isNcConnected = o.getBoolean(JSONKEY_ISNCCONNECTED);
		obj.isNeConnected=o.getBoolean(JSONKEY_ISNECONNECTED);
		obj.pid = o.getLong(JSONKEY_PID);
		obj.started = o.optString(JSONKEY_STARTED,NetconfTimeStamp.getTimeStamp());
		return obj;
	}
	public void setStartTime() {
		this.setStartTime(NetconfTimeStamp.getTimeStamp());
	}
	public void setStartTime(String time) {
		this.started=time;
	}
	public String getStartTime() {
		return this.started;
	}
	public void setPID(long pid) {
		this.pid=pid;
	}
	public void setIsNeConnected(boolean b) {
		this.isNeConnected=b;
	}
	public ODLConnectionList getConnections() {
		return this.connections;
	}
	public void setIsNcConnected(boolean b) {
		this.isNcConnected=b;
	}
	public long getPID() {
		return this.pid;
	}
	public boolean getIsNetworkElementConnected() {
		return this.isNeConnected;
	}
}
