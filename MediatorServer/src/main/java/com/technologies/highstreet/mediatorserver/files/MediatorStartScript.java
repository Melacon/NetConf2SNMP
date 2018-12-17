package com.technologies.highstreet.mediatorserver.files;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.technologies.highstreet.mediatorserver.data.ServerSideMediatorConfig;


public class MediatorStartScript {

	public static final String LR = MediatorCoreFiles.LR;

	private static String odlLoginScript(ServerSideMediatorConfig cfg)
	{
		String s="<node xmlns=\"urn:TBD:params:xml:ns:yang:network-topology\">"+
				"<node-id>"+cfg.getName()+"</node-id>"+
				"<host xmlns=\"urn:opendaylight:netconf-node-topology\">"+ServerSideMediatorConfig.GetHostIP()+"</host>"+
				"<port xmlns=\"urn:opendaylight:netconf-node-topology\">"+cfg.getNetconfPort()+"</port>"+
				"<username xmlns=\"urn:opendaylight:netconf-node-topology\">"+cfg.getFirstODLConfig().User+"</username>"+
				"<password xmlns=\"urn:opendaylight:netconf-node-topology\">"+cfg.getFirstODLConfig().Password+"</password>"+
				"<tcp-only xmlns=\"urn:opendaylight:netconf-node-topology\">false</tcp-only>"+
				"<keepalive-delay xmlns=\"urn:opendaylight:netconf-node-topology\">120</keepalive-delay>"+
				"</node>";
		return s;
	}
	private static String startScriptContent(ServerSideMediatorConfig cfg)
	{
		return startScriptContent(cfg,false);
	}
	private static String startScriptContent(ServerSideMediatorConfig cfg, boolean cli)
	{

		return "#!/bin/bash"+LR+LR+
				"MED_HOME=\"mediators/"+cfg.getName()+"/\""+LR+
				"CONFIGFILE=$MED_HOME\""+cfg.getName()+".config\""+LR+
				"SERVERCONFIGFILE=\"/etc/mediatorserver.conf\""+LR+
				"DBGFILE=$MED_HOME\"debug.conf\"" + LR +
				"LOGINFILE=$MED_HOME\"login.xml\""+LR+
				"LCKFILE=$MED_HOME\""+cfg.getName()+".lck\""+LR+
				"PIDFILE=$MED_HOME\""+cfg.getName()+".pid\""+LR+
				"if [ -e $SERVERCONFIGFILE ]; then" + LR +
				"  source $SERVERCONFIGFILE" + LR +
				"  JAVA_MEMCONFIG=$MediatorMemory" + LR+
				"  if [ -e $DBGFILE ]; then" + LR+
				"    source $DBGFILE" + LR+
				"  else" + LR +
				"    JAVA_JMXCONFIG=\"\"" + LR+
				"  fi" + LR +
				"else" + LR +
				"  JAVA_MEMCONFIG=\"-Xmx256m -Xms128m\"" + LR +
				"  JAVA_JMXCONFIG=\"\"" + LR +
				"fi" + LR +
				""+LR+
				"#check if lock file exists"+LR+
				"if [ -e $LCKFILE ]; then"+LR+
				"  echo \"process is still locked\""+LR+
				"  exit 1"+LR+
				"fi"+LR+
				LR+
				"#create lock file"+LR+
				"touch $LCKFILE"+LR+
				"#send odl register request"+LR+
				"#program call"+LR+
				"java $JAVA_JMXCONFIG $JAVA_MEMCONFIG -jar bin/Netconf2SNMPMediator.jar "+(cli?" --cli":"")+" $MED_HOME\""+cfg.getName()+".config\" yang/yangNeModel"+LR+
				"#remove Mediator from ODL"+LR+
				"#remove lock file"+LR+
				"if [ -e $LCKFILE ]; then"+LR+
				"  rm $LCKFILE"+LR+
				"fi"+LR+
				"#remove pid file"+LR+
				"if [ -e $PIDFILE ]; then"+LR+
				"  rm $PIDFILE"+LR+
				"fi"+LR+
				"exit 0"+LR+
				"";
	}
	public static void Create(String filename,String filenameManual,String filenameLoginXML,ServerSideMediatorConfig cfg) throws IOException {


		File file=new File(filename);
		//write start.sh
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.write(startScriptContent(cfg));
		bw.flush();
		bw.close();
		file.setExecutable(true);
		File file2=new File(filenameManual);
		//write start.sh
		bw = new BufferedWriter(new FileWriter(file2));
		bw.write(startScriptContent(cfg,true));
		bw.flush();
		bw.close();
		file2.setExecutable(true);
		//write login.xml
		bw = new BufferedWriter(new FileWriter(filenameLoginXML));
		bw.write(odlLoginScript(cfg));
		bw.flush();
		bw.close();


	}

}
