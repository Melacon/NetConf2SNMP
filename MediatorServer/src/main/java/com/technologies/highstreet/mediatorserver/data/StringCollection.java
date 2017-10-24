package com.technologies.highstreet.mediatorserver.data;

import java.util.ArrayList;

public class StringCollection extends ArrayList<String>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1279870087907066727L;

	public String toJSON()
	{
		if(this.size()<=0)
			return "[]";
		StringBuilder sb=new StringBuilder();
		sb.append("[");
		if(this.size()>0);
			sb.append("\""+this.get(0)+"\"");
		for(int i=1;i<this.size();i++)
			sb.append(",\""+this.get(i)+"\"");
		sb.append("]");
		return sb.toString();
	}
}
