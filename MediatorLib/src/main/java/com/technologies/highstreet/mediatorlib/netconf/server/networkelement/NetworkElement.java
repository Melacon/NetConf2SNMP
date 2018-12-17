/**
 * NetworkElement is the database for the simulation. NetworkElement is getting responses from the XML file that defines the
 * Behavior of the simulated device. The answers are XML strings, that are directly produced from the contents of the XML inpt file.
 *
 * @author herbert.eiselt@highstreet-technologies.com
 *
 */

package com.technologies.highstreet.mediatorlib.netconf.server.networkelement;

import com.technologies.highstreet.mediatorlib.netconf.server.basetypes.Console;
import com.technologies.highstreet.mediatorlib.netconf.server.types.NetconfTag;
import com.technologies.highstreet.mediatorlib.netconf.server.types.NetconfTagList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import net.i2cat.netconf.messageQueue.MessageQueue;
import net.i2cat.netconf.rpc.RPCElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Own XML Document with all ONF Core NetworkElement and interfaces related information
 *
 * @author herbert
 */

public class NetworkElement extends NetconfMessageAssembler {

    private static final Log LOG = LogFactory.getLog(NetworkElement.class);
    private static final String EMPTY = "";

    private static final int INITSTATUS_NONE=0;
	private static final int INITSTATUS_INITIALIZING = 1;
	private static final int INITSTATUS_INITIALIZED = 2;
	
    private static final String UUIDNAME = "uuid";
    private static final String CONSOLEPREFIX1 = "\tdoc-change: ";

    private final Transformer transformer;
    private final String schemaPath;
    protected final Console console;
    private final HashMap<String, String> xmlSubTreeNotifications;
    private Document doc = null;
    private String nePath = null;
    private boolean configRefreshTimestamp = false;
    //private MessageQueue messageQueue = new MessageQueue();
    private int initializationStatus;
    private Node neNode;
    private Node neCurrentProblemListNode;
    private final HashMap<String,Node> mProblemLists;   //key = id, value current-problem-list node

    private final List<MessageQueue> externalMessageQueueList;

    /* ---------------------------------------------------------------
     * Constructor
     */
    public NetworkElement(String filename, String schemaPath, String uuid, Console console) throws SAXException, IOException, ParserConfigurationException, TransformerConfigurationException, XPathExpressionException {

        LOG.debug("Networkelements uses file: "+filename);
        this.initializationStatus = INITSTATUS_NONE;
        this.mProblemLists=new HashMap<>();
        this.externalMessageQueueList=new ArrayList<>();
         File file = new File(filename);
        FileInputStream fis = new FileInputStream(file);
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fis);
        fis.close();
        TransformerFactory tf = TransformerFactory.newInstance();
        transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        this.schemaPath = schemaPath;
        this.console = console;

        this.xmlSubTreeNotifications =  new HashMap<>();
        addNotifications(
                new String[]{ "userCmd" },
                "//mw-notifications/problem-notification",
                "//MW_Notifications/ProblemNotification",

                "//mw-notifications/attribute-value-changed-notification",
                "//MW_Notifications/AttributeValueChangedNotification");

        File schemaPathDirectory = new File(this.schemaPath);
        if (schemaPathDirectory.exists() && schemaPathDirectory.isDirectory()) {

            LOG.info(consoleMessage("Verify yang schemas listed in netconf-state/schemas definition: "));

            NodeList schemas = getNodeList(doc, "//data/netconf-state/schemas/schema");
            Node node;
            StringBuffer sbNotOk = new StringBuffer();
            StringBuffer sbOk = new StringBuffer();
            LOG.info(consoleMessage("Found number of schemas in XML-file: "+schemas.getLength()));
            for (int n = 0; n < schemas.getLength(); n++) {
                node = schemas.item(n);

                String fileName = getFilenameFromNode(schemaPath, node);
                File fileSchema = new File(fileName);
                if (!fileSchema.exists() || fileSchema.isDirectory()) {
                    sbNotOk.append("\t"+fileSchema.getName()+"\n");
                } else {
                    sbOk.append("\t"+fileSchema.getName()+"\n");
                }
            }
            if (sbOk.length() > 0) {
                LOG.info(consoleMessage("Schemas-OK:\n"+sbOk.toString()));
            }
            if (sbNotOk.length() > 0) {
                LOG.info(consoleMessage("Schemas-NOT OK:\n"+sbNotOk.toString()));
            }

            // Analyse mode version
            nePath = "//data/network-element"; //ONF V1.2
            this.neNode = getNode(doc, nePath);
            this.neCurrentProblemListNode = getNode(doc,"//data/network-element-pac/network-element-current-problems");
            if (this.neNode == null) {
                nePath = "//data/NetworkElement";  //ONF V1.0
                this.neNode = getNode(doc, nePath);
                this.neCurrentProblemListNode = getNode(doc,"//data/NetworkElementCurrentProblems");
            }
            if (this.neNode == null) {
                LOG.error(consoleMessage("Can not find networkelement definition"));
            } else {
                LOG.info(consoleMessage("Network element root: "+nePath));
            }

            //Get UUID
            Node uuidNode = getNode(doc, nePath+"/"+UUIDNAME);
            if (uuidNode != null) {
                if (uuid != null && !uuid.isEmpty()) {
                    LOG.info(consoleMessage("Overwrite uuid and name with parameter "+uuid));
                    uuidNode.setTextContent(uuid);
                    Node nameNode = getNode(doc, "//data/network-element/name/value");
                    if (nameNode != null) {
                        LOG.info(consoleMessage("Overwrite name with parameter "+uuid));
                        nameNode.setTextContent(uuid);
                    }
                }
                LOG.info(consoleMessage("device info uuid'"+uuidNode.getTextContent()+"'"));
            } else {
                LOG.info(consoleMessage("no uuid found within xml-File"));
            }

        } else {
            throw new IllegalArgumentException("Invalid schema directory: '"+String.valueOf(schemaPath)+"'");
        }
    }

    public NetworkElement(String filename, String schemaPath) throws SAXException, IOException, ParserConfigurationException, TransformerConfigurationException, XPathExpressionException {
        this(filename, schemaPath, null, null);
    }

    /* ---------------------------------------------------------------
     * getter/Setter
     */

    /**
     * Decide if timestamps are updated or not if raised multiple times
     * @return the configRefreshTimestamp
     */
    public boolean isConfigRefreshTimestamp() {
        return configRefreshTimestamp;
    }

    /*
    public Document getDocument() {
        return doc;
    }*/

    public Console getConsole() {
        return console;
    }

    public Node getNetworkElementNode()
    {return this.neNode;}
    public Node getNetworkElementCurrentProblemsNode()
    {return this.neCurrentProblemListNode;}
    /**
     * @param configRefreshTimestamp the configRefreshTimestamp to set
     */
    public void setConfigRefreshTimestamp(boolean configRefreshTimestamp) {
        this.configRefreshTimestamp = configRefreshTimestamp;
    }

    //public MessageQueue getMessageQueue() {
    //    return messageQueue;
    //}

    /*public void setMessageQueue(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }
*/
    /**
     * Get notification for given key
     * @param key for notification
     * @return xml String
     */
    public String getNotification(String key) {
        return xmlSubTreeNotifications.get(key);
    }

    /* ---------------------------------------------------------------
     * Private and static functions to generate some output
     */

    public boolean isInitialized()
    {return this.initializationStatus==INITSTATUS_INITIALIZED;}
    public void setInitializationAsStarted() {
    	this.initializationStatus=INITSTATUS_INITIALIZING;
    }
    public void setInitializationAsFinished() {
    	this.initializationStatus=INITSTATUS_INITIALIZED;
    }
    
    /**
     * Message to console
     * @param msg content
     * @return again the msg
     */
    protected String consoleMessage(String msg) {
    	if(console!=null)
    		return console.cliOutput("NE:"+msg);
    	return msg;
    }

    /**
     * Helper function to print set of strings
     * @param set to be converted to string
     * @return string with output
     */
    private static String listToString(Set<String> set ) {
        StringBuffer sb = new StringBuffer();
        for (String s : set) {
            sb.append(' ');
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * Create a list of the actual document structure
     * @param doc the document
     * @param xPathRoot the start node that is listed
     */
    private static void listNodesToConsole(Document doc, String xPathRoot, Console console) {
        Node root;

        try {
            root = getNode(doc, xPathRoot);
            for (Node l1 : getChildElementNodes(root)) {
                console.cliOutput(l1.getNodeName()+"-<root>");
                for (Node l2 : getChildElementNodes(l1)) {
                    console.cliOutput(l1.getNodeName()+"-"+l2.getNodeName());
                    for (Node l3 : getChildElementNodes(l2)) {
                        console.cliOutput(l1.getNodeName()+"-"+l2.getNodeName()+"-"+l3.getNodeName());
                    }
                }
            }
        } catch (XPathExpressionException e) {
            LOG.error("(..something..) failed", e); // TODO
        }

    }

    /* ---------------------------------------------------------------
     * Private and static functions to modify XML documents
     */

    /**
     * Convert XML-String into Document
     * @param xml input string
     * @return xml as document
     * @throws Exception different types could occur during conversion
     */

    protected static Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

    /**
     * get nodelist by xpath
     * Examples xpath:
     *  "//MW_EthernetContainer_Pac/ethernetContainerCurrentProblems"
     *  final XPathExpression xpath = XPathFactory.newInstance().newXPath().compile("//ethernetContainerCurrentProblems/currentProblemList/sequenceNumber");
     * @param doc document with xml data
     * @param root xpath declaration of root
     * @return the NodeList
     * @throws XPathExpressionException if something wrong
     */
    protected static NodeList getNodeList(Document doc, String root) throws XPathExpressionException {

        XPathExpression xpath = XPathFactory.newInstance().newXPath().compile(root);
        NodeList nodeList = (NodeList) xpath.evaluate(doc, XPathConstants.NODESET);
        return nodeList;

    }

    /**
     * get Node by xpath
     * @param doc document with xml data
     * @param root xpath declaration of root
     * @return the Node
     * @throws XPathExpressionException if something wrong
     */
    protected static Node getNode(Document doc, String root) throws XPathExpressionException {

        XPathExpression xpath = XPathFactory.newInstance().newXPath().compile(root);
        Node node = (Node)xpath.evaluate(doc, XPathConstants.NODE);
        return node;

    }

    /**
     * get all child nodes with type ELEMENT_NODE back in a list
     * @param node parent node
     * @return List with child nodes
     */
    protected static List<Node> getChildElementNodes(Node node) {
        List<Node> res = new ArrayList<>();
        NodeList childs = node.getChildNodes();
        Node item;
        //System.out.println("Query node "+node.getNodeName());
        for (int n=0; n < childs.getLength(); n++) {
            item = childs.item(n);
            //System.out.println(node.getNodeName()+"-"+item.getNodeName()+" "+item.getNodeType());
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                res.add(childs.item(n));
            }
        }
        return res;
    }

    /**
     * Subtree access to the XML tree that was created by the xml file.
     * If Element is a list with more than one element .. all are delivered back.
     * @param root starting node to read subtree
     * @return XML subtree
     */
    public static NodeList getXmlSubTree(Document doc, String root) {
        NodeList list=null;
        try {
            list = getNodeList(doc, root);
        } catch (XPathExpressionException e) {
            LOG.debug("XML XPath problem: "+e.getMessage());
        }
        return list;
    }


    /**
     * Subtree access to the XML tree that was created by the xml file.
     * If Element is a list with more than one element .. all are delivered back.
     * @param root starting node to read subtree
     * @return string with subtree content
     */
    public static String getXmlSubTreeAsString(Document doc, String root, Transformer transformer) {
        StringWriter sw = new StringWriter();

        try {
            NodeList nodeList = getNodeList(doc, root);

            for (int t = 0; t < nodeList.getLength(); t++) {
                transformer.transform(new DOMSource(nodeList.item(t)), new StreamResult(sw));
            }
        } catch (XPathExpressionException e) {
            LOG.debug("XML XPath problem: "+e.getMessage());
        } catch (TransformerException e) {
            LOG.debug("XML Transformer problem: "+e.getMessage());
        }

        return sw.toString();
    }

    /*
     * Subtree access to the XML tree that was created by the xml file.
     * If Element is a list with more than one element .. all are delivered back.
     * @param root starting node to read subtree
     * @return List of strings with subtree content with one element for node of the expected list
     *
    private List<String> getXmlSubTreeAsStringList(String root) {
        List<String> res = new ArrayList<String>();
        try {
            NodeList nodeList = getNodeList(doc, root);

            StringWriter sw = new StringWriter();
            for (int t = 0; t < nodeList.getLength(); t++) {
                sw = new StringWriter();
                transformer.transform(new DOMSource(nodeList.item(t)), new StreamResult(sw));
                res.add(sw.toString());
            }
        } catch (XPathExpressionException e) {
            LOG.debug("XML XPath problem: "+e.getMessage());
        } catch (TransformerException e) {
            LOG.debug("XML Transformer problem: "+e.getMessage());
        }
        return res;
    }/**/

    /**
     * Subtree access to the XML tree that was created by the xml file.
     * If Element is a list with more than one element .. all are delivered back.
     * @param root starting node to read subtree
     * @return string with subtree content
     */
    public String getXmlSubTreeAsString(String root) {
        return getXmlSubTreeAsString(doc, root, transformer);
    }

    /**
     * Get specific attribute content as String.
     * @param node The node in question
     * @param attribute the attribute name. Only ELEMENT_NODEs are considered
     * @return String or EMPTY string
     */
    protected static String getNodeAttribute(Node node, String attribute) {

        for (Node nodeChild : getChildElementNodes(node)) {

            if (nodeChild.getNodeName().equals(attribute)) {
                return nodeChild.getTextContent();
            }

        }
        return EMPTY;
    }

    private Document getMessageContentAsDocument(String xml) throws Exception {
       final Document inDoc = loadXMLFromString(xml);
       return inDoc;
    }

    /**
     * Collect from the given root a list with the given attribute that is used as univied ID.
     * @param doc document to search
     * @param root starting node to read subtree
     * @param attibuteName or null if the value itself is the id
     * @return HashSet with all id.
     */
    private static HashSet<String> getNodeIds(Document doc, String root, String attibuteName) {
        HashSet<String> res = new HashSet<>();
        try {
             final XPathExpression xpath = XPathFactory.newInstance().newXPath().compile(root);
             final NodeList nodeList = (NodeList) xpath.evaluate(doc, XPathConstants.NODESET);

             String uuidString;
             for (int t = 0; t < nodeList.getLength(); t++) {
                 uuidString = attibuteName.isEmpty() ? nodeList.item(t).getTextContent() : getNodeAttribute(nodeList.item(t), attibuteName);
                 if (! uuidString.isEmpty() ) {
                     res.add(uuidString);
                 }
             }
        } catch (XPathExpressionException e) {
            LOG.debug("XML XPath problem: "+e.getMessage());
        }
        return res;
    }

    /**
     * See {getXmlAttributes(Document, String, String)
     */
    private static HashSet<String> getXmlAttributes(Document doc, String root) {
        return getNodeIds(doc, root, EMPTY);
    }


    /**
     * Deliver if in both lists
     * @param indexListFirst list1 input
     * @param indexListSecond list2 input
     * @return All index matching criteria
     */
    private static HashSet<String> getStringInBoth(HashSet<String> indexListFirst, HashSet<String> indexListSecond) {
        HashSet<String> res = new HashSet<>();
        for (String value1 : indexListFirst) {
            for (String value2 : indexListSecond) {
                if (value1.equals(value2)) {
                    res.add(value1);
                    continue;
                }
            }
        }
        return res;
    }

    /**
     * Deliver if in first, but not in second
     * @param indexListFirst list1 input
     * @param indexListSecond list2 input
     * @return All index matching criteria
     */
    private static HashSet<String> getStringInFirstNotInSecond(HashSet<String> indexListFirst, HashSet<String> indexListSecond) {
        HashSet<String> res = new HashSet<>();
        boolean found;
        for (String value1 : indexListFirst) {
            found = false;
            for (String value2 : indexListSecond) {
                if (found = value1.equals(value2)) {
                    break;
                }
            }
            if (!found) {
                res.add(value1);
            }
        }
        return res;
    }

    /**
     * Add a new child to a node
     * @param destination node to be appended
     * @param newChild element to add
     * @param idx null or index information for console output
     */
    private static void appendChild(Node destination, Node newChild, String idx, Console console) {

        StringBuffer msg = new StringBuffer();
        msg.append(CONSOLEPREFIX1);
        msg.append("append "+newChild.getNodeName()+" to "+destination.getNodeName());
        if (idx != null) {
            msg.append(" idx["+idx+"]");
        }

        LOG.info( console.cliOutput(msg.toString()) );
        Node copy = newChild.cloneNode(true);

        destination.appendChild(copy);
    }

    /**
     * Replace a node by a node
     * @param destination parent node if child.
     * @param child has to be replaced
     * @param newChild replace with this content
     * @param idx null or index information for console output.
     */
    private static void replaceChild(Node destination, Node child, Node newChild, String idx, Console console) {

        StringBuffer msg = new StringBuffer();
        msg.append(CONSOLEPREFIX1);
        msg.append("replace "+newChild.getNodeName()+" to "+destination.getNodeName());
        if (idx != null) {
            msg.append(" idx["+idx+"]");
        }
        LOG.info( console.cliOutput(msg.toString()));

        Node copy = newChild.cloneNode(true);
        destination.replaceChild(copy, child);
    }


    /**
     * replace or create a child node by a new node.
     * Only nodes with UUID and of the type ELEMENT_NODE are effected.
     * @param destination has the effected attributes
     * @param newChild is the new attribute that replaces an existing one or is added.
     */
    private static void replaceChild(Node destination, Node newChild, Console console ) {

        if (newChild.getNodeType() == Node.ELEMENT_NODE) {

            String newChildUuidString = getNodeAttribute(newChild, UUIDNAME);
            if (!newChildUuidString.isEmpty()) {

                NodeList destinationChilds = destination.getChildNodes();
                String destinationUuidString = EMPTY;
                boolean found = false;
                Node child = null;

                for (int i=0; i < destinationChilds.getLength(); i++ ) {
                    destinationUuidString = getNodeAttribute(child = destinationChilds.item(i), UUIDNAME);
                    if (newChildUuidString.equals(destinationUuidString)) {
                        found = true;
                        break;
                    }
                }

                if (found) {
                    replaceChild(destination, child, newChild, newChildUuidString, console);

                } else {
                    appendChild(destination, newChild, newChildUuidString, console );
                }
            }
        }
    }

    /**
     * Replaces attributes of a destination node by new attribute entries in a source node
     * @param destination attributes to be replaced
     * @param source new attributes to replace existing ones or to be added
     */
    protected static void replaceChilds(Node destination, Node source, Console console) {

        for (Node sourceChild : getChildElementNodes(source)) {
            replaceChild(destination, sourceChild,console );
        }

    }


    /**
     * Get list of notifications to be produced.
     * @param mwNotifications contains list of specific notifications to be send out
     * @param myTransformer to map to xml string
     * @param notificationXmls List, to be extended with new notifications
     * @throws TransformerException Transforming problem
     */
    private void getNotifications(Node mwNotifications, Transformer myTransformer, List<String> notificationXmls) throws TransformerException {

        StringWriter sw;
        for (Node item : getChildElementNodes(mwNotifications)) {
            sw = new StringWriter();
            myTransformer.transform(new DOMSource(item), new StreamResult(sw));
            notificationXmls.add(assembleRpcNotification(sw.toString()));
        }
    }

    /**
     * Delete the object, referenced by xPath
     * @param deleteCommand Node with xpath as parameter
     * @throws XPathExpressionException
     */
    private static void deleteNode(Document doc, Node deleteCommand, Console console) throws XPathExpressionException {

        String path = deleteCommand.getTextContent();
        LOG.info("Try to delete: '"+path+"'");
        if (path != null && ! path.isEmpty()) {
            Node toBeDeleted = getNode(doc, path);

            if (toBeDeleted != null) {
                toBeDeleted.getParentNode().removeChild(toBeDeleted);
                LOG.info(console.cliOutput(CONSOLEPREFIX1+" deleted: '"+path+"'"));
            } else {
                LOG.info("Node to delete not found by xpath: '"+path+"'");
            }
        } else {
            LOG.warn("Delete with xpath null or empty: "+doc.getBaseURI());
        }
    }

    /**
     * Read file into String
     * @param path filename
     * @return String with content
     * @throws IOException if problem during read.
     */
    private static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    /**
     * Generate filename for YANG file using idendifier and revision date
     * @param schemaPath
     * @param schema with schema 'identifier' and 'version' date
     * @return filename of schema or empty.
     */
    private static String getFilenameFromNode( String schemaPath, Node schema ) {

        if (schema != null) {
            return schemaPath + "/"+getNodeAttribute(schema, "identifier")+"@"+getNodeAttribute(schema, "version")+".yang";
        } else {
            return EMPTY;
        }

    }

    /*-----------------------------------------------------------------------
     * Public functions to processing of messages and modify database
     */

    /**
     * Modify data in document. Insert new or update object in document.
     * @param myDoc Document with ltp to change
     * @param node Document with command and objects to modify
     * @param myTransformer conversion to string
     * @param notificationXmls contains xml Strings with all created notifications
     * @throws XPathExpressionException Wrong xpath
     * @throws TransformerException
     */
    private void processDynamic(Document myDoc, String idx, Transformer myTransformer, List<String> notificationXmls, Console myConsole) throws XPathExpressionException, TransformerException {

        Node node = getNode(myDoc, idx);

        if (node == null) {
            LOG.warn( myConsole.cliOutput("Can not find any action for node index: '"+idx+"'"));
            return;
        }

        for (Node item : getChildElementNodes(node)) {
            //System.out.println("--> Processing node: "+item.getNodeName());

            if (item.getNodeName().equals("network-element")) {
                // Exchange / create LTPs already done in earlier step. Ignore here
                replaceChilds( getNode(myDoc, "//data/network-element"), item, myConsole );

            } else if (item.getNodeName().equals("mw-notifications")) {
                // Generate Notification .. Ignore here
                getNotifications( item, myTransformer, notificationXmls);

            } else if (item.getNodeName().equals("delete")) {
                // Delete from doc
                deleteNode(myDoc, item, myConsole);

            } else {
                // Create remaining objects
                appendChild(getNode(myDoc, "//data"), item, null, myConsole);

            }
        }
    }


    /**
     * Create a hash list with a attributes content hash.
     * @param list with nodes
     * @param result Output list of nodes converted to xml strings
     * @param attributes
     *                 if null or empty     the index is used as key
     *                 concatenated content of attributes
     * @return list appended by matching attributes
     */
    protected HashMap<String, String> getAttributeHashList(Transformer myTransformer, NodeList list, HashMap<String, String> result, String ... attributes)
    {
        Element e=null;
        StringBuffer key;
        StringWriter sw;
        String keyString;

        int baseIdx = result.size();

        for(int i=0;i<list.getLength();i++)
        {
            Node n=list.item(i);
            if(n.hasAttributes() && n instanceof Element)
            {
                e=(Element)n;
                 key = new StringBuffer();

                if (attributes != null) {
                    for (String attribute : attributes) {
                        if(e.hasAttribute(attribute) && !e.getAttribute(attribute).isEmpty()) {
                            key.append(e.getAttribute(attribute));
                        }
                    }
                }
                if (key.length() == 0) {
                    key.append(baseIdx+i);
                }

                sw = new StringWriter();
                try {
                   myTransformer.transform(new DOMSource(n), new StreamResult(sw));
                   result.put(keyString = key.toString(), sw.toString());
                   consoleMessage("INIT Notification: "+(baseIdx+i)+" "+e.getNodeName()+" '"+keyString+"'  "+e.getNodeType());
               } catch (TransformerException e1) {
                   LOG.debug("XML Transformer problem: "+e1.getMessage());
                }
            }
        }
        return result;
    }

    /**
     * Add all notifications from subtree
     * @param attributes that are used as key
     * @param xPathList with a list of xPath for Notifications
     * @return List with all elements
     */
    protected HashMap<String, String> addNotifications(String[] attributes, String...xPathList) {

        HashMap<String, String> res = this.xmlSubTreeNotifications;
        for (String xPath : xPathList ) {
            LOG.debug("start find subtree "+xPath);
            NodeList nodeList = getXmlSubTree(doc, xPath);
            getAttributeHashList(transformer, nodeList, res, attributes);
            LOG.debug("finished find subtree "+xPath );
        }
        return res;
    }

    /**
     * List all notifications to console
     */
    public void doListNotifications() {
        consoleMessage("Lists of problems and changes");
        int idx = 0;
        for (Entry<String, String> entry : xmlSubTreeNotifications.entrySet()) {
            consoleMessage("["+idx+"]Key:"+entry.getKey()+" Content: "+entry.getValue());
            idx++;
        }

    }

    /**
     * User action for sending notification
     * @param command 'l' to list all notifications or number to send related notification
     * @return XML content or null
     */
    public String doProcessUserAction(String command) {

        LOG.info("-- User action notification start -- Command: '"+command+"'");
        /*
        List<String> xmlSubTreeProblems = getXmlSubTreeAsStringList("//mw-notifications/problem-notification");
        xmlSubTreeProblems.addAll(getXmlSubTreeAsStringList("//MW_Notifications/ProblemNotification"));
        List<String> xmlSubTreeChanges = getXmlSubTreeAsStringList("//mw-notifications/attribute-value-changed-notification");
        xmlSubTreeChanges.addAll(getXmlSubTreeAsStringList("//MW_Notifications/AttributeValueChangedNotification"));
        */
        List<String> xmlSubTreeNotificationsList  =  new ArrayList<>(xmlSubTreeNotifications.values());

        int idx = 0;
        if (command.startsWith("x")) {

            listNodesToConsole(doc, "//data", console);

        } else if (command.startsWith("l")) {

            doListNotifications();

         } else {

            try {
                idx = Integer.parseInt(command.trim());
                String xmlSubTree = null;

                if (idx < xmlSubTreeNotificationsList.size()) {
                    xmlSubTree = xmlSubTreeNotificationsList.get(idx);
                }

                if (xmlSubTree != null ) {
                    return assembleRpcNotification(xmlSubTree);
                }
            } catch (NumberFormatException e) {
                consoleMessage("Please enter a number.");
            }
        }
           LOG.info("-- -- User action notification end --");
        return null;
    }


    /*-----------------------------------------------------------------------
     * Functions to create READ message content to deliver answers back to the SDN controller.
     */

    /***
     * Execute NETCONF 'edit-config'-command against documents network-element.
     * Verify all LTS by uuid ->
     *      if exists in both lists .. do nothing
     *       if exists in command and not in document .. check if instructions available .. if yes do create according to instructions.
     *       if exists in document ..  check if instructions available .. if yes do remove according to instructions.
     * @param xml String with complete message
     */

    public List<String> assembleEditConfigElementReplyList(String sessionId, NetconfTagList tags, String xml) {

        LOG.info("Start processing of edit-config");

        List<String> res = new ArrayList<>();

        try {
            final Document inDoc = loadXMLFromString(xml);

            //Create the first list with the object status
            HashSet<String> firstList;
            firstList = getNodeIds(doc, "//data/network-element/ltp",UUIDNAME);
            firstList.addAll(getNodeIds(doc, "//data/network-element/fd",UUIDNAME));
            firstList.addAll(getXmlAttributes(doc, "//data/network-element/fd/fc"));

            //Create the second list with the goal to reach
            HashSet<String> secondList;
            secondList = getNodeIds(inDoc, "//network-element/ltp",UUIDNAME);
            secondList.addAll(getNodeIds(inDoc, "//network-element/fd",UUIDNAME));
            secondList.addAll(getXmlAttributes(inDoc, "//network-element/fd/fc"));

            //Debug purpose
            LOG.info("List1 doc: "+listToString(firstList));
            LOG.info("List2 doc: "+listToString(secondList));

            //Not in first, but in second => Create
            for ( String idx : getStringInFirstNotInSecond( secondList, firstList ) ) {
                LOG.info(consoleMessage("Create action: "+idx));
                processDynamic(doc, "//"+idx+"/create", transformer, res, console);
            }

            //In first, and in second => Update (Not implemented)
            for ( String idx : getStringInBoth( firstList, secondList ) ) {
                LOG.info("Update: "+idx+" (do not change anything)");
            }

            //In first, but not in second => Remove (Not implemented)
            for ( String idx : getStringInFirstNotInSecond( firstList, secondList ) ) {
                LOG.info(consoleMessage("Remove action: "+idx));
                processDynamic(doc, "//"+idx+"/remove", transformer, res, console);
            }

        } catch (Exception e) {
            LOG.error("(..something..) failed", e);
        }

        //Add positive reply at very beginning
        res.add(0, assembleRpcReplyOk(sessionId));

        return replaceAndWash(res);
    }

    /**
     * Send hello Answer
     * @param sessionId of message message
     * @return xml String with result
     */
    public String assembleHelloReply(String sessionId) {

        String xmlSubTree = getXmlSubTreeAsString("//capabilities");
        StringBuffer res = new StringBuffer();
        res.append("<hello xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n");
        res.append(xmlSubTree);
        appendXml(res, "session-id", sessionId == null ? "1" : sessionId);
        res.append("</hello>");
        return replaceAndWash(res.toString());

    }
    /**
     * Write the document into a file.
     * @param filename The output filename
     */
    public void writeXml(String filename) {

    	LOG.info("Starte write "+filename);

        // Mark the document as a DOM (XML) source
        DOMSource source = new DOMSource(doc);

        // Say where we want the XML to go
        StreamResult result = new StreamResult(new File(filename));

        // Write the XML to file
        try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			e.printStackTrace();
		}
    	LOG.info("Done writing "+filename);

    }
    /**
     * Deliver the YANG files for the requested schema
     * @param sessionId for generation an answer
     * @param xml from received message
     * @return answer xml
     */
    public String assembleGetSchemaReply(String sessionId, String xml) {

        LOG.info("Start processing of get-schema");

        StringBuffer res;

        try {
            final Document inDoc = loadXMLFromString(xml);

            Node schema = getNode(inDoc, "//get-schema");
            if (schema != null) {

                String fileName = getFilenameFromNode(schemaPath, schema);
                consoleMessage("Load schema: "+fileName);
                String yang = readFile(fileName);
                //Assemble message
                res = new StringBuffer();
                res.append("<rpc-reply message-id=\""+sessionId+"\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n");
                res.append("<data xmlns=\"urn:ietf:params:xml:ns:yang:ietf-netconf-monitoring\">\n");
                res.append("<![CDATA[");
                res.append(yang);
                res.append("]]>");
                res.append("\n</data>\n");
                appendXmlMessageRpcReplyClose(res);
            } else {
                LOG.warn("Can not find get-schema node");
                res = assembleRpcReplyError("invalid-value", "Can not find get-schma node");
            }
        } catch (Exception e) {

            LOG.warn("(..something..) failed", e);
            res = assembleRpcReplyError("invalid-value", e.getMessage());
       }
       return replaceAndWash(res.toString());
    }

    /**
     * Send Reply with no data, normally after get-config request
     * @param id of message message
     * @return xml String with result
     */
    public String assembleRpcReplyEmptyData(String id) {
        StringBuffer res = new StringBuffer();
        appendXmlMessageRpcReplyOpen(res, id);
        res.append("<data/>\n");
        appendXmlMessageRpcReplyClose(res);
        return replaceAndWash(res.toString());

    }

    /**
     * Send rcp-reply empty data reply with ok
     * @param id of message message
     * @return xml String with result
     */
    public String assembleRpcReplyEmptyDataOk(String id) {

        StringBuffer res = new StringBuffer();
        appendXmlMessageRpcReplyOpen(res, id);
        res.append("<data/>\n");
        res.append("<ok/>\n");
        appendXmlMessageRpcReplyClose(res);
        return replaceAndWash(res.toString());

    }

    /**
     * Send rcp-reply with ok
     * @param id of message message
     * @return xml String with result
     */
    public String assembleRpcReplyOk(String id) {

        StringBuffer res = new StringBuffer();
        appendXmlMessageRpcReplyOpen(res, id);
        res.append("<ok/>\n");
        appendXmlMessageRpcReplyClose(res);
        return replaceAndWash(res.toString());
    }


    /**
     * Assemble rpc-reply message from model according to paramers
     * @param id    MessageId for answer
     * @param name Name of root element of subtree
     * @param namespace Namespace of root elemenet
     * @param xmlSubTree xml-data with requested subtree
     * @return xml String with rpc-reply message
     */
    public String assembleRpcReplyMessage(String id, String name, String namespace, String xmlSubTree) {
        StringBuffer res = new StringBuffer();
        appendXmlMessageRpcReplyOpen(res, id);
        res.append("<data/>\n");
        res.append(xmlSubTree.replaceFirst(name, name+" xmlns=\""+namespace+"\""));
        res.append("</data>\n");
        appendXmlMessageRpcReplyClose(res);
        return replaceAndWash(res.toString());
    }

    /**
     * Assemble rpc-reply message from model according to paramers
     * @param id    MessageId for answer
     * @param tags list with element for filter information
     * @return xml String with rpc-reply message
     */
   public String assembleRpcReplyFromFilterMessage(String id, NetconfTagList tags) {
        if (tags.isEmtpy() ) {

            return assembleRpcReplyEmptyData(id);

        } else {

            String xmlSubTreePath = tags.getSubTreePath();
            LOG.info("Subtreepath="+xmlSubTreePath);
            String xmlSubTree = getXmlSubTreeAsString("//data/"+xmlSubTreePath);

            StringBuffer res = new StringBuffer();
            appendXmlMessageRpcReplyOpen(res, id);
            res.append("<data>\n");
            recuresAddContent(tags, res, xmlSubTree, 0 );
            res.append("</data>\n");
            appendXmlMessageRpcReplyClose(res);
            if(LOG.isTraceEnabled())
            	LOG.trace(res.toString());
            return replaceAndWash(res.toString());
        }
    }

   /**
    * Add content according to tag sequence
    * @param tags tags of received message
    * @param sb filled with new answer message content
    * @param content XML content that has to be delivered back
    * @param idx parameter doing the recursion. Start from 0 and increasing
    */
   private void recuresAddContent(NetconfTagList tags, StringBuffer sb, String content,int idx) {

       if (idx < tags.size()) {
           //Get akt
           NetconfTag tag = tags.getList().get(idx);
           NetconfTag tagidx = null;
           boolean last;
           if (idx + 1 < tags.size()) {
               tagidx = tags.getList().get(idx+1);
               if (!tagidx.hasOneValue()) {
                   tagidx = null; //No index
                   last = false;
               } else {
                   last = !(idx + 2 < tags.size());
               }
           } else {
               last = true;
           }

           if (last) {
               //Leaf
               sb.append(content.replaceFirst(tag.getName(), tag.getName()+" xmlns=\""+tag.getNamespace()+"\""));

           } else {
               //Wrapper
               //Wrap Begin
               if (!tag.hasOneValue()) {
                   appendXmlTagOpen( sb, tag.getName(), tag.getNamespace() );
                   recuresAddContent(tags, sb, content, idx+1 );
                   //Wrap1 End
                   appendXmlTagClose(sb, tag.getName());
               } else {
                   appendXml( sb, tag.getName(), tag.getValue());
                   recuresAddContent(tags, sb, content, idx+1 );
               }
           }
       }
   }

   private static Element findChildElementByName(final NodeList list, final String nodeName) {
        Element node = null;
        for (int j = 0; j < list.getLength(); j++) {
            if (list.item(j).getNodeType() == Node.ELEMENT_NODE && list.item(j).getNodeName().equals(nodeName)) {
                node = (Element) list.item(j);
                break;
            }
        }
        return node;
    }
   /*
     * returns <current-problem-list> Node in which <problem-name> value equals
     * param 'problemName'
     */
    private static Element findProblemNode(final NodeList list, final String cplName, final String pnName,
            String problemName, boolean withCleared) {
        int i;
        Element node = null;
        Element nodeName = null;
        if (problemName != null) {
            if (withCleared) {
                int idx = problemName.indexOf("Cleared");
                if (idx > 0) {
                    problemName = problemName.substring(0, idx);
                }
            }
            for (i = 0; i < list.getLength(); i++) {
                if (list.item(i).getNodeType() == Node.ELEMENT_NODE && list.item(i).getNodeName().equals(cplName)) {
                    NodeList children = list.item(i).getChildNodes();
                    nodeName = findChildElementByName(children, pnName);
                    if (nodeName != null && nodeName.getTextContent().equals(problemName)) {
                        node = (Element) list.item(i);
                        break;
                    }
                }
            }
        }
        return node;
    }
   private static Element findProblemNode10(final NodeList list, String problemName) {
        return findProblemNode10(list, problemName, false);
    }

    private static Element findProblemNode10(final NodeList list, String problemName, boolean withCleared) {
        return findProblemNode(list, "currentProblemList", "problemName", problemName, withCleared);
    }

    private static Element findProblemNode12(final NodeList list, String problemName) {
        return findProblemNode12(list, problemName, false);
    }

    private static Element findProblemNode12(final NodeList list, String problemName, boolean withCleared) {
        return findProblemNode(list, "current-problem-list", "problem-name", problemName, withCleared);
    }

    /**
     * Add a dynamic problem list
     * @param key
     * @param n from xml document
     */
    public void addProblemList(String key,Node n) {
        this.mProblemLists.put(key, n);
    }

    public boolean removeFromProblemList12(final Node root, String problemName, boolean withCleared) {

        if (root == null) {
            return false;
        }
        NodeList list = root.getChildNodes();
        Element node = findProblemNode12(list, problemName, withCleared);
        if (node != null) {
            root.removeChild(node);
            return true;
        }
        return false;
    }

    public boolean removeFromProblemList10(final Node root, String problemName, boolean withCleared) {

        if (root == null) {
            return false;
        }
        NodeList list = root.getChildNodes();
        Element node = findProblemNode10(list, problemName, withCleared);
        if (node != null) {
            root.removeChild(node);
            return true;
        }
        return false;
    }
    private boolean addToProblemList10(final Node root, String problemName, String problemSeverity, String timestamp,
            String objectRefId, String counter) {
        LOG.debug(String.format("try to add problem %s with sev=%s to node %s", problemName, problemSeverity,
                root == null ? "null" : root.getNodeName()));
        if (root == null) {
            return false;
        }
        NodeList list = root.getChildNodes();

        // search for <urrent-problem-list> Node with problemName
        Element node = findProblemNode10(list, problemName);
        if (problemSeverity.equals("non-alarmed")) {
            if (node != null) {
                root.removeChild(node);
            }
        } else {
             if (node == null) {
                node = doc.createElement("currentProblemList");
                Node seq = doc.createElement("sequenceNumber");
                seq.setTextContent(String.format("%d", list.getLength() + 1));
                Node name = doc.createElement("problemName");
                name.setTextContent(problemName);
                Node sev = doc.createElement("problemSeverity");
                sev.setTextContent(problemSeverity);
                Node ts = doc.createElement("timeStamp");
                ts.setTextContent(timestamp);
                node.appendChild(seq);
                node.appendChild(name);
                node.appendChild(sev);
                node.appendChild(ts);
                if(objectRefId!=null)
                {
                    Node objid = doc.createElement("objectIdRef");
                    objid.setTextContent(objectRefId);
                    node.appendChild(objid);
                }
                if (counter != null) {
                    Node c = doc.createElement("counter");
                    c.setTextContent(counter);
                    node.appendChild(c);
                }
                root.appendChild(node);
                LOG.debug("problem added");
            } else // refresh data
            {
                LOG.debug("problem already exists");
                if (this.configRefreshTimestamp) {
                    LOG.debug("time-stamp will be refreshed");
                    Node ts = findChildElementByName(node.getChildNodes(), "timeStamp");
                    if (ts != null) {
                        ts.setTextContent(timestamp);
                    }
                }
            }

        }
        return true;
    }
    public boolean addToProblemList12(String key, String problemName, String problemSeverity, String timestamp,
            String objectRefId, String counter) {

        final Node root = this.mProblemLists.getOrDefault(key,null);
        LOG.debug(String.format("try to add problem %s with sev=%s to node %s", problemName, problemSeverity,
                root == null ? "null" : root.getNodeName()));
        if (root == null) {
            return false;
        }
        return this.addToProblemList12(root, problemName, problemSeverity, timestamp, objectRefId, counter);
    }
    public boolean addToProblemList12(Node root, String problemName, String problemSeverity, String timestamp,
            String objectRefId, String counter) {

        NodeList list = root.getChildNodes();
        LOG.debug("try to add "+problemName+" sev="+problemSeverity+" ts="+timestamp+" ref="+objectRefId+" count="+counter);
        // search for <urrent-problem-list> Node with problemName
        Element node = findProblemNode12(list, problemName);
        if (problemSeverity.equals("non-alarmed")) {
        	LOG.debug("sev=non-alarmed. remove from list");
            if (node != null) {
                root.removeChild(node);
            }
        } else {
            
            if (node == null) {
            	LOG.debug("create xml node for problem");
                node = doc.createElement("current-problem-list");
                Node seq = doc.createElement("sequence-number");
                seq.setTextContent(String.format("%d", list.getLength() + 1));
                Node name = doc.createElement("problem-name");
                name.setTextContent(problemName);
                Node sev = doc.createElement("problem-severity");
                sev.setTextContent(problemSeverity);
                Node ts = doc.createElement("time-stamp");
                ts.setTextContent(timestamp);
                node.appendChild(seq);
                node.appendChild(name);
                node.appendChild(sev);
                node.appendChild(ts);
                if (objectRefId != null) {
                    Node objid = doc.createElement("object-reference");
                    objid.setTextContent(objectRefId);
                    node.appendChild(objid);
                }
                if (counter != null) {
                    Node c = doc.createElement("counter");
                    c.setTextContent(counter);
                    node.appendChild(c);
                }
                root.appendChild(node);
                
                LOG.debug("problem added");
            } else // refresh data
            {
                LOG.debug("problem already exists");
                if (this.configRefreshTimestamp) {
                    LOG.debug("time-stamp will be refreshed");
                    Node ts = findChildElementByName(node.getChildNodes(), "time-stamp");
                    if (ts != null) {
                        ts.setTextContent(timestamp);
                    }
                }
            }

        }
        return true;
    }

    public  boolean removeFromProblemList(String key,String problemName) {
        return this.removeFromProblemList(key, problemName, false);
    }

    public  boolean removeFromProblemList(String key,String problemName, boolean withCleared) {
        return this.removeFromProblemList12(this.mProblemLists.getOrDefault(key,null), problemName, withCleared);
    }

    /**
     * @param problemlistKeyNetworkelement
     * @param string
     * @return
     * @throws XPathExpressionException
     */
    public boolean addProblemList(String problemlistKeyNetworkelement, String string) throws XPathExpressionException {
        Node node = NetworkElement.getNode(doc, string);
        if (node != null) {
            addProblemList(problemlistKeyNetworkelement, node);
            return true;
        } else {
            return false;
        }
    }


    //-Message queue -------------------------------------------
    public void addExternalMessageQueue(MessageQueue messageQueue) {
        if(!this.externalMessageQueueList.contains(messageQueue)) {
            this.externalMessageQueueList.add(messageQueue);
        }
    }
    public void removeExternalMessageQueue(MessageQueue messageQueue)
    {
        this.externalMessageQueueList.remove(messageQueue);
    }
    public void pushToExternalMessageQueue(RPCElement e)
    {
        for(MessageQueue q: this.externalMessageQueueList) {
            q.put(e);
        }
    }

    protected Node createNode(String nodeName)
    {
    	return this.createNode(nodeName,null);
    }
    protected Node createNode(String nodeName,String nodeValue) {
    	Node n=this.doc.createElement(nodeName);
    	if(nodeValue!=null)
    		n.appendChild(this.doc.createTextNode(nodeValue));
    	return n;
    }
  
    public String detectXMLVersion() {
       return detectXMLVersion(this.doc);
    }
    public Node getNode(String filter) throws XPathExpressionException
    {
    	return getNode(this.doc, filter);
    }
    public NodeList getXmlSubTree(String subtree)
    {
    	return getXmlSubTree(this.doc, subtree);
    }
	public static List<Node> getSubNode(Node root, String nodeName) {
		List<Node> nlist=new ArrayList<Node>();
		NodeList children = root.getChildNodes();
		for(int i=0;i<children.getLength();i++)
		{
			Node child=children.item(i);
			if(child.getNodeName().equals(nodeName))
				nlist.add(child);
		}
		return nlist;
	}

	public static String getSubNodeValue(Node root, String nodeName) {
		List<Node> subnodes=getSubNode(root,nodeName);
		if(subnodes!=null && subnodes.size()>0)
		{
			Node n=subnodes.get(0);
			NodeList childs=n.getChildNodes();
			for(int i=0;i<childs.getLength();i++)
			{
				if(childs.item(i) instanceof Text)
					return childs.item(i).getTextContent();
			}
		}
		return null;
	}
	public static void clearSubNodes(Node root)
	{
		while (root.hasChildNodes())
		    root.removeChild(root.getFirstChild());
	}
	  public static String detectXMLVersion(String filename) {
	        Document d = null;
	        File file = new File(filename);

	        FileInputStream fis = null;
	        try {
	            fis = new FileInputStream(file);
	        } catch (FileNotFoundException e1) {}
	        if (fis != null) {
	            try {
	                d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fis);
	            } catch (Exception e) {}
	            try {
	                fis.close();
	            } catch (IOException e) {}
	        }
	        return detectXMLVersion(d);
	    }

	    public static String detectXMLVersion(Document doc) {
	        String version = "0.0.0";
	        if (doc != null) {
	            try {
	                Node n = getNode(doc, "//data/version");
	                if(n!=null) {
	                    version = n.getTextContent();
	                }
	            } catch (Exception e) {
	            }
	        }
	        return version;
	    }

		public static void clearSubNodes(NodeList nodes) {
			for(int i=0;i<nodes.getLength();i++)
				clearSubNodes(nodes.item(i));
		}

		public Document getDocument() {
			// TODO Auto-generated method stub
			return this.doc;
		}


}
