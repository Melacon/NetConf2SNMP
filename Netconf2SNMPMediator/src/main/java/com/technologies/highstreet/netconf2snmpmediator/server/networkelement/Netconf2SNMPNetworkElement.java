package com.technologies.highstreet.netconf2snmpmediator.server.networkelement;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.technologies.highstreet.mediatorlib.netconf.server.basetypes.Console;
import com.technologies.highstreet.mediatorlib.netconf.server.networkelement.NodeEditConfigCollection;
import com.technologies.highstreet.mediatorlib.netconf.server.networkelement.SnmpNetworkElement;
import com.technologies.highstreet.mediatorlib.netconf.server.types.NetconfTagList;
import com.technologies.highstreet.netconf2snmpmediator.server.MonitoredNetworkElement;

/**
 * Extend NetworkElement with function to act as Mediator for SNMP Devices.
 * Contains the connector to SNMP
 *
 * @author Micha
 */
public abstract class Netconf2SNMPNetworkElement extends SnmpNetworkElement implements  MonitoredNetworkElement {

    private static final Log LOG = LogFactory.getLog(Netconf2SNMPNetworkElement.class);

    protected final Netconf2SNMPConnector snmpConnector;
    protected final long mDeviceType;

    /*
     * Constructor
     */
    public Netconf2SNMPNetworkElement(String filename, String schemaPath, String uuid, long type,
            String remoteSNMPIp, int remoteSNMPPort, int trapport, Console console,IOnPluginEventListener pluginListener,IOnTrapReceivedListener trapListener) throws SAXException, IOException,
            ParserConfigurationException, TransformerConfigurationException, XPathExpressionException {
        super(filename, schemaPath, uuid,remoteSNMPIp,remoteSNMPPort,trapport,console);
        this.mDeviceType = type;
        this.snmpConnector = new Netconf2SNMPConnector(this, getConsole(),pluginListener,trapListener);
        LOG.info(String.format("Current SNMP NetworkElement ip:%s Trapport:%d has %d SNMP values and %d possible traps",
                this.snmpIp, this.snmpTrapPort, this.mSNMPNodes.size(), this.mAvailableTraps.size()));
    }
    /*
     * Constructor
     *
     * @Deprecated public Netconf2SNMPNetworkElement(String filename, String
     * schemaPath, SNMPDeviceType type, String remoteIp, int snmpPort, Console
     * console) throws SAXException, IOException, ParserConfigurationException,
     * TransformerConfigurationException, XPathExpressionException { this(filename,
     * schemaPath, null, type, console); }
     */

    /*----------------------------------------------------------------------------------------
     * Get/Set
     */

    public Netconf2SNMPConnector getConnector() {
        return this.snmpConnector;
    }

    @Override
    public String getXmlVersion() {
        return this.xmlVersion;
    }

    @Override
    public NodeEditConfigCollection getSNMPNodes() {
        return this.mSNMPNodes;
    }


    @Override
    public String getDeviceIp() {
        return this.snmpIp;
    }

    @Override
    public void setDeviceIp(String ip) {
        this.snmpIp = ip;
    }

    @Override
    public int getDevicePort() {
        return this.snmpPort;
    }

    @Override
    public void setDevicePort(int port) {
        this.snmpPort = port;
    }

    @Override
    public int getSNMPTrapPort() {
        return this.snmpTrapPort;
    }

    public long getDeviceClass() {
        return this.mDeviceType;
    }

    @Override
    public String getDeviceName() {
        return this.mName;
    }

    @Override
    public void setDeviceName(String name) {
        this.mName = name;
    }


    /*----------------------------------------------------------------------------------------
     * Modify xml-model as preparation for answer back to controller
     */
    /**
     * First analyze message, call information from SNMP device and update the
     * related Document parameter and send SNMP commands Secondly do the original
     * function and create reply message.
     */
    @Override
    public List<String> assembleEditConfigElementReplyList(String sessionId, NetconfTagList tags,
            String xml) {
        try {
            // consoleMessage("------------------------------------");
            // consoleMessage("Start editConfig processing for message "+sessionId);
            Document inDoc = loadXMLFromString(xml);
            // consoleMessage("Doc loaded");
            snmpConnector.onPreEditConfigTarget(sessionId, tags, inDoc);
        } catch (Exception e) {
            LOG.warn("Can not do SNMP processing: ", e);
        }
        // return super.assembleEditConfigElementReplyList(sessionId, tags, xml);
        List<String> res = new ArrayList<>();
        res.add(assembleRpcReplyOk(sessionId));
        return res;
    }

    /**
     * First analyze message, call information from SNMP device and update the
     * related Document parameter. Secondly create reply message.
     */
    @Override
    public String assembleRpcReplyFromFilterMessage(String id, NetconfTagList tags) {
        // do a sync snmp set request
        NodeEditConfigCollection oidNodes = getOIDsForRequest(id, tags);
        snmpConnector.onPreReplyMessage(id, tags, oidNodes);
        String message = super.assembleRpcReplyFromFilterMessage(id, tags);
        return this.replaceConfigValues(message);
    }

    /**
     * Delete from outgoing message SNMP related elements
     */
    @Override
    protected String replaceAndWash(String xmlMessage) {
        xmlMessage = super.replaceAndWash(xmlMessage);
        xmlMessage = this.replaceConfigValues(xmlMessage);
        xmlMessage = removeCommentsAndAttributes(xmlMessage);
        return xmlMessage;
    }

    /*----------------------------------------------------------------------------------------
    * Functions
    */
    /*
    @Override
    public synchronized String doProcessSnmpTrapAction(List<SnmpKeyValuePair> traps) {
        String s= super.doProcessSnmpTrapAction(traps);
        return s;
    }
    private void configRefreshProblemTimestamps() {
        this.setConfigRefreshTimestamp( Config.getInstance().updateProblemTimestamps() );
    }
    */
    @Override
    public abstract void addToProblemListNe(String problemName, String problemSeverity, String timeStamp, Object deviceName,
            String valueOf);
    @Override
    public abstract boolean removeFromProblemListNe(String problemName);






}
