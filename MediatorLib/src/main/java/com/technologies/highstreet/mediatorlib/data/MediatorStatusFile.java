package com.technologies.highstreet.mediatorlib.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.technologies.highstreet.mediatorlib.netconf.server.types.MediatorStatus;
import com.technologies.highstreet.mediatorlib.netconf.server.types.MediatorStatus.ODLConnection;

/**
 * StatusFile for Mediator to read and write MediatorStatus class
 * @author herbert
 *
 */
public class MediatorStatusFile {

	private static final Log LOG = LogFactory.getLog(MediatorStatusFile.class);
	private final String filename;
	private final MediatorStatus status;
	private boolean AUTOSAVE = true;
	private static String _getFileContent(final String fn)
	{
		String s="";
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fn));
		} catch (FileNotFoundException e1) {
		}
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			s= sb.toString();
		}
		catch(Exception e)
		{
			LOG.warn(e.getMessage());
		}
		finally
		{
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return s;
	}
	private static void _setFileContent(final String fn, final String content) {
		try {
			FileWriter fw = new FileWriter(fn);
			BufferedWriter bw=new BufferedWriter(fw);
			bw.write(content);
			bw.flush();
			fw.close();
			bw.close();
		} catch (IOException e) {
			LOG.warn("problem saving file +"+fn+": "+e.getMessage());
		}
	}
	public MediatorStatusFile(String filename)
	{
		this.filename=filename;
		File f=new File(filename);
		MediatorStatus tmpStatus = null;
		if(f.exists())
		{
			try
			{
				tmpStatus=MediatorStatus.FromJSON(_getFileContent(filename));
			}
			catch(Exception e)
			{
				LOG.warn("problem loading status file: " + e.getMessage());
			}

		}
		if(tmpStatus==null)
			tmpStatus=new MediatorStatus();

		this.status=tmpStatus;
	}
	public void save()
	{
		_setFileContent(this.filename,this.status.toJSON());
	}

	public void setPID(String pid) {

		this.setPID(Long.parseLong(pid));
	}
	public void setPID(long pid) {

		this.status.setPID(pid);
		if(AUTOSAVE)
			save();
	}
	public void setIsNeConnected(boolean b) {
		this.status.setIsNeConnected(b);
		if(AUTOSAVE)
			save();
	}
	public void addConnection(String remoteIp) {
		this.status.addConnection(remoteIp);
		this.status.setIsNcConnected(this.status.getConnections().size()>0);
		if(AUTOSAVE)
			save();
	}
	public void removeConnection(String remoteIp) {
		this.status.removeConnection(remoteIp);
		this.status.setIsNcConnected(this.status.getConnections().size()>0);
		if(AUTOSAVE)
			save();
	}
	public void resetNetconfConnections() {
		this.status.getConnections().clear();
		this.status.setIsNcConnected(this.status.getConnections().size()>0);
		if(AUTOSAVE)
			save();
	}
	public String getFilename() {
		return this.filename;
	}
	public MediatorStatus getStatus() {
		return this.status;
	}
	public void clrPID() {
		this.setPID(0);
	}
	public void clear()
	{
		this.status.getConnections().clear();
		this.status.setIsNcConnected(false);
		this.status.setIsNeConnected(false);
		this.clrPID();
	}
	public void setStarted() {
		this.status.setStartTime();
		if(AUTOSAVE)
			save();
	}
	public int getConnectionCount() {
		return this.status.getConnections().size();
	}
	public int getConnectionCount(String remoteIp) {
		int c=0;
		for(ODLConnection conn: this.status.getConnections())
		{
			if(remoteIp.equals(conn.remoteIp))
				c++;
		}
		return c;
	}
}
