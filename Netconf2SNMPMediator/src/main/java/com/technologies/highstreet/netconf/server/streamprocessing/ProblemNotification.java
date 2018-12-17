package com.technologies.highstreet.netconf.server.streamprocessing;

import com.technologies.highstreet.netconf2snmpmediator.server.streamProcessing.SNMPDevicePollingThread;

import net.i2cat.netconf.rpc.RPCElement;

public class ProblemNotification extends RPCElement{

	/**
	 *
	 */
	private static final long serialVersionUID = -1511905063731518866L;
	private static int idCounter=0;
	private final String xml;
	
	public ProblemNotification(String problemName,String objRefId,String severity,String timestamp,String counter) {
		this(" <problem-notification xmlns=\"urn:onf:params:xml:ns:yang:microwave-model\">" + 
		"            <problem>"+problemName+"</problem>" + 
		"            <object-id-ref>"+objRefId+"</object-id-ref>" + 
		"            <severity>"+severity+"</severity>" + 
		"			<time-stamp>" + timestamp+"</time-stamp>" +
		"			<counter>"+counter+"</counter>"+
		"        </problem-notification>");
	}
	public ProblemNotification(String xml)
	{
		this.xml=xml;
		this.setMessageId("ProblemNotification_"+((idCounter++)&0x3fffffff));

	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ProblemNotification)
			return ((ProblemNotification)obj).xml.equals(this.xml);
		return super.equals(obj);
	}
	@Override
	public String toXML() {
		return this.xml;
	}

}
