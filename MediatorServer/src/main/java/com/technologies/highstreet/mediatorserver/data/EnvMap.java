package com.technologies.highstreet.mediatorserver.data;

import java.util.Map;

public class EnvMap {
	
	
	private static EnvMap mObj;
	
	private Map<String,String> _map;
	private EnvMap()
	{
		this._map = System.getenv();			   
	}
	public String get(final String key)
	{
		   
		for (String envName : this._map.keySet()) {
			    if(envName.equals(key))
			    	return this._map.get(envName);		    
		}
		return null;
	}
	public static EnvMap GetInstance()
	{
		if(mObj==null)
			mObj=new EnvMap();
		return mObj;
	}
}