package com.technologies.highstreet.netconf2snmpmediator.server.networkelement;

import com.technologies.highstreet.mediatorlib.netconf.server.basetypes.SnmpKeyValuePairList;

public interface IOnTrapReceivedListener {

    /**
     * Handle traps
     * @param trapList conatins the received traps
     * @return true if traps are handled and false if not handled
     */
    boolean onTrapReceived(SnmpKeyValuePairList trapList);
}
