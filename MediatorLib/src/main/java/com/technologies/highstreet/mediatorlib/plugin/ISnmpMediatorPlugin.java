package com.technologies.highstreet.mediatorlib.plugin;

import org.w3c.dom.Document;

import com.technologies.highstreet.mediatorlib.netconf.server.basetypes.SnmpKeyValuePairList;
import com.technologies.highstreet.mediatorlib.netconf.server.networkelement.NodeEditConfigCollection;
import com.technologies.highstreet.mediatorlib.netconf.server.types.NetconfTagList;

public interface ISnmpMediatorPlugin  {

    void onPreInit();
    void onPostInit();
    void onPreRequest(String messageId, NetconfTagList tags, NodeEditConfigCollection nodes);
    void onPostRequest(String messageId, NetconfTagList tags, NodeEditConfigCollection nodes);
    void onPreEditRequest(String messageId, NetconfTagList tags, Document sourceMessage);
    void onPostEditRequest(String messageId, NetconfTagList tags, Document sourceMessage);
    void onClose();
    void onDeviceConnectionStatusChanged(int before,int now);
    /**
     * Handle received traps
     * @param traps contains traps
     * @return true if traps are handled
     */
    boolean onTrapReceived(SnmpKeyValuePairList traps);
    String getVersion();
}
