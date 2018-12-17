package com.technologies.highstreet.mediatorlib.data;

import java.util.HashMap;
import java.util.Map.Entry;

import com.technologies.highstreet.mediatorlib.netconf.server.basetypes.SnmpKeyValuePair;

public class SNMPCache {

	private static class CacheElement
	{
		public long TimeCreated;
		public long TimeSpanKeep;
		public SnmpKeyValuePair Value;

		public CacheElement(SnmpKeyValuePair x,long ts)
		{
			this.Value=x;
			this.TimeSpanKeep=ts;
			this.TimeCreated=System.currentTimeMillis();
		}
		public void ChangeValue(SnmpKeyValuePair x,long ts)
		{
			this.Value=x;
			this.TimeSpanKeep=ts;
			this.TimeCreated=System.currentTimeMillis();
		}
		public boolean isTimedOut(long now)
		{
			return this.TimeCreated+this.TimeSpanKeep>now;
		}
	}

	private long mTimeout;
	private final HashMap<String,CacheElement> mData;  //oid<=>Complete data
	public static final long TIMEOUT_1SECOND = 1000;
	public static final long TIMEOUT_5SECONDS = 5*TIMEOUT_1SECOND;
	public static final long TIMEOUT_10SECONDS = 10*TIMEOUT_1SECOND;
	public static final long TIMEOUT_1MINUTE = 60*TIMEOUT_1SECOND;
	public static final long TIMEOUT_TEMPORARY = 10000; //10 seconds
	public static final long TIMEOUT_STATIC = 60*60*1000; // 1 hour

	public SNMPCache(long timeout)
	{
		this.mTimeout = timeout;
		this.mData=new HashMap<String, SNMPCache.CacheElement>();
	}
	/*
	 * Check for entries to clear if they are timed out
	 */
	public void trigger()
	{
		long now=System.currentTimeMillis();
		for(Entry<String, CacheElement> entry : this.mData.entrySet())
		{
			if(entry.getValue().isTimedOut(now))
				this.mData.remove(entry.getKey());
		}
	}
	public void add(SnmpKeyValuePair x)
	{
		this.add(x,this.mTimeout);
	}
	public void add(SnmpKeyValuePair x,long ts)
	{
		CacheElement e=this.mData.get(x.getOid());
		if(e==null)
			e=new CacheElement(x,ts);
		else
			e.ChangeValue(x,ts);
		this.mData.put(x.getOid(), e);
	}
	public void clear()
	{
		this.mData.clear();
	}
	public SnmpKeyValuePair get(String oid)
	{
		CacheElement x=this.mData.get(oid);
		if(x==null)
			return null;
		if(x.isTimedOut(System.currentTimeMillis()))
		{
			this.mData.remove(oid);
			return null;
		}
		return x.Value;
	}
	public void set(String oid, String value) {
		this.add(new SnmpKeyValuePair(oid, value));
	}
}
