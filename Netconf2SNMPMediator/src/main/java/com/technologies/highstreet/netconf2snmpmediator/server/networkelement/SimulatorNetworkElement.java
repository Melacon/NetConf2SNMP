package com.technologies.highstreet.netconf2snmpmediator.server.networkelement;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.technologies.highstreet.mediatorlib.netconf.server.basetypes.Console;
import com.technologies.highstreet.netconf2snmpmediator.devices.MediatorDevice;
import com.technologies.highstreet.netconf2snmpmediator.devices.MediatorDevice.DeviceInfos;

public class SimulatorNetworkElement extends Netconf2SNMPNetworkElement{

	private static final String VENDOR = "OpenSource";
	private static final String DEVICE = "Simulator";
	private static final String VERSION = "1.0.0";
	private static final String XML = "DVM_MWCore12_BasicAir.xml";
	public static DeviceInfos INFOS;
	public static void registerDevices()
	{
		INFOS = MediatorDevice.register(VENDOR,DEVICE,VERSION,SimulatorNetworkElement.class,XML);
	}
	public SimulatorNetworkElement(String filename, String schemaPath, String uuid, long type,
			String remoteSNMPIp, int remoteSNMPPort, int trapport, Console console,
			IOnPluginEventListener pluginListener, IOnTrapReceivedListener trapListener) throws SAXException,
			IOException, ParserConfigurationException, TransformerConfigurationException, XPathExpressionException {
		super(filename, schemaPath, uuid, type, remoteSNMPIp, remoteSNMPPort, trapport, console, pluginListener, trapListener);
		this.setInitializationAsFinished();
	}

	@Override
	public void addToProblemListNe(String problemName, String problemSeverity, String timeStamp, Object deviceName,
			String valueOf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean removeFromProblemListNe(String problemName) {
		// TODO Auto-generated method stub
		return false;
	}

}