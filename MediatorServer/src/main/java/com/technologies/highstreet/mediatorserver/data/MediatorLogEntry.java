package com.technologies.highstreet.mediatorserver.data;

public class MediatorLogEntry implements IJSONable {

	public static final String JSONKEY_TIMESTAMP ="ts";
	public static final String JSONKEY_LEVEL = "lvl";
	public static final String JSONKEY_MESSAGE = "msg";
	public static final String JSONFORMAT = "{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":\"%s\"}";
	public final String TimeStamp;
	public final String Level;
	public final String Message;

	public MediatorLogEntry(String ts,String lvl,String msg)
	{
		this.TimeStamp=ts;
		this.Level=lvl;
		this.Message=msg;
	}
	@Override
	public String toJSON() {
		return String.format(JSONFORMAT, JSONKEY_TIMESTAMP,this.TimeStamp,JSONKEY_LEVEL,this.Level,JSONKEY_MESSAGE,this.Message);
	}

	public static MediatorLogEntry FromLine(String line) {
		// TODO Auto-generated method stub
		return null;
	}

}
