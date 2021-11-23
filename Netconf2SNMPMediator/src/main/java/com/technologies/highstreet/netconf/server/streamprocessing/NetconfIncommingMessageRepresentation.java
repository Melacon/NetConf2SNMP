/**
 * Simple representation of incoming NETCONF message.
 *
 * The sequence of XML attributes is stored into a "tag" list.
 * This list is used to distinguish incoming messages and react accordingly.
 *
 * @author herbert.eiselt@highstreet-technologies.com
 */

package com.technologies.highstreet.netconf.server.streamprocessing;

import java.util.HashMap;

import com.technologies.highstreet.mediatorlib.netconf.server.types.NetconfTagList;

import net.i2cat.netconf.rpc.RPCElement;


public class NetconfIncommingMessageRepresentation extends RPCElement {

    private static final long serialVersionUID = 1887817430908834282L;
    private final static String NOTSET = "NotSet";

    private String xmlSourceMessage = NOTSET;

    private String messageType = NOTSET;
    private String messageUri = NOTSET;
    private final HashMap<String, String> content = new HashMap<String, String>();
    private final NetconfTagList tags = new NetconfTagList();
	private static int messageID=0;

    public NetconfIncommingMessageRepresentation()
    {
    	this.setMessageId("NetconfIncommingMessageRepresentation"+messageID++);
    }

    public void setXmlSourceMessage(String xmlSourceMessage) {
        this.xmlSourceMessage = xmlSourceMessage;
    }

    public String getXmlSourceMessage() {
        return xmlSourceMessage;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageUri() {
        return messageUri;
    }

    public void setMessageUri(String messageUri) {
        this.messageUri = messageUri;
    }

    public void addCollectionValue(int level, String value ) {

        value = value.trim();
        if (! (value.isEmpty() || value.equals("\n"))) {
            //used to manage list access
            //Method 1 add the value
            tags.addTagsValue(value);

            //Method 2 add the key and value
            String res = tags.getTagsPath();
            String key = res;
            int idx=1;
            while ( content.containsKey(key) ) {
            	//Generate a new unique key .. that is not in the content
                key = res + "." + String.valueOf(idx++);
            }
            content.put(key, value);
        }
    }

    public void pushTag(String tagName, String namespace) {
        tags.pushTag(tagName, namespace);
    }

    public NetconfTagList getFilterTags() {
        int filterPosition = tags.getTagPosition("filter");
        return tags.getSubsetFromIndex(filterPosition+1);
    }

    /*----------------------------------------------------------------
     * Filter configuration for specific messages, that are important
     * and processed by  the simulator
     */

    public boolean isHello() {
        return messageType.equals("hello");
    }

    public boolean isRpcGetFilter() {
        return messageType.equals("rpc") && tags.checkTags("get","filter");
    }

    public boolean isRpcGetConfigSourceRunningFilter() {
        return messageType.equals("rpc") && tags.checkTags("get-config","source","running", "filter");
    }

    public boolean isRpcEditConfigTargetRunningDefaultOperationConfig() {
        return messageType.equals("rpc") && tags.checkTags("edit-config","target","running","default-operation", "config");
    }

    public boolean isRpcLockTargetRunning() {
        return messageType.equals("rpc") && tags.checkTags("lock","target","running");
    }

    public boolean isRpcUnlockTargetRunning() {
        return messageType.equals("rpc") && tags.checkTags("unlock","target","running");
    }

    public boolean isRpcCreateSubscription() {
        return messageType.equals("rpc") && tags.checkTags("create-subscription");
    }

    public boolean isRpcGetSchema() {
        return messageType.equals("rpc") && tags.checkTags("get-schema");
    }

    /*----------------------------------------------------------------
     * Required but not really used
     */

    @Override
    public String toXML() {
        return NOTSET;
    }

    @Override
    public String toString() {
        return "RpcIn [messageType=" + messageType + ", tags="
                + tags + ", messageUri=" + messageUri + ", content=" + content + ", getMessageId()="
                + getMessageId() + ", getCtx()=" + getCtx() + ", xmlSourceMessage='" + xmlSourceMessage + "']";
    }


}
