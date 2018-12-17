package com.technologies.highstreet.netconf2snmpmediator.server.networkelement;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.technologies.highstreet.mediatorlib.netconf.server.basetypes.Console;
import com.technologies.highstreet.mediatorlib.netconf.server.basetypes.SnmpKeyValuePairList;
import com.technologies.highstreet.mediatorlib.netconf.server.networkelement.NodeEditConfigCollection;
import com.technologies.highstreet.mediatorlib.netconf.server.types.NetconfTagList;
import com.technologies.highstreet.netconf2snmpmediator.server.Config;

public class Netconf2SNMPConnector extends SNMPConnector  {

	public interface SNMPErrorListener {
		void OnError(final String message);
	}

	private static final Log LOG = LogFactory.getLog(Netconf2SNMPConnector.class);
	//private final BaseSNMPDevice snmpDevice;
	private final Netconf2SNMPNetworkElement sne;
	private SNMPErrorListener errorListener;
	private final Console console;
	private final IOnPluginEventListener pluginListener;
	private final IOnTrapReceivedListener trapListener;


	// Constructor
	public Netconf2SNMPConnector(Netconf2SNMPNetworkElement sne, Console console,IOnPluginEventListener pluginListener, IOnTrapReceivedListener trapListener) throws IOException {

		super(Config.getInstance(),sne.getSNMPTrapPort(),sne.getDeviceIp(),sne.getDevicePort());
		this.sne = sne;
		this.console = console;
		//this.snmpDevice = BaseSNMPDevice.CREATOR.Create(sne.getDeviceClass());
		this.pluginListener = pluginListener;
		this.trapListener = trapListener;
		this.initSNMP();

	}



	/*
	 * ----------------------------------- Functions Get/Set/Console/Listener
	 */

	private void onSNMPError(SNMPErrorListener listener) {
		this.errorListener = listener;
	}

	private void onError(final String message) {
		if (this.errorListener != null) {
			this.errorListener.OnError(message);
		}
	}

	/**
	 * Message to console
	 *
	 * @param msg
	 *            content
	 * @return again the msg
	 */
	protected String consoleMessage(String msg) {
		return console.cliOutput("NE:" + msg);
	}

	/*
	 * --------------------------- Functions Message handling
	 */

	/**
	 * Replace specified OID parameters of actual request within the DOM
	 *
	 * @param messageId
	 *            of the message
	 * @param tags
	 *            of the message
	 */
	public void onPreReplyMessage(String messageId, NetconfTagList tags, NodeEditConfigCollection nodes) {

		LOG.debug("running snmp requests for " + messageId);
		if(this.pluginListener!=null) {
			this.pluginListener.onPreRequest(messageId,tags,nodes);
		}

		if(this.pluginListener!=null) {
			this.pluginListener.onPostRequest(messageId,tags,nodes);
		}

	}



	/**
	 * Do changes in the NE
	 *
	 * @param messageId
	 * @param xmlSourceMessage
	 */
	void onPreEditConfigTarget(String messageId, NetconfTagList tags, Document sourceMessage) {

		// consoleMessage("onPreEditConfigTarget");
		// do a sync snmp set request
		LOG.debug("Start pre edit-config request message " + messageId);
		if(this.pluginListener!=null) {
			this.pluginListener.onPreEditRequest(messageId,tags,sourceMessage);
		}

		if(this.pluginListener!=null) {
			this.pluginListener.onPostEditRequest(messageId,tags,sourceMessage);
		}

	}

	@Override
	protected void onTrapReceived(final SnmpKeyValuePairList traps) {

		LOG.debug("TRAPs received (" + traps+")");
		boolean handled=false;
		if(trapListener!=null) {
			handled=trapListener.onTrapReceived(traps);
		}
		if(!handled)
		{
			LOG.warn("unhandled trap:"+traps);
//			LOG.debug("put trap into message queue");
//			sne.pushToExternalMessageQueue(traps);
		}
	
	}









}
