package com.technologies.highstreet.mediatorlib.netconf.server.basetypes;

import net.i2cat.netconf.rpc.RPCElement;

/**
 * Mixture of Constants used somewhere in the code, propably related to specific SNMP Implementation. TODO Verify and shift to related place
 * Class contains result of SNMP Trap parsing that took place somewhere else .. TODO: Consolidate to MIB/Device related place
 * Class contains conversion top a netconf format ... TODO: Use the same definition in network-element class
 * (Comment by Herbert)
 *
 * @author Micha
 */

public class SnmpTrapNotification extends RPCElement{

    private static final long serialVersionUID = 3918311811706099401L;
    private static final String CLSNAME = SnmpTrapNotification.class.getName();
    public static final int TYPE_MICROWAVE = 0;
    public static final int TYPE_ETHERNET = 1;
    public static final int TYPE_NETWORKELEMENT = 2;

    /**
     * TODO ... move to the one place that contains netcon related definition for alarm severties close to network-element
     */
    public static final String SEVERITY_NON_ALARMED = "non-alarmed";
    public static final String SEVERITY_CRITICAL = "critical";
    public static final String SEVERITY_MAJOR = "major";
    public static final String SEVERITY_MINOR = "minor";
    public static final String SEVERITY_WARNING = "warning";
//    public static final String NOCURRENTPROBLEMINDICATION = "#";

    private String timeStamp;
    private final String severity;
    private String counter;
    private final String problemName;
    private final String objectIdReference;
    private final int type;
    private static int messageId;

    public void setCounter(String c) {this.counter=c;}
    public void setTimeStamp(String ts) {this.timeStamp=ts;}

    public SnmpTrapNotification(String name,String ref,String sev)
    {
        this.problemName=name;
        this.objectIdReference=ref;
        this.severity=sev;
        this.type=this.findType();
        this.setMessageId(CLSNAME+String.valueOf((messageId++)&0x3fffffff));
    }

    private int findType() {
        if(this.objectIdReference!=null)
        {
            if(this.objectIdReference.equals("LP-MWTN-TTP-Radio")) {
                return TYPE_MICROWAVE;
            }
            if(this.objectIdReference.equals("System")) {
                return TYPE_NETWORKELEMENT;
            }
            if(this.objectIdReference.equals("LTP-ETC-06-LP-1")) {
                return TYPE_ETHERNET;
            }
        }
        return TYPE_MICROWAVE;
    }
    @Override
    public String toXML()
    {
        return String.format("<problem-notification xmlns=\"urn:onf:params:xml:ns:yang:microwave-model\">"+
                "<counter>%s</counter>"+
                "<time-stamp>%s</time-stamp>"+
                "<problem>%s</problem>"+
                "<severity>%s</severity>"+
                "<object-id-ref>%s</object-id-ref>"+
                "</problem-notification>", this.counter,this.timeStamp,/*NOCURRENTPROBLEMINDICATION+*/this.problemName,this.severity,this.objectIdReference);
    }
    public boolean isNonAlarmed() {
        return this.severity.equals(SEVERITY_NON_ALARMED);
    }
    public int getType(){
        return this.type;
    }
    public String getProblemName() {
        return this.problemName;
    }
    public String getSeverity() {
        return this.severity;
    }
    public String getTimestamp() {
        return this.timeStamp;
    }
    public String getCounter() {
        return this.counter;
    }
    public String getObjectIdReference() {
        return this.objectIdReference;
    }
    @Override
    public String toString() {
        return "SnmpTrapNotification [timeStamp=" + timeStamp + ", severity=" + severity + ", counter=" + counter
                + ", problemName=" + problemName + ", objectIdReference=" + objectIdReference + ", type=" + type + "]";
    }

}
