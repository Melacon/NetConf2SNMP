/**
 *
 */
package com.technologies.highstreet.mediatorlib.netconf.server.basetypes;

import java.util.ArrayList;
import java.util.List;
import net.i2cat.netconf.rpc.RPCElement;

/**
 * @author herbert
 *
 */
public class SnmpKeyValuePairList  extends RPCElement {

     private static final long serialVersionUID = -1235647591803564718L;
     private static final String CLSNAME = SnmpKeyValuePairList.class.getName();
    private final List<SnmpKeyValuePair> traps = new ArrayList<SnmpKeyValuePair>();
	private static int messageId;

    public SnmpKeyValuePairList()
    {
    	this.setMessageId(CLSNAME+(messageId++));
    }
    public void add(SnmpKeyValuePair trap) {
        traps.add(trap);
    }

    public List<SnmpKeyValuePair> get() {
        return traps;
    }

    public SnmpKeyValuePair getValueFor(String oid,SnmpKeyValuePair def)
    {
    	if(oid.endsWith("*"))
    		return getValueStartsWith(oid.substring(0,oid.length()-1),def);

    	for(SnmpKeyValuePair item:this.traps)
    	{
    		if(item.getOid().equals(oid))
    			return item;
    	}
    	return def;
    }
    private SnmpKeyValuePair getValueStartsWith(String oid, SnmpKeyValuePair def) {
    	for(SnmpKeyValuePair item:this.traps)
    	{
    		if(item.getOid().startsWith(oid))
    			return item;
    	}
    	return def;
	}

	@Override
    public String toString() {
        return "SnmpTrapList [traps=" + traps + "]";
    }

	public SnmpKeyValuePair getIntValueFor(String oid, SnmpKeyValuePair def) {
		SnmpKeyValuePair x=this.getValueFor(oid, def);
		if(x==null)
			x=def;
		if(x!=null && x.isInteger32())
			return x;
		return def;
	}
	public SnmpKeyValuePair get(String oid) {
		return this.getValueFor(oid, null);
		}



}
