package com.technologies.highstreet.mediatorserver.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MediatorLogFile {

	private final BufferedReader mFilestreamReader;

	public MediatorLogFile(String filename) throws FileNotFoundException
	{
		this.mFilestreamReader= new BufferedReader (new InputStreamReader (new ReverseLineInputStream(new File(filename))));
	}
	public MediatorLogEntryCollection GetLast(int lines) throws IOException
	{
		MediatorLogEntryCollection c=new MediatorLogEntryCollection();
		MediatorLogEntry e=null;
		List<String> linesToAdd = new ArrayList<String>();
		while(lines-->0) {
		    String line = this.mFilestreamReader.readLine();
		    if (line == null) {
		        break;
		    }
		    e=MediatorLogEntry.FromLine(line);
		    if(e!=null)
		    {
		    	e.addLines(linesToAdd,false);
		    	linesToAdd.clear();
		    	c.add(e);
		    }
		    else
		    	linesToAdd.add(line);
		}
		return c;
	}

}
