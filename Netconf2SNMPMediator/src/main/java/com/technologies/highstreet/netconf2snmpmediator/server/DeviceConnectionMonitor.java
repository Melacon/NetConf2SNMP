package com.technologies.highstreet.netconf2snmpmediator.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.technologies.highstreet.netconf.server.streamprocessing.NetconfStreamCodecThread;
import com.technologies.highstreet.netconf2snmpmediator.server.streamProcessing.MediatorConnectionListener;
import com.technologies.highstreet.netconf2snmpmediator.server.streamProcessing.SNMPDevicePollingThread;

public class DeviceConnectionMonitor {

    private final SNMPDevicePollingThread pollThread;
    private boolean pollAsThread;
    private static final Log LOG = LogFactory.getLog(DeviceConnectionMonitor.class);

    public DeviceConnectionMonitor(MonitoredNetworkElement ne, NetconfStreamCodecThread ioCodec,int altPingPort,MediatorConnectionListener connectionListener) {

        pollThread = SNMPDevicePollingThread.GetInstance(ne, ioCodec, altPingPort, false,connectionListener);
        if (!pollAsThread) {
            SNMPDevicePollingThread.scheduleAtFixedRate(pollThread,SNMPDevicePollingThread.PERIOD);
            LOG.info("device polling scheduler started with interval="+SNMPDevicePollingThread.PERIOD);

        } else
        {
            if(!pollThread.isAlive()) {
                pollThread.start();
            }
            LOG.info("device polling thread started");
        }
    }

    public void waitAndInterruptThreads() {

        if (pollThread == null) {
            return;
        }
        // wait for thread
        try {
            pollThread.trystop();
            pollThread.join(2000);
        } catch (InterruptedException e) {
            LOG.error("Error waiting for thread end: " + e.getMessage());
        }

        // kill thread if it don't finish naturally
        if (pollThread != null && pollThread.isAlive()) {
            LOG.info("Killing polling processor thread");
            pollThread.interrupt();
        }
    }
   

}
