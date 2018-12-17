package com.technologies.highstreet.examplemediatorplugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.technologies.highstreet.mediatorlib.netconf.server.networkelement.NetworkElement;
import com.technologies.highstreet.mediatorlib.netconf.server.types.MediatorConfig;
import com.technologies.highstreet.mediatorlib.netconf.server.types.NetconfTagList;
import com.technologies.highstreet.mediatorlib.plugin.AbstractMediatorPlugin;

public class Plugin extends AbstractMediatorPlugin{

	private static final Log LOG = LogFactory.getLog(NetworkElement.class);
	private static final String VERSION_STRING = "1.0.0";

	public Plugin(NetworkElement ne,MediatorConfig cfg) {
		super(ne,cfg);
		LOG.debug("instantiated");
	}

	public void onPreInit() {
		LOG.debug("onPreInit");
	}

	public void onPostInit() {
		LOG.debug("onPostInit");
	}

	public void onPreRequest(String messageId, NetconfTagList tags) {
		LOG.debug("onPreRequest");
		//this.networkElement...
	}

	public void onPostRequest(String messageId, NetconfTagList tags) {
		LOG.debug("onPostRequest");
	}

	public void onPreEditRequest(String messageId, NetconfTagList tags, Document sourceMessage) {
		LOG.debug("onPreEditRequest");
		//this.networkElement...
	}

	public void onPostEditRequest(String messageId, NetconfTagList tags, Document sourceMessage) {
		LOG.debug("onPostEditRequest");


	}

	public void onClose() {
		LOG.debug("onClose");

	}

	public void onDeviceConnectionStatusChanged(int before, int now) {
		LOG.debug("onConnectionStatusChanged");

	}

	public String getVersion() {
		return VERSION_STRING;
	}





}
