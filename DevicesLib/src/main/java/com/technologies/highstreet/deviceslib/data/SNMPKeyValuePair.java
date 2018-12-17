package com.technologies.highstreet.deviceslib.data;

public class SNMPKeyValuePair {

	public static final int VALUETYPE_STRING = 1;
	public static final int VALUETYPE_INT = 2;
	public static final int VALUETYPE_INETADR = 3;
	public static final int VALUETYPE_INETADR_HEXSTRING = 4;
	public static final int VALUETYPE_INT32 = 5;
	public static final int VALUETYPE_COUNTER32 = 6;
	public static final int VALUETYPE_GAUGE32 = 7;
	public static final int VALUETYPE_TICKS = 8;
	public static final int VALUETYPE_OID = 9;


	public final String OID;
	public final String Value;
	public final int ValueType;

	public SNMPKeyValuePair(String oid,String value)
	{
		this(oid,value,VALUETYPE_STRING);
	}
	public SNMPKeyValuePair(String oid,long value)
	{
		this(oid,String.format("%d", value),VALUETYPE_INT);
	}
	public SNMPKeyValuePair(String oid,String value,int valueType)
	{this.OID=oid;this.Value=value;this.ValueType=valueType;

	}
}
