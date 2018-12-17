package com.technologies.highstreet.deviceslib.data;

public enum SNMPDeviceType {
	SIMULATOR;
	
	private static final SNMPDeviceType[] map=new SNMPDeviceType[]{
			SIMULATOR,					//0
		};
	private static SNMPDeviceType getFromIndex(int idx,SNMPDeviceType def)
	{
		if(idx<map.length)
			return map[idx];
		return def;
	}
	private static int indexOf(SNMPDeviceType t,int def)
	{
		for(int i=0,l=map.length;i<l;i++)
		{
			if(map[i]==t)
				return i;
		}
		return def;
	}
	public static SNMPDeviceType FromInt(int x) {
		return getFromIndex(x,SIMULATOR);
	}
	public int intValue()
	{
		return indexOf(this,0);
	}
}
