package com.technologies.highstreet.mediatorserver.data;

import java.util.ArrayList;

public class JSONableCollection<T extends IJSONable> extends ArrayList<T> implements IJSONable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7688408810249426043L;

	@Override
	public String toJSON()
	{
		if(this.size()<=0)
			return "[]";
		StringBuilder sb=new StringBuilder();
		sb.append("[");
		if(this.size()>0)
			sb.append(this.get(0).toJSON());
		for(int i=1;i<this.size();i++)
			sb.append(","+this.get(i).toJSON()+"");
		sb.append("]");
		return sb.toString();
	}

}
