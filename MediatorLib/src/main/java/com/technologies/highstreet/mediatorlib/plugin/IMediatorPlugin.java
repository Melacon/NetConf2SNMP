package com.technologies.highstreet.mediatorlib.plugin;

import org.w3c.dom.Document;

import com.technologies.highstreet.mediatorlib.netconf.server.types.NetconfTagList;

public interface IMediatorPlugin {

	void onPreInit();
	void onPostInit();
	void onPreRequest(String messageId, NetconfTagList tags);
	void onPostRequest(String messageId, NetconfTagList tags);
	void onPreEditRequest(String messageId, NetconfTagList tags,Document sourceMessage);
	void onPostEditRequest(String messageId, NetconfTagList tags,Document sourceMessage);
	void onClose();
	void onDeviceConnectionStatusChanged(int before,int now);
	String getVersion();

}
