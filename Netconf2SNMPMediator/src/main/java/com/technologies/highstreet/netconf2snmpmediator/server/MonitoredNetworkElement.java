/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.technologies.highstreet.netconf2snmpmediator.server;

import net.i2cat.netconf.rpc.RPCElement;

/**
 * @author herbert
 *
 */
public interface MonitoredNetworkElement {

    /**
     * @param b
     */
    void setRemoteDeviceConnected(boolean b);

    /**
     * @return
     */
    String getDeviceName();

    /**
     * @param problemName
     * @param problemSeverity
     * @param timeStamp
     * @param deviceName
     * @param valueOf
     */
    void addToProblemListNe(String problemName, String problemSeverity, String timeStamp, Object deviceName,
            String valueOf);

    /**
     * @param problemNotification
     */
    void pushToExternalMessageQueue(RPCElement problemNotification);

    /**
     * @param problemName
     */
    boolean removeFromProblemListNe(String problemName);

    /**
     * @return
     */
    String getDeviceIp();

}
