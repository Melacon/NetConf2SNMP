package com.technologies.highstreet.deviceslib.data;

import java.util.HashMap;

public class SNMPKeyValuePairCollection extends HashMap <String,SNMPKeyValuePair>{

	public void add(SNMPKeyValuePair item) {
		this.put(item.OID, item);
	}



}
