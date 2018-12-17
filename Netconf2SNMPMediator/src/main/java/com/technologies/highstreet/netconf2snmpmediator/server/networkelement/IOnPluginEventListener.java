package com.technologies.highstreet.netconf2snmpmediator.server.networkelement;

import org.w3c.dom.Document;

import com.technologies.highstreet.mediatorlib.netconf.server.networkelement.NodeEditConfigCollection;
import com.technologies.highstreet.mediatorlib.netconf.server.types.NetconfTagList;

public interface IOnPluginEventListener {

	void onPreRequest(String messageId, NetconfTagList tags, NodeEditConfigCollection nodes);

	void onPostRequest(String messageId, NetconfTagList tags, NodeEditConfigCollection nodes);

	void onPreEditRequest(String messageId, NetconfTagList tags, Document sourceMessage);

	void onPostEditRequest(String messageId, NetconfTagList tags, Document sourceMessage);

}
