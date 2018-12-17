package com.technologies.highstreet.netconf.server.streamprocessing;

import net.i2cat.netconf.rpc.RPCElement;

public class AttributeValueChangedNotification extends RPCElement{

	/**
	 *
	 */
	private static final long serialVersionUID = -1511905063731518866L;
	private static int idCounter=0;
	private final String xml;

	public AttributeValueChangedNotification(String attrName,String objRefId,String newValue)
	{
		this("<attribute-value-changed-notification xmlns=\"urn:onf:params:xml:ns:yang:microwave-model\">" + 
				"<attribute-name>"+attrName+"</attribute-name>" + 
				"<object-id-ref>"+objRefId+"</object-id-ref>" + 
				"<new-value>"+newValue+"</new-value>" + 
				"</attribute-value-changed-notification>");
	}
	public AttributeValueChangedNotification(String xml)
	{
		this.xml=xml;
		this.setMessageId("AttributeValueChangedNotification"+(idCounter++));

	}
	@Override
	public String toXML() {
		return this.xml;
	}

}
