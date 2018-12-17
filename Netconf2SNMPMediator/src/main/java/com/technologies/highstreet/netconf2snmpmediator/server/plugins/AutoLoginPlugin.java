package com.technologies.highstreet.netconf2snmpmediator.server.plugins;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.technologies.highstreet.mediatorlib.netconf.server.networkelement.NetworkElement;
import com.technologies.highstreet.mediatorlib.netconf.server.types.MediatorConfig;
import com.technologies.highstreet.mediatorlib.netconf.server.types.MediatorConfig.ODLConfig;
import com.technologies.highstreet.mediatorlib.netconf.server.types.MediatorConfig.ODLConfigCollection;
import com.technologies.highstreet.mediatorlib.netconf.server.types.NetconfTagList;
import com.technologies.highstreet.mediatorlib.plugin.AbstractMediatorPlugin;
import com.technologies.highstreet.netconf.server.basetypes.BaseHTTPClient;
import com.technologies.highstreet.netconf.server.basetypes.BaseHTTPResponse;

/**
 *
 * @author Micha
 *
 * example ODLConfig = {"Server":"192.168.178.104","Port":8181,"User":"admin","Password":"admin"}
 */
public class AutoLoginPlugin extends AbstractMediatorPlugin{

	private static final Log LOG = LogFactory.getLog(AutoLoginPlugin.class);
	private static final String VERSION_STRING = "1.0.0";
	private static final int LOGIN_INIT_TIMEOUT = 20000;
	private static final long LOGOUT_INIT_TIMEOUT = 5000;
	private boolean closeCommandReceived=false;
	/**
	 * flag to run logout thread
	 */
	private boolean isLoggedIn=false;
	
	private final Runnable loginRunnable = new Runnable() {

		@Override
		public void run() {
			long started = System.currentTimeMillis();
			long now=started;
			while(!networkElement.isInitialized())
			{
				if(AutoLoginPlugin.this.closeCommandReceived)
					return;
				LOG.debug("wait for ne initialization completion");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					
				}
				now=System.currentTimeMillis();
				if(now-started>LOGIN_INIT_TIMEOUT)
					break;
			}
			AutoLoginPlugin.this.isLoggedIn=true;
			if(AutoLoginPlugin.this.odlsToConnect!=null && AutoLoginPlugin.this.odlsToConnect.size()>0)
			{
				for(ODLConfig odlcfg: AutoLoginPlugin.this.odlsToConnect)
				{
					doPutRequest(AutoLoginPlugin.this.config,odlcfg,AutoLoginPlugin.this.mediatorHostIp);
				}
			}

		}

	};
	private final Runnable logoutRunnable = new Runnable() {

		@Override
		public void run() {
			if(AutoLoginPlugin.this.odlsToConnect!=null && AutoLoginPlugin.this.odlsToConnect.size()>0)
			{
				if(AutoLoginPlugin.this.odlsToConnect!=null && AutoLoginPlugin.this.odlsToConnect.size()>0)
				{
					for(ODLConfig odlcfg: AutoLoginPlugin.this.odlsToConnect)
					{
						doDeleteRequest(AutoLoginPlugin.this.config,odlcfg,AutoLoginPlugin.this.mediatorHostIp);
					}
				}
			}
		}

	};
	private static String odlLoginScript(MediatorConfig cfg,ODLConfig odl,String hostip)
	{
		String s="<node xmlns=\"urn:TBD:params:xml:ns:yang:network-topology\">"+
				"<node-id>"+cfg.getName()+"</node-id>"+
				"<host xmlns=\"urn:opendaylight:netconf-node-topology\">"+hostip+"</host>"+
				"<port xmlns=\"urn:opendaylight:netconf-node-topology\">"+cfg.getNetconfPort()+"</port>"+
				"<username xmlns=\"urn:opendaylight:netconf-node-topology\">"+cfg.getNetconfUsername()+"</username>"+
				"<password xmlns=\"urn:opendaylight:netconf-node-topology\">"+cfg.getNetconfPassword()+"</password>"+
				"<tcp-only xmlns=\"urn:opendaylight:netconf-node-topology\">false</tcp-only>"+
				"<keepalive-delay xmlns=\"urn:opendaylight:netconf-node-topology\">120</keepalive-delay>"+
				"</node>";
		return s;
	}
	private void doPutRequest(MediatorConfig cfg,ODLConfig odl,String mediatorHostIp)
	{
		LOG.debug("login mediator to "+odl.Server);
		String base = odl.GetHostURL();
		BaseHTTPClient http=new BaseHTTPClient(base, odl.TrustAll);
		Map<String, String> headers = new HashMap<String,String>();
		headers.put("Accept","application/xml");
		headers.put("Content-Type","application/xml");
		headers.put("Authorization", BaseHTTPClient.getAuthorizationHeaderValue(odl.User,odl.Password));
		String uri="/restconf/config/network-topology:network-topology/topology/topology-netconf/node/"+cfg.getName();
		try {
			BaseHTTPResponse response = http.sendRequest(uri, "PUT", odlLoginScript(cfg, odl,mediatorHostIp),headers);
			if(response.isSuccessful())
				LOG.debug("login successful");
			else
				LOG.debug("login maybe failed. "+response.toString());
		} catch (IOException e) {
			LOG.warn("problem login mediator to "+odl.Server+":"+e.getMessage());
		}

	}
	private void doDeleteRequest(MediatorConfig cfg,ODLConfig odl,String mediatorHostIp)
	{
		LOG.debug("logout mediator from "+odl.Server);
		String base = odl.GetHostURL();
		BaseHTTPClient http=new BaseHTTPClient(base, odl.TrustAll);
		Map<String, String> headers = new HashMap<String,String>();
		headers.put("Accept","application/xml");
		headers.put("Content-Type","application/xml");
		headers.put("Authorization", BaseHTTPClient.getAuthorizationHeaderValue(odl.User,odl.Password));
		String uri="/restconf/config/network-topology:network-topology/topology/topology-netconf/node/"+cfg.getName();
		try {
			BaseHTTPResponse response = http.sendRequest(uri, "DELETE", odlLoginScript(cfg, odl,mediatorHostIp),headers);
			if(response.code==200)
				LOG.debug("logout successful");
			else
				LOG.debug("logout maybe failed. "+response.toString());
	} catch (IOException e) {
			LOG.warn("problem logout mediator to "+odl.Server+":"+e.getMessage());
		}
	}
	private final ODLConfigCollection odlsToConnect;
	private final String mediatorHostIp;
	public AutoLoginPlugin(NetworkElement ne, MediatorConfig cfg,String mediatorHostIp) {
		super(ne, cfg);
		this.mediatorHostIp=mediatorHostIp;
		this.odlsToConnect = cfg.getODLConfig();
	}

	@Override
	public void onPreInit() {
		new Thread(loginRunnable).start();

	}

	@Override
	public void onPostInit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPreRequest(String messageId, NetconfTagList tags) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPostRequest(String messageId, NetconfTagList tags) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPreEditRequest(String messageId, NetconfTagList tags, Document sourceMessage) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPostEditRequest(String messageId, NetconfTagList tags, Document sourceMessage) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClose() {
		this.closeCommandReceived = true;
		if (this.isLoggedIn) {
			Thread t = new Thread(logoutRunnable);
			t.start();

			try {
				t.wait(LOGOUT_INIT_TIMEOUT);
			} catch (InterruptedException e) {
				
			}
		}

	}

	@Override
	public void onDeviceConnectionStatusChanged(int before, int now) {
		// TODO Auto-generated method stub

	}
	@Override
	public String getVersion() {
		return VERSION_STRING;
	}

}
