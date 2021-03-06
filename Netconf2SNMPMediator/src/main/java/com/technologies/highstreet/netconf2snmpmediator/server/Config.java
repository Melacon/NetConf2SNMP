package com.technologies.highstreet.netconf2snmpmediator.server;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.mp.SnmpConstants;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.technologies.highstreet.netconf2snmpmediator.server.networkelement.SimulatorNetworkElement;

public class Config {

	private static final Logger LOG = LoggerFactory.getLogger(Config.class);

	public static final int DEFAULT_LATENCY = 1500;
    public static final int SNMPPING_INTERVAL = 30*1000;	//30 seconds
    public int DEVICEPING_TIMEOUT = 2000;

    /* basic snmp settings */
    public int SNMPRequestLatency = DEFAULT_LATENCY;  //in ms
    public final int SNMPRequestRetries = 1;
    public int SNMPVersion = SnmpConstants.version2c;
    public long SNMPDeviceClass = SimulatorNetworkElement.INFOS.getId();

    /* Mediator Settings */
    public int MediatorTrapsPort = 10162;
    public String MediatorIp="";
    public int MediatorDefaultNetworkInterfaceNum=0;
    public String MediatorDeviceIp="";
	private boolean sendConnectionStateContinously=false;
	private boolean updateProblemTimestamps = true;

    /* other settings */
    public Level LogLevel = Level.ALL;

	private static final String GLOBALCONFIG_KEY_LOGLEVEL = "MediatorLogLevel";
	private static final String GLOBALCONFIG_KEY_DEVICEPING_TIMEOUT = "MediatorDevicePingTimeout";
	private static final String GLOBALCONFIG_KEY_SNMPLATENCY = "MediatorSnmpLatency";

    private static Config mObj;
    public static Config getInstance(){
    	if(mObj==null)
    		mObj=new Config();
    	return mObj;
    }

    public boolean tryLoad(String filename)
    {
    	if(filename==null || filename.isEmpty())
    		return false;
    	File f=new File(filename);
    	if(!f.exists())
    		return false;
    	Properties properties=null;
    	 try {
             properties = new Properties();
             FileInputStream fileInput = new FileInputStream(f);
             properties.load(fileInput);
             fileInput.close();
    	 }
    	 catch(Exception e)
    	 {
    		 LOG.warn("unable to load global config: "+e.getMessage());
    	 }
    	 if(properties==null)
    		 return false;

    	//LogLevel
    	 try {this.LogLevel=Level.toLevel(properties.getProperty(GLOBALCONFIG_KEY_LOGLEVEL,this.LogLevel.toString()));}catch(Exception err) {}
    	//pingTimeout
    	 try {this.DEVICEPING_TIMEOUT=Integer.parseInt(properties.getProperty(GLOBALCONFIG_KEY_DEVICEPING_TIMEOUT,String.format("%d",this.DEVICEPING_TIMEOUT)));}catch(Exception err) {}
    	//snmpLatency
    	 try {this.SNMPRequestLatency = Integer.parseInt(properties.getProperty(GLOBALCONFIG_KEY_SNMPLATENCY,String.format("%d",this.SNMPRequestLatency)));}catch(Exception err) {}

    	return true;
    }

    /**
     * get all child nodes with type ELEMENT_NODE back in a list
     * @param node parent node
     * @return List with child nodes
     */
    private static List<Node> getChildElementNodes(Node node) {
        List<Node> res = new ArrayList<Node>();
        NodeList childs = node.getChildNodes();
        Node item;
        //System.out.println("Query node "+node.getNodeName());
        for (int n=0; n < childs.getLength(); n++) {
            item = childs.item(n);
            //System.out.println(node.getNodeName()+"-"+item.getNodeName()+" "+item.getNodeType());
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                res.add(childs.item(n));
            }
        }
        return res;
    }
    protected static String getNodeAttribute(Node node, String attribute) {

        for (Node nodeChild : getChildElementNodes(node)) {

            if (nodeChild.getNodeName().equals(attribute)) {
                return nodeChild.getTextContent();
            }

        }
        return "";
    }

	public boolean sendConnectionStateContinously() {
		return this.sendConnectionStateContinously;
	}

	public boolean updateProblemTimestamps() {
		return updateProblemTimestamps;
	}

	public static Config TestConfig() {
		Config x=getInstance();
		x.LogLevel = Level.DEBUG;
		x.DEVICEPING_TIMEOUT = SNMPPING_INTERVAL;
		x.SNMPRequestLatency = DEFAULT_LATENCY;
		return x;
	}
}
