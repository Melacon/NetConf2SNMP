package com.technologies.highstreet.mediatorlib.data;

import java.util.HashMap;

public class DataRow extends HashMap<String,Object>{
	/**
	 *
	 */
	private static final long serialVersionUID = -4503230493466281384L;
	private static final Object EMPTY = "";
	private final String key;
	public String getKey() {return this.key;}
	private boolean autoCreate;
	public DataRow()
	{
		this((String)null);
	}
	public DataRow(String k,Object[] values) {
		this(k,values,true);
	}

	public DataRow(String k,Object[] values,boolean autocreate) {
		this.autoCreate = autocreate;
		this.key=k;
		if(values!=null)
		{
			int i=0;
			for(Object o:values)
				this.put(String.format("%d",i++),o);
		}
	}
	public DataRow(String k) {
		this(k,null);
	}
	private String getKeyByInt(int i)
	{return String.valueOf(i);}

	public Object getValueAt(int col) {
		if (col < this.size())
			return this.get(String.valueOf(col));
		return null;
	}
	public boolean setValue(String colKey, Object value) {
		this.put(colKey, value);
		return true;
	}
	public boolean setValueAt(int col, Object value) {
		if(this.autoCreate)
		{
			this.put(getKeyByInt(col),null);
		}
		if(this.containsKey(getKeyByInt(col)))
		{
			return this.setValue(getKeyByInt(col), value);
		}
		return false;
	}
	public String toString(String[] cols,String seperator) {
		String s="";
		if(cols.length>0)
			s=this.getString(cols[0]);
		for(int i=1;i<cols.length;i++)
			s+=seperator+this.getString(cols[i]);
		return s;
	}
	private String getString(String key) {
		if(this.containsKey(key))
			return this.get(key).toString();
		return "null";
	}
}
