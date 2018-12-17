package com.technologies.highstreet.netconf2snmpmediator.server.streamProcessing;

public interface MediatorConnectionListener {

	public void netconfOnConnect();
	public void netconfOnDisconnect();
	public void networkElementOnConnect();
	public void networkElementOnDisconnect();

}
