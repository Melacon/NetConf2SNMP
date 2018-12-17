package com.technologies.highstreet.mediatorserver.data;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

public class MediatorLogEntry implements IJSONable {

	public static final String JSONKEY_TIMESTAMP ="ts";
	public static final String JSONKEY_LEVEL = "lvl";
	public static final String JSONKEY_SOURCE = "src";
	public static final String JSONKEY_MESSAGE = "msg";
	public static final String JSONFORMAT = "{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":\"%s\"}";
	public final String TimeStamp;
	public final String Level;
	public final String Source;
	public String Message;
	private static final String regex = "^([0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}[,0-9]{0,4}) ([A-Z]+) (\\[.+\\]) (.+)$";
	private static final Pattern pattern = Pattern.compile(regex);
	private static final String SEPERATOR = "| ";
	public MediatorLogEntry(String ts,String lvl,String src,String msg)
	{
		this.TimeStamp=ts;
		this.Level=lvl;
		this.Source=src;
		this.Message=msg;
	}
	@Override
	public String toJSON() {
		JSONObject o=new JSONObject();
		o.put(JSONKEY_LEVEL, this.Level);
		o.put(JSONKEY_SOURCE, this.Source);
		o.put(JSONKEY_TIMESTAMP, this.TimeStamp);
		o.put(JSONKEY_MESSAGE, this.Message);
		return o.toString();
	}

	public static MediatorLogEntry FromLine(String line) {
		final Matcher matcher = pattern.matcher(line);
		if(matcher.find())
		{
			return new MediatorLogEntry(matcher.group(1), matcher.group(2), matcher.group(3),matcher.group(4));
		}
		return null;
	}
	public void addLines(List<String> linesToAdd, boolean forward) {
		if(linesToAdd!=null && linesToAdd.size()>0)
		{
			if(forward)
			{
				for(String s:linesToAdd)
					this.Message+=SEPERATOR+s;
			}
			else
			{
				for(int i=linesToAdd.size()-1;i>=0;i--)
					this.Message+=SEPERATOR+linesToAdd.get(i);
			}
		}
	}

}
