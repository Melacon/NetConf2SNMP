package com.technologies.highstreet.netconf2snmpmediator.server.networkelement;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import com.technologies.highstreet.mediatorlib.data.DataTable;
import com.technologies.highstreet.mediatorlib.data.SNMPCache;
import com.technologies.highstreet.mediatorlib.netconf.server.basetypes.SnmpKeyValuePair;
import com.technologies.highstreet.mediatorlib.netconf.server.basetypes.SnmpKeyValuePairList;
import com.technologies.highstreet.netconf.server.basetypes.TimeSpan;
import com.technologies.highstreet.netconf2snmpmediator.server.Config;

public class SNMPConnector implements CommandResponder, ResponseListener {
	private static final Log LOG = LogFactory.getLog(SNMPConnector.class);
	//disabled for now
	private static final SNMPCache snmpCache=null;//new SNMPCache(SNMPCache.TIMEOUT_5SECONDS);

	private final int snmpTimeout;
	private final int trapPort;
	private final int snmpRetries;
	private final int snmpVersion;
	protected Snmp snmp;
	private int version;
	private Target target;
	private final String remoteIP;
	private final int remotePort;
	private int iterations;
	private boolean finished;
	private String lastOID = "";
	private final Random rnd;
	private InetAddress bindingIp;
	private boolean reuseAddress;
	private int requestErrors;
	public void setBindingIpAddress(InetAddress ip)
	{this.bindingIp=ip;}
	public void setReuseAddress(boolean b)
	{this.reuseAddress=b;}

	/**
	 *
	 * @param cfg :
	 * @param trapport: port to listen to for traps
	 * @param remoteip: snmp destination ip address
	 * @param remoteport : snmp destination port (default should be 161)
	 *
	 */
	public SNMPConnector(Config cfg,int trapport,String remoteip,int remoteport)
	{
		this.snmpTimeout = cfg.SNMPRequestLatency;// in ms
		this.snmpRetries = cfg.SNMPRequestRetries;
		this.snmpVersion = cfg.SNMPVersion;
		this.trapPort = trapport;
		this.remoteIP = remoteip;
		this.remotePort = remoteport;
		this.rnd = new Random();
		this.reuseAddress=false;
		this.requestErrors=0;
	}
	public void triggerCache() {
		try
		{
			if(snmpCache!=null)
				snmpCache.trigger();
		}catch(Exception err)
		{
			LOG.error(err.getMessage());
		}
	}
	/** Do SNMP specific construct actions */
	protected void initSNMP() throws IOException {
		TransportMapping<?> transport = null;
		// try {
		LOG.debug("init snmp: TRAPPORT: " + this.trapPort + " RemoteIP: " + this.remoteIP + ":" + this.remotePort);
		//transport = new DefaultUdpTransportMapping(new UdpAddress("0.0.0.0/" + this.trapPort));
		if(this.bindingIp!=null)
			transport = new DefaultUdpTransportMapping(new UdpAddress(this.bindingIp,this.trapPort),this.reuseAddress);
		else
			transport = new DefaultUdpTransportMapping(new UdpAddress(this.trapPort),this.reuseAddress);
		long before=System.currentTimeMillis();
		LOG.debug("listening for traps on "+transport.getListenAddress().toString() +" lazyMode="+this.reuseAddress + "("+before+")");
		this.snmp = new Snmp(transport);
		long after = System.currentTimeMillis();
		TimeSpan ts = TimeSpan.ofMillis(after-before);
		LOG.debug("finished binding port ("+after+") => duration: "+ts.toString());
		if (this.version == SnmpConstants.version3) {
			byte[] localEngineID = MPv3.createLocalEngineID();
			// byte[] localEngineID = ((MPv3)
			// snmp.getMessageProcessingModel(MessageProcessingModel.MPv3)).createLocalEngineID();
			USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(localEngineID), 0);
			SecurityModels.getInstance().addSecurityModel(usm);
			snmp.setLocalEngine(localEngineID, 0, 0);

		}
		LOG.debug("remote target device is "+this.remoteIP + "/"+this.remotePort);
		this.target = new CommunityTarget(new UdpAddress(this.remoteIP + "/"+this.remotePort), new OctetString("public"));
		this.target.setRetries(this.snmpRetries);
		this.target.setTimeout(this.snmpTimeout);
		this.target.setVersion(this.snmpVersion);
		this.finished = true;
		snmp.addCommandResponder(this);
		snmp.listen();

		// } catch (IOException e) {
		// e.printStackTrace();
		// log.error("unable to start snmplistener on Port"+this.trapPort);
		// }
	}
	public String snmpGetSyncCached(String oid, String defaultValue) throws IOException {
		String value=null;
		if(snmpCache!=null)
		{
			SnmpKeyValuePair x = snmpCache.get(oid);
			if(x!=null)
				value = x.getValue();
		}
		if(value==null)
			value=this.snmpGetSync(oid, defaultValue);
		return value;
	}
	public boolean snmpSetSync(String oid, Variable value) throws IOException {

		boolean r=false;
		PDU pdu = new PDU();
		OID o = new OID(oid);

		LOG.debug("try to send set-request for oid="+oid+" with value="+value);
		VariableBinding varBind = new VariableBinding(o, value);
		pdu.add(varBind);

		pdu.setType(PDU.SET);
		pdu.setRequestID(new Integer32(this.rnd.nextInt()));
		ResponseEvent response = this.snmp.set(pdu, this.target);

		// Process Agent Response
		if (response != null) {
			LOG.debug("got snmp set response from agent");
			PDU responsePDU = response.getResponse();

			if (responsePDU != null) {
				int errorStatus = responsePDU.getErrorStatus();
				int errorIndex = responsePDU.getErrorIndex();
				String errorStatusText = responsePDU.getErrorStatusText();

				if (errorStatus == PDU.noError) {
					LOG.debug("snmp set response = " + responsePDU.getVariableBindings());
					r=true;
				} else {
					LOG.debug("set-request failed");
					LOG.debug("error status = " + errorStatus);
					LOG.debug("error index = " + errorIndex);
					LOG.debug("error status Text = " + errorStatusText);
				}
			} else {
				LOG.error("set-response PDU is null");
			}
		} else {
			LOG.error("set-request timed out ");
		}
		return r;
	}
	public void fillDataTable(DataTable dt) throws IOException
	{
		//dt.
	}
	public DataTable snmpGetTable(String baseOID,String[] colOIDs,String[] rows) throws Exception
	{
		DataTable dt=new DataTable(baseOID,colOIDs,rows);
		int num_retries=4;
		String oidTable = baseOID;
		String oid;
		int rowIdx=0;
		for(String colOID : colOIDs)
		{
			rowIdx=0;
			if(rows!=null)
			{
				for(String row:rows)
				{
					oid=oidTable+colOID+row;
					dt.setValueAt(colOID, rowIdx, this.snmpGetSync(oid,null));
					rowIdx++;
				}
			}
			else
			{
				oid=oidTable+colOID+".0";
				while(true)
				{
					SnmpKeyValuePair kvp=this.snmpGetNextSync(oid, null);
					if(oid.equals(kvp.getOid()) && kvp.getValue()==null)
					{
						num_retries--;
						if(num_retries<=0)
							throw new Exception("problem requesting values for "+oid);
					}
					if(kvp.getOid().contains(oidTable+colOID))
					{
						dt.setValueAt(colOID, rowIdx, kvp.getValue());
						rowIdx++;
						oid=kvp.getOid();
					}
					else
						break;
				}
			}
		}
		return dt;
	}
	public SnmpKeyValuePairList snmpGetSync(String[] oids, String[] defaultValues) {
		SnmpKeyValuePairList list=new SnmpKeyValuePairList();
		String oid,defaultValue;
		for(int i=0;i<oids.length;i++)
		{
			oid = oids[i];
			defaultValue=defaultValues==null?null:i<defaultValues.length?defaultValues[i]:null;
			try {
				defaultValue=this.snmpGetSync(oid,defaultValue);
			}
			catch(Exception e)
			{}
			list.add(new SnmpKeyValuePair(oids[i], defaultValue));
		}

		return list;
	}
	public SnmpKeyValuePairList snmpGetBulkSync(String[] oids,String[] defaultValues) throws IOException
	{
		PDU pdu = new PDU();
		String soids="";
		for(int i=0;i<oids.length;i++)
		{
			pdu.add(new VariableBinding(new OID(oids[i])));
			soids=oids[i]+",";
		}
		LOG.debug("send bulk request for "+soids);
		SnmpKeyValuePairList list=new SnmpKeyValuePairList();
		for(int i=0;i<oids.length;i++)
		{
			list.add(new SnmpKeyValuePair(oids[i],defaultValues==null?null:i<defaultValues.length?defaultValues[i]:null ));
		}
		ResponseEvent evt = this.snmp.getBulk(pdu, this.target);
		String oid,defaultValue;
		if (evt != null) {
			PDU evtResponsePdu = evt.getResponse();
			if (evtResponsePdu != null) {
				// int errorStatus = evtResponsePdu.getErrorStatus();
				// String error = evtResponsePdu.getErrorStatusText();
				if (evtResponsePdu.size() > 0) {
					for(int i=0;i<evtResponsePdu.size();i++)
					{
						oid = evtResponsePdu.get(i).getOid().toString();
						defaultValue = evtResponsePdu.get(i).toValueString();
						LOG.debug("bulk result oid="+oid+" value="+defaultValue);
						list.add(new SnmpKeyValuePair(oid, defaultValue));
						if(snmpCache!=null)
							snmpCache.set(oid,defaultValue);
					}

				}
				else
					LOG.debug("pdu is empty");
			}
		}
		else
			LOG.debug("result is null");
		return list;
	}
	public String snmpGetSync(String oid, String defaultValue) throws IOException {

		if(snmpCache!=null)
		{
			LOG.debug("checking cache");
			SnmpKeyValuePair x=snmpCache.get(oid);
			if(x!=null)
			{
				LOG.debug("found value in cache");
				return x.getValue();
			}
		}
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(new OID(oid)));
		ResponseEvent evt = this.snmp.get(pdu, this.target);
		if (evt != null) {
			PDU evtResponsePdu = evt.getResponse();
			if (evtResponsePdu != null) {
				// int errorStatus = evtResponsePdu.getErrorStatus();
				// String error = evtResponsePdu.getErrorStatusText();
				if (evtResponsePdu.size() > 0) {
					defaultValue = evtResponsePdu.get(0).toValueString();
					if(snmpCache!=null)
						snmpCache.set(oid,defaultValue);
				}
			}
		}

		return defaultValue;
	}

	public SnmpKeyValuePair snmpGetNextSync(String oid, String defaultValue) throws IOException {
		PDU pdu = new PDU();
		if (oid.endsWith(".*")) {
			oid = oid.substring(0, oid.length() - 2);
		}
		LOG.debug("getnext for "+oid);
		pdu.add(new VariableBinding(new OID(oid)));
		ResponseEvent evt = this.snmp.getNext(pdu, this.target);
		if (evt != null) {
			PDU evtResponsePdu = evt.getResponse();
			if (evtResponsePdu != null) {
				// int errorStatus = evtResponsePdu.getErrorStatus();
				// String error = evtResponsePdu.getErrorStatusText();
				if (evtResponsePdu.size() > 0) {
					oid = evtResponsePdu.get(0).getOid().toString();
					defaultValue = evtResponsePdu.get(0).toValueString();
					LOG.debug("value="+ defaultValue);
					if(snmpCache!=null)
						snmpCache.set(oid,defaultValue);
				}
			}

		}
		return new SnmpKeyValuePair(oid, defaultValue);
	}
	private void printPDU(PDU pdu) {
		if (pdu != null) {
			this.printPDU(pdu.get(0).getOid(), pdu.get(0).toValueString());
		} else {
			LOG.debug("pdu is null");
		}
	}

	private void printPDU(OID oid, String value) {
		LOG.debug("" + this.iterations + ": " + oid + ":" + value);
	}
	/*
	 * Async Request Response(non-Javadoc)
	 *
	 * @see org.snmp4j.event.ResponseListener#onResponse(org.snmp4j.event.
	 * ResponseEvent)
	 */
	@Override
	public void onResponse(ResponseEvent evt) {

		((Snmp) evt.getSource()).cancel(evt.getRequest(), this);
		PDU pdu = evt.getResponse();
		if (pdu != null && this.iterations > 0) {
			printPDU(pdu);
			try {
				String currentOID = pdu.get(0).getOid().toDottedString();
				// if oid is the same as the last one
				if (this.lastOID != null && this.lastOID.length() > 0 && this.lastOID.equals(currentOID)) {
					this.finished = true;
				} else // new oid to request
				{
					this.lastOID = currentOID;
					PDU pdu_req = new PDU();
					pdu_req.add(new VariableBinding(pdu.get(0).getOid()));
					this.iterations--;
					this.snmp.getNext(pdu_req, this.target, null, this);
				}
			} catch (Exception e) {
				e.printStackTrace();
				this.finished = true;
			}

		} else {
			this.finished = true;
		}
	}


	/*
	 * UPD Trap Event(non-Javadoc)
	 *
	 * @see
	 * org.snmp4j.CommandResponder#processPdu(org.snmp4j.CommandResponderEvent)
	 */
	@Override
	public void processPdu(CommandResponderEvent evt) {
		PDU command = evt.getPDU();
		if (command != null && command.size() > 0) {
			SnmpKeyValuePairList traps = new SnmpKeyValuePairList();
			for (int i = 0; i < command.size(); i++) {
				VariableBinding item = command.get(i);
				if (item != null) {
					Variable value=item.getVariable() ;
					if(value!= null)
					{
						traps.add(new SnmpKeyValuePair(item.getOid().toDottedString(),value));
					}
				}
			}
			this.onTrapReceived(traps);

		}
		// printPDU(command);
	}

	protected void onTrapReceived(SnmpKeyValuePairList traps) {

	}
	public void startListen() throws IOException
	{
		this.initSNMP();
	}
	public void shutdown() {
		this.snmp.removeCommandResponder(this);
		try {
			this.snmp.close();
		} catch (IOException e) {
			LOG.warn("problem closing object: "+e.getMessage());
		}

	}

}
