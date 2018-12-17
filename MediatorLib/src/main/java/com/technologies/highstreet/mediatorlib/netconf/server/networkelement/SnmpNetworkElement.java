package com.technologies.highstreet.mediatorlib.netconf.server.networkelement;


import com.technologies.highstreet.mediatorlib.netconf.server.basetypes.Console;
import com.technologies.highstreet.mediatorlib.netconf.server.basetypes.SnmpKeyValuePair;
import com.technologies.highstreet.mediatorlib.netconf.server.basetypes.SnmpKeyValuePairList;
import com.technologies.highstreet.mediatorlib.netconf.server.basetypes.SnmpTrapNotification;
import com.technologies.highstreet.mediatorlib.netconf.server.networkelement.NetworkElement;
import com.technologies.highstreet.mediatorlib.netconf.server.types.NetconfTagList;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Extend NetworkElement with function to act as Mediator for SNMP Devices.
 * This extension does not implement specific device functions.
 * SNMP Related OID information is part of the XML Document, that represents the device.
 *
 * @author Micha
 */

public class SnmpNetworkElement extends NetworkElement {

    private static final Log LOG = LogFactory.getLog(SnmpNetworkElement.class);

    private static String OIDPARAMETERNAMESTART = "$OIDVALUE=";
    private static String OIDPARAMETERNAMEEND = "<";

    // private final BaseSNMPDevice mSNMPDevice;
    //private final ProblemProcessor problemProcessor = null;

    protected String snmpIp = null;
    protected int snmpPort = 161;
    protected String mName = "";
    protected Integer snmpTrapPort = null;
    @SuppressWarnings("unused")
    private final boolean isPortMapMaster = false;
    protected final NodeEditConfigCollection mSNMPNodes;
    protected final HashMap<String, SnmpTrapNotification> mAvailableTraps;
    protected final String xmlFilename;
    protected final String xmlVersion;

    private boolean mDeviceConnected;

    /*
     * Constructor
     */
    public SnmpNetworkElement(String filename, String schemaPath, String uuid, String remoteSNMPIp, int remoteSNMPPort, int trapport, Console console)
            throws SAXException, IOException, ParserConfigurationException, TransformerConfigurationException, XPathExpressionException {
        super(filename, schemaPath, uuid, console);
        this.xmlFilename = filename;
        this.xmlVersion = this.detectXMLVersion();
        this.snmpIp = remoteSNMPIp;
        this.snmpPort = remoteSNMPPort;
        this.snmpTrapPort = trapport;
        if (this.snmpIp == null || this.snmpTrapPort == null) {
            throw new IllegalArgumentException("Can not find ip and trap port.");
        }

        // this.mSNMPDevice = BaseSNMPDevice.CREATOR.Create(type);
        this.mSNMPNodes = this.searchForOID(""/*"LTP-MWPS-TTP-RADIO"*/, this.getXmlSubTree("//data"), "oid",
                new NodeEditConfigCollection());

        this.mAvailableTraps = new HashMap<>();
        this.fillAvailableTraps(this.getNode("//snmptrap-notifications"));

        this.addNotifications(new String[] { "snmpTrapOid", "snmpTrapValue" },
                "//snmptrap-notifications/problem-notification",
                "//snmptrap-notifications/attribute-value-changed-notification");

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

    public HashMap<String, SnmpTrapNotification> getAvailableTraps()
    {return this.mAvailableTraps;}

    public String getXmlVersion() {
        return this.xmlVersion;
    }

    public NodeEditConfigCollection getSNMPNodes() {
        return this.mSNMPNodes;
    }

    public String getXmlFilename() {
        return this.xmlFilename;
    }

 

    public String getDeviceIp() {
        return this.snmpIp;
    }

    public void setDeviceIp(String ip) {
        this.snmpIp = ip;
    }

    public int getDevicePort() {
        return this.snmpPort;
    }

    public void setDevicePort(int port) {
        this.snmpPort = port;
    }

    public int getSNMPTrapPort() {
        return this.snmpTrapPort;
    }

    public String getDeviceName() {
        return this.mName;
    }

    public void setDeviceName(String name) {
        this.mName = name;
    }

    private void fillAvailableTraps(Node root) {
        if (root == null) {
            return;
        }
        NodeList notifications = root.getChildNodes();
        if (notifications == null || notifications.getLength() <= 0) {
            return;
        }
        int i, j;
        String snmpTrapOID = "", snmpTrapValue = "";
        NamedNodeMap attrs;
        for (i = 0; i < notifications.getLength(); i++) {
            if (notifications.item(i).getNodeType() == Node.ELEMENT_NODE
                    && notifications.item(i).getNodeName().equals("problem-notification")) {
                attrs = notifications.item(i).getAttributes();
                if (attrs != null) {
                    for (j = 0; j < attrs.getLength(); j++) {
                        if (attrs.item(j) != null && attrs.item(j).getNodeName().equals("snmpTrapOid")) {
                            snmpTrapOID = attrs.item(j).getTextContent();
                        } else if (attrs.item(j) != null && attrs.item(j).getNodeName().equals("snmpTrapValue")) {
                            snmpTrapValue = attrs.item(j).getTextContent();
                        }
                    }

                    String key = String.format("%s%s", snmpTrapOID, snmpTrapValue);
                    String name = findNodeValue(notifications.item(i).getChildNodes(), "problem");
                    String ref = findNodeValue(notifications.item(i).getChildNodes(), "object-id-ref");
                    String sev = findNodeValue(notifications.item(i).getChildNodes(), "severity");
                    if (name != null && ref != null && sev != null) {
                        this.mAvailableTraps.put(key, new SnmpTrapNotification(name, ref, sev));
                    }

                }
            }
        }

    }

    private static String findNodeValue(NodeList items, String nodeName) {
        String value = null;
        if (items != null) {
            for (int i = 0; i < items.getLength(); i++) {
                if (items.item(i) != null && items.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    if (items.item(i).getNodeName().equals(nodeName)) {
                        value = items.item(i).getTextContent();
                        break;
                    }

                }
            }
        }
        return value;
    }

    /*----------------------------------------------------------------------------------------
     * Modify xml-model as preparation for answer back to controller
     */


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
    //protected abstract void runPreInitTask();

    protected String replaceConfigValues(String xmlMessage) {
        return xmlMessage.replace("$NEIPADDRESS", this.snmpIp).replace("$MOUNTPOINTNAME", this.mName);
    }

    private static String removeSnmpCommentsAndAttributes(String xmlMessage) {
        return removeCommentsAndAttributes(xmlMessage, "oid", "snmpTrapOid", "snmpTrapValue", "conversion", "access");
    }

    /**
     * Remove specified part of message. Example1: oid="dsfsdf"
     * removeFromString(xml, "oid", "\"", "\"") Example2: <!-- -->
     * removeFromString(xml, "<!--", null, "-->")
     *
     * @param xml
     *            input/output with message
     * @param start
     *            indicating string (could be null)
     * @param intermediate
     *            intermediate
     * @param end
     *            end indicating string
     * @return changed xml as StringBuffer
     */
    protected static StringBuffer removeFromString(StringBuffer xml, String start, String intermediate, String end) {

        int protect = 100;
        int idx1a = 0;
        int idx1b, idx2;

        while ((idx1a = xml.indexOf(start, idx1a)) > -1 && protect-- > 0) {
            if (intermediate == null || intermediate.isEmpty()) {
                idx1b = idx1a + start.length();
            } else {
                idx1b = xml.indexOf(intermediate, idx1a + start.length());
                if (idx1b == -1) {
                    idx1a++;
                    continue;
                }
                idx1b += intermediate.length();
            }
            idx2 = xml.indexOf(end, idx1b);
            if (idx2 == -1) {
                idx1a++;
                continue;
            }
            if (idx2 > idx1a) {
                xml.replace(idx1a, idx2 + end.length(), "");
            }
        }
        return xml;
    }

    /**
     * Remove specific strings and all comments from message
     *
     * @param xmlMessage
     *            to process
     * @param names
     *            with attributes to be removed
     * @return xml as string
     */
    protected static String removeCommentsAndAttributes(String xmlMessage, String... names) {
        StringBuffer xml = new StringBuffer(xmlMessage);

        xml = removeFromString(xml, "<!--", null, "-->");

        for (String name : names) {
            xml = removeFromString(xml, name, "\"", "\"");
        }

        return xml.toString();
    }

    /**
     * Do processing for bundle of received traps
     *
     * @param traps
     *            contains all consecutive taps
     * @return processed message
     */
    public String doProcessSnmpTrapAction(List<SnmpKeyValuePair> traps) {

        // start processing
        String key = null;
        String xmlSubTree = null;

        // Try to find for one key created by oid+value a definition for a notification
        for (SnmpKeyValuePair trap : traps) {
            key = trap.getOid() + trap.getValue();
            xmlSubTree = getNotification(key);
            if (xmlSubTree != null) {
                break;
            }
        }

        // No process the notification and take over some values from the traps
        if (xmlSubTree != null) {
            StringBuffer xmlMsg = new StringBuffer(assembleRpcNotification(xmlSubTree));

            int idxStart = 0, paramIdxStart, paramIdxEnd;
            String oidString;

            while ((idxStart = xmlMsg.indexOf(OIDPARAMETERNAMESTART, idxStart)) > -1) {
                paramIdxStart = idxStart + OIDPARAMETERNAMESTART.length();
                paramIdxEnd = xmlMsg.indexOf(OIDPARAMETERNAMEEND, paramIdxStart);
                if (idxStart < paramIdxStart && paramIdxStart < paramIdxEnd) {
                    oidString = xmlMsg.substring(paramIdxStart, paramIdxEnd);
                    for (SnmpKeyValuePair trap : traps) {
                        if (oidString.contentEquals(trap.getOid())) {
                            xmlMsg.replace(idxStart, paramIdxEnd, trap.getValue());
                        }
                    }
                }
                idxStart = paramIdxStart;
            }
            String xmlMsgString = removeSnmpCommentsAndAttributes(xmlMsg.toString());
            LOG.debug("Notification for key '" + key + "'\n" + xmlMsgString);
            return xmlMsgString;
        } else {
            LOG.warn("No instructions for:" + traps);
            return null;
        }
    }

    public NodeEditConfigCollection getOIDsForRequest(String messageId, NetconfTagList tags) {

        if (tags.isEmtpy()) {

            return NodeEditConfigCollection.EMPTY;

        } else {

            String xmlSubTreePath = tags.getSubTreePath();
            LOG.debug("Subtreepath=" + xmlSubTreePath);
            NodeList xmlSubTree = this.getXmlSubTree( "//data/" + xmlSubTreePath); // Get nodes
                                                                                                            // from
                                                                                                            // Document
            LOG.debug("onPreEditConfigTarget odelist: " + xmlSubTree.getLength());
            NodeEditConfigCollection res = itOIDsForRequest(mSNMPNodes, xmlSubTree, new NodeEditConfigCollection());
            LOG.debug("onPreEditConfigTarget odelist: " + res.size());
            return res;

        }
    }

    public NodeEditConfigCollection getOIDsForEditRequest(String messageId, NetconfTagList tags) {

        if (tags.isEmtpy()) {

            return NodeEditConfigCollection.EMPTY;

        } else {

            String xmlSubTreePath = tags.getSubTreePath(5, 3);
            LOG.debug("onPreEditConfigTarget subtreepath=" + xmlSubTreePath);
            // consoleMessage("onPreEditConfigTarget subtree: "+xmlSubTreePath);
            NodeList xmlSubTree = this.getXmlSubTree("//data/" + xmlSubTreePath); // Get nodes
                                                                                                            // from
                                                                                                            // Document
            LOG.debug("onPreEditConfigTarget nodelist: " + xmlSubTree.getLength());
            NodeEditConfigCollection res = itOIDsForRequest(mSNMPNodes, xmlSubTree, new NodeEditConfigCollection());
            LOG.debug("onPreEditConfigTarget nodelist: " + res.size());
            return res;

        }
    }

    /**
     * set Value From SNMP Response into internal XMLModel
     *
     * @param node
     *            of document to be changed
     * @param value
     *            with new content
     * @return true if there was a parameter change
     */
    public boolean setSNMPValueInDocumentNode(NodeEditConfig node, String value) {
        boolean r = false;
        if (node != null) {
            return node.setConvertedSnmpValue2Xml(value);
        }
        return r;
    }

    /*----------------------------------------------------------------------------------------
     * Private functions to process the XML model.
     */

    private NodeEditConfigCollection searchForOID(String xPath, NodeList list, final String attributeName,
            NodeEditConfigCollection result) {
        Element e = null;
        String xPathOfNode, oid;
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            xPathOfNode = xPath + "/" + n.getNodeName();
            String lp = getNodeAttribute(n, "layer-protocol");
            if (!lp.isEmpty()) {
                xPathOfNode = xPathOfNode + "[" + "layer-protocol=\"" + lp + "\"]"; // Add layerprotocol index
            }
            // consoleMessage("searchForOID: "+lp+" "+n.getNodeType()+" "+n.getNodeName()+"
            // "+n.getBaseURI()+" xPath:"+xPathOfNode);

            if (n.hasAttributes() && n instanceof Element) {
                e = (Element) n;
                if (e.hasAttribute(attributeName) && !e.getAttribute(attributeName).isEmpty()) {
                    oid = e.getAttribute(attributeName);
                    result.add(new NodeEditConfig(xPathOfNode.replaceFirst("data", ""), e, oid));
                    e.setAttribute(attributeName,"");
                }
            }
            if (n.hasChildNodes()) {
                searchForOID(xPathOfNode, n.getChildNodes(), attributeName, result);
            }
        }
        return result;
    }

    private static NodeEditConfigCollection itOIDsForRequest(NodeEditConfigCollection mSNMPNodes, NodeList list,
            NodeEditConfigCollection result) {
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item(i);
            Element e = null;
            if (n.hasAttributes() && n instanceof Element) {
                e = (Element) n;
                if (e.hasAttribute("oid")) {
                    NodeEditConfig c = mSNMPNodes.find(e);
                    if (c != null) {
                        result.add(c);
                    }
                }
            }
            if (n.hasChildNodes()) {
                itOIDsForRequest(mSNMPNodes, n.getChildNodes(), result);
            }
        }
        return result;
    }



    /*
     * process function for filling or clearing problem list
     */
    @Deprecated
    public boolean onTrapReceived(SnmpKeyValuePairList traps) {

        boolean r = false;
        String key = null;
        SnmpTrapNotification notification = null;
        // Try to find for one key created by oid+value a definition for a notification
        for (SnmpKeyValuePair trap : traps.get()) {
            key = trap.getOid() + trap.getValue();
            notification = this.mAvailableTraps.get(key);
            if (notification != null) {
                break;
            }
        }

        if (notification != null) {
            r = sendNetconfNotification(notification);
        } else {
            LOG.warn("no notification found for trap:" + traps.toString());
            r = false;
        }
        return r;
    }

    public boolean isRemoteDeviceConnected() {
        return mDeviceConnected;
    }
    public void setRemoteDeviceConnected(boolean connected) {
        this.mDeviceConnected=connected;
    }

    public boolean sendNetconfNotification(SnmpTrapNotification notification) {
           this.pushToExternalMessageQueue(notification);
           return true;
    }
}
