package com.technologies.highstreet.mediatorlib.netconf.server.basetypes;

import org.snmp4j.smi.Counter32;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.Variable;

public class SnmpKeyValuePair {

    private static final long serialVersionUID = 7469960234549882489L;

    public static final int TYPE_OCTETSTRING = 0;
    public static final int TYPE_INTEGER32 = 1;
    public static final int TYPE_TIMETICKS = 2;
    public static final int TYPE_IPADDR = 3;
/*
    public static final int VALUETYPE_STRING = 1;
	public static final int VALUETYPE_INT = 2;
	public static final int VALUETYPE_INETADR = 3;
	public static final int VALUETYPE_INETADR_HEXSTRING = 4;
	public static final int VALUETYPE_INT32 = 5;
	public static final int VALUETYPE_COUNTER32 = 6;
	public static final int VALUETYPE_GAUGE32 = 7;
	public static final int VALUETYPE_TICKS = 8;
	public static final int VALUETYPE_OID = 9;
*/
    private static int counter = 0;
    private final String oid;
    private final String value;
    private final int valueType;
    private final int myNumber;

    public SnmpKeyValuePair( String oid, String value ) {
        this.oid = oid;
        this.value = value;
        this.valueType=TYPE_OCTETSTRING;
        this.myNumber = counter++;
    }
    public SnmpKeyValuePair( String oid, int value ) {
        this.oid = oid;
        this.value = String.format("%d",value);
        this.valueType=TYPE_INTEGER32;
        this.myNumber = counter++;
    }
    public SnmpKeyValuePair(String oid, Variable value) {
        this.oid=oid;
        this.value = value.toString();
        this.valueType=this._getType(value);
        this.myNumber=counter++;
    }
    public SnmpKeyValuePair(String oid, String value, int type) {
		this.oid = oid;
		this.value = value;
		this.valueType = type;
		this.myNumber=counter++;
	}
	private int _getType(Variable value) {
        if(value instanceof Integer32 || value instanceof Counter32) {
            return TYPE_INTEGER32;
        } else if(value instanceof TimeTicks) {
            return TYPE_TIMETICKS;
        } else if(value instanceof org.snmp4j.smi.IpAddress) {
            return TYPE_IPADDR;
        }
        return TYPE_OCTETSTRING;
    }
    /**
     * @return the oid
     */
    public String getOid() {
        return oid;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @return the myNumber
     */
    public int getMyNumber() {
        return myNumber;
    }

    @Override
    public String toString() {
        return "SnmpKeyValuePair [oid=" + oid + ", value=" + value + ", valueType=" + valueType + ", myNumber="
                + myNumber + "]";
    }
    public boolean isInteger32() {
        return this.valueType==TYPE_INTEGER32;
    }
	public int getValueType() {
		return this.valueType;
	}





}
