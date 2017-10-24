package com.technologies.highstreet.mediatorserver.files;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.json.JSONObject;

import com.technologies.highstreet.mediatorserver.data.MediatorConfig;
import com.technologies.highstreet.mediatorserver.data.MediatorConfigCollection;
import com.technologies.highstreet.mediatorserver.data.MediatorConfigExtended;
import com.technologies.highstreet.mediatorserver.data.MediatorLogEntry;
import com.technologies.highstreet.mediatorserver.data.MediatorLogEntryCollection;
import com.technologies.highstreet.mediatorserver.data.ReverseLineInputStream;


public class MediatorFiles {

	public static String HOME=MediatorCoreFiles.MEDIATORSPATH();

	private static final String LOCKFILENAMEFORMAT = "%s.lck";
	private static final String LOGFILENAMEFORMAT = "%s.log";
	private static final String CONFIGFILENAMEFORMAT = "%s.config";
	private static final String PIDFILENAMEFORMAT = "%s.pid";
	private static final String RUNFILENAMEFORMAT = "%s.run";
	private static final String STARTSCRIPTFILE = "start.sh";
	private static final String STARTSCRIPTMANUALFILE = "start_manual.sh";
	private static final String STOPSCRIPTFILE = "stop.sh";
	private static final String ODLLOGINXMLFILE = "login.xml";
	private static final String FWSCRIPTFILE_ADD ="firewall_add.sh";
	private static final String FWSCRIPTFILE_REMOVE ="firewall_remove.sh";
	private static final String FWSTATUSFILE = "fwstatus.json";

	private static final String DS = MediatorCoreFiles.DS;

	private static final String JSONKEY_FWSTATUSFILE_FWACTIVE = "fwactive";


	private final String mLockFilename;
	private final String mLogFilename;
	private final String mConfigFilename;
	private final String mStartscriptFilename;
	private final String mStopScriptFilename;
	private final String mPIDFilename;
	private final String mFirewallAddscriptFilename;
	private final String mFirewallRemoveScriptFilename;
	private final String mRunFilename;
	private final String mFirewallStatusFilename;
	private final String mLoginXMLFilename;

	private final String mStartscriptManualFilename;

	public String getConfigFilename()
	{return this.mConfigFilename;}

	private MediatorFiles(String name) throws Exception
	{
		if(!exists(name))
			throw new Exception("mediator instance not found");

		this.mLogFilename = HOME+name+DS+String.format(LOGFILENAMEFORMAT,name);
		this.mConfigFilename = HOME+name+DS+String.format(CONFIGFILENAMEFORMAT,name);
		this.mLockFilename = HOME+name+DS+String.format(LOCKFILENAMEFORMAT, name);
		this.mStartscriptFilename=HOME+name+DS+STARTSCRIPTFILE;
		this.mStartscriptManualFilename=HOME+name+DS+STARTSCRIPTMANUALFILE;
		this.mStopScriptFilename = HOME+name+DS+STOPSCRIPTFILE;
		this.mFirewallAddscriptFilename = HOME+name+DS+FWSCRIPTFILE_ADD;
		this.mFirewallRemoveScriptFilename = HOME+name+DS+FWSCRIPTFILE_REMOVE;
		this.mPIDFilename=HOME+name+DS+String.format(PIDFILENAMEFORMAT, name);
		this.mRunFilename=HOME+name+DS+String.format(RUNFILENAMEFORMAT, name);
		this.mFirewallStatusFilename = HOME+name+DS+FWSTATUSFILE;
		this.mLoginXMLFilename= HOME+name+DS+ODLLOGINXMLFILE;
	}
	private static boolean exists(String name)
	{
		File f=new File(HOME+name+DS);
		return (f.exists() && f.isDirectory());
	}
	public static void Create(String name,MediatorConfig cfg) throws Exception
	{
		MediatorConfig.ValidateName(name);

		if(exists(name))
			throw new Exception("mediator instance already exists");
		//check config if ports are available to all other configs
		if(!MediatorConfig.IsNetconfPortInRange(cfg.getNetconfPort()))
			throw new Exception("netconf port is not in given range");
		if(!MediatorConfig.IsNetconfPortAvailable(cfg.getNetconfPort()))
			throw new Exception("netconf port is already in use");
		if(!MediatorConfig.IsSNMPPortInRange(cfg.getTrapPort()))
			throw new Exception("snmp port is not in given range");
		if(!MediatorConfig.IsSNMPPortAvailable(cfg.getTrapPort()))
			throw new Exception("snmp port is already in use");

		//create folder
		File dir=new File(HOME+name);
		if(!dir.mkdir())
			throw new Exception("failed to create directory "+dir.getAbsolutePath());
		//create files
		MediatorFiles mf =new MediatorFiles(name);
		cfg.checkXMLNePath(HOME,MediatorCoreFiles.NEMODELPATHREL());
		cfg.saveTo(mf.mConfigFilename);
		MediatorStartScript.Create(mf.mStartscriptFilename,mf.mStartscriptManualFilename,mf.mLoginXMLFilename,cfg);
		MediatorStopScript.Create(mf.mStopScriptFilename,cfg);
		FirewallScript.Create(mf.mFirewallAddscriptFilename,mf.mFirewallRemoveScriptFilename,cfg);
	}
	public static void CreateRun(String name,boolean executeStart) throws Exception
	{
		MediatorFiles mf=new MediatorFiles(name);
		File f=new File(mf.mRunFilename);
		if(!f.exists())
			f.createNewFile();
		f=new File(mf.mLockFilename);
		if(f.exists())
			throw new Exception("mediator is locked");
		if(executeStart)
			executeInBackground(mf.mStartscriptFilename);
	}
	private static void executeInBackground(String script) throws IOException {
		ProcessBuilder pb = new ProcessBuilder(script, "&");
		pb.start();

	}

	public static void DeleteRun(String name,boolean executeStop) throws Exception {

		MediatorFiles mf=new MediatorFiles(name);
		File f=new File(mf.mRunFilename);
		if(f.exists())
			if(!f.delete())
				throw new Exception("unable to delete file: "+f.getAbsolutePath());
		if(executeStop)
			executeInBackground(mf.mStopScriptFilename);
	}
	public static void Delete(String name) throws Exception
	{
		if(exists(name))
		{
			try
			{
				moveDirectory(new File(HOME+name),new File(HOME+"."+name));
				//deleteDirectory(new File(HOME+name));
			}
			catch(Exception err)
			{
				throw new Exception("error deleting "+HOME+name);
			}
		}
	}
	private static void moveDirectory(File from, File to) throws IOException {
		Files.move(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

	/*
	private static boolean deleteDirectory(File directory) {
	    if(directory.exists()){
	        File[] files = directory.listFiles();
	        if(null!=files){
	            for(int i=0; i<files.length; i++) {
	                if(files[i].isDirectory()) {
	                    deleteDirectory(files[i]);
	                }
	                else {
	                    files[i].delete();
	                }
	            }
	        }
	    }
	    return(directory.delete());
	}
	*/
	public static MediatorLogEntryCollection GetLog(String name,int limit) throws Exception
	{
		MediatorFiles mf =new MediatorFiles(name);
		MediatorLogEntryCollection c= new MediatorLogEntryCollection();
		File file = new File(mf.mLogFilename);
		if(file.exists())
		{
			//read last n lines
			BufferedReader in = new BufferedReader (new InputStreamReader (new ReverseLineInputStream(file)));

			while(limit-->=0) {
			    String line = in.readLine();
			    c.add(MediatorLogEntry.FromLine(line));
			    if (line == null) {
			        break;
			    }
			}
			in.close();
		}
		return c;
	}
	public static MediatorConfig GetConfig(String name) throws Exception
	{
		MediatorFiles mf=new MediatorFiles(name);
		return new MediatorConfigExtended(mf.mConfigFilename,mf.readPIDFromFile(),mf.lockfileExists(),mf.runfileExists(),mf.readFirewallActiveStatus());

	}
	private boolean readFirewallActiveStatus() {
		File f=new File(this.mFirewallStatusFilename);
		if(!f.exists())
			return false;
		boolean x=false;
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
		    String line = br.readLine();
		    JSONObject o=new JSONObject(line);
		    x=o.getBoolean(JSONKEY_FWSTATUSFILE_FWACTIVE);
		    br.close();
		}
		catch(Exception err)
		{
			x=false;
		}
		return x;
	}

	public static MediatorConfigCollection GetConfigs() throws Exception
	{
		File root=new File(HOME);
		MediatorConfigCollection c=new MediatorConfigCollection();
		if(root.isDirectory())
		{
			File[] inners=root.listFiles();
			for(File f: inners)
			{
				if(f.isDirectory() && !f.getName().startsWith("."))
				{
					MediatorFiles mf=new MediatorFiles(f.getName());
					MediatorConfigExtended cfg=new MediatorConfigExtended(mf.getConfigFilename());
					cfg.setPID(mf.readPIDFromFile());
					cfg.setIsLocked(mf.lockfileExists());
					cfg.setAutorun(mf.runfileExists());
					cfg.setFirewallActive(mf.readFirewallActiveStatus());
					c.add(cfg);
				}
			}
		}
		return c;
	}
	private boolean runfileExists() {
		File f=new File(this.mRunFilename);
		return f.exists();
	}

	private boolean lockfileExists() {
		File f=new File(this.mLockFilename);
		return f.exists();
	}

	private int readPIDFromFile() {
		int x=0;
		File f= new File(this.mPIDFilename);
		if(f.exists())
		{
			try {
				BufferedReader br = new BufferedReader(new FileReader(f));
			    String line = br.readLine();
			    x= Integer.parseInt(line.trim());
			    br.close();
			}
			catch(Exception err)
			{
				x=-1;
			}
		}
		return x;
	}

	public static MediatorConfigCollection GetConfigsFiltered(String nameFilter) throws Exception
	{
		MediatorConfigCollection c=new MediatorConfigCollection();
		c.add(GetConfig(nameFilter));
		return c;
	}
	public static void ClearLock(String name) throws Exception
	{
		MediatorFiles mf=new MediatorFiles(name);
		File f=new File(mf.mLockFilename);
		if(f.exists())
			if(!f.delete())
				throw new Exception("unable to delete file: "+f.getAbsolutePath());
	}


}
