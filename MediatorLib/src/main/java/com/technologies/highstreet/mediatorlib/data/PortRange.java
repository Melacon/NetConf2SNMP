package com.technologies.highstreet.mediatorlib.data;

import java.util.ArrayList;
import java.util.List;

public class PortRange {

	private static class MyIntList extends ArrayList<Integer>
	{

		/**
		 *
		 */
		private static final long serialVersionUID = -4885045737602761640L;
		//no need at all. works with primitives
	/*	public boolean contains(int p)
		{
			for(Integer i:this)
			{
				if((int)i==p)
					return true;
			}
			return false;
		}*/
	}
	private final int MinValue;
	private final int MaxValue;

	private final MyIntList mExceptions;

	public PortRange(int min,int max)
	{
		this.MinValue=min;
		this.MaxValue=max;
		this.mExceptions=new MyIntList();
	}
	public void AddException(int e)
	{
		if(!this.mExceptions.contains(e))
			this.mExceptions.add(e);
	}
	public boolean IsAvailable(int p)
	{
		if(p<this.MinValue || p>this.MaxValue)
			return false;
		return !this.mExceptions.contains(p);
	}
	public boolean IsInRange(int p)
	{
		if(p<this.MinValue || p>this.MaxValue)
			return false;
		return true;
	}
	public int[] getFree(int maxSize)
	{
		List<Integer> l=new ArrayList<Integer>();
		int i;
		for(i=this.MinValue;i<=this.MaxValue;i++)
		{
			if(!this.mExceptions.contains(i))
			{
				l.add(i);
				if(l.size()>=maxSize)
					break;
			}
		}
		//Copy to array
		int[] a=new int[l.size()];
		i=0;
		for(Integer x:l)
			a[i++]=x;
		return a;
	}
	public String toJSON() {

		return String.format("[%d,%d]", this.MinValue,this.MaxValue);
	}
}
