package com.technologies.highstreet.netconf2snmpmediator.server.streamProcessing;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.technologies.highstreet.netconf.server.streamprocessing.NetconfStreamCodecThread;
import com.technologies.highstreet.netconf.server.streamprocessing.ProblemNotification;
import com.technologies.highstreet.netconf2snmpmediator.server.Config;
import com.technologies.highstreet.netconf2snmpmediator.server.MonitoredNetworkElement;

public class SNMPDevicePollingThread extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(SNMPDevicePollingThread.class);

    /* polling states for neconnection state */
    private static final int STATE_NULL = 0;
    private static final int STATE_FOUND = 1;
    private static final int STATE_NOTFOUND_FIRSTTIME = 2;
    private static final int STATE_NOTFOUND_MORE = 3;

    public static final long PERIOD = 5000;        //for runnable scheduler in ms
    private static final int POLLINTERVAL = 60 * 1000; // polling interval in ms
    private static int NETWORK_TIMEOUT = 5000; // in ms
    private static final long DOUBLECHECK_DELAY = 10000; // recheck before sending loss in ms
    private static final String PROBLEM_NAME = "MediatorDeviceConnectionLoss";
    private static final String PROBLEM_SEVERITY = "major";

    public static final long CHECKPROBLEMSTABLE_INTERVAL = 10*60*1000; //=10minutes [in ms]



    @SuppressWarnings("unused")
	private static final int SNMPPORT = 161;
    private static final int TELNETPORT = 23;
    private static final int HTTPPORT = 80;

    private static int POLLPORT = HTTPPORT;

    private static int lastState = STATE_NULL;
    private static int state = STATE_NULL;
    private static long lastTimePoll = 0;
    private static int connectionLossCounter=0;
    private final MonitoredNetworkElement networkElement;
    private final boolean runAsThread;
    private boolean stop;
    private final MediatorConnectionListener mConnectionListener;
    private final int mAlternativePingPort;

    public boolean isDeviceConnected() {
        return state == STATE_FOUND;
    }
   
    public SNMPDevicePollingThread(MonitoredNetworkElement ne, int altPingPort, boolean asthread, MediatorConnectionListener connectionListener) {
        this.networkElement = ne;
        this.mAlternativePingPort=altPingPort==POLLPORT?TELNETPORT:altPingPort;
        this.stop = false;
        this.runAsThread = asthread;
        this.mConnectionListener = connectionListener;
    }

    public SNMPDevicePollingThread(MonitoredNetworkElement ne) {

        this(ne,TELNETPORT, true, null);
    }

    private void onConnectionLost() {
        String timeStamp=new Timestamp(System.currentTimeMillis()).toInstant().toString();
        if (this.networkElement != null)
        {
            this.networkElement.setRemoteDeviceConnected(false);
            this.networkElement.addToProblemListNe(PROBLEM_NAME, PROBLEM_SEVERITY, timeStamp, this.networkElement.getDeviceName(), String.valueOf(SNMPDevicePollingThread.connectionLossCounter));
        }
        LOG.debug("send notification to odl (" + PROBLEM_NAME + ":critical)");
        String xmlSubTree = "<problem-notification xmlns=\"urn:onf:params:xml:ns:yang:microwave-model\">" +
                "<counter>"+SNMPDevicePollingThread.connectionLossCounter+"</counter>"+
                "<problem>" + PROBLEM_NAME + "</problem>" +
                "<object-id-ref>" + this.networkElement.getDeviceName()    + "</object-id-ref>" +
                "<severity>" + PROBLEM_SEVERITY + "</severity>" +
                "<time-stamp>" + timeStamp+"</time-stamp>" +
                "</problem-notification>";
        this.networkElement.pushToExternalMessageQueue(new ProblemNotification(xmlSubTree));
        SNMPDevicePollingThread.connectionLossCounter++;

        if(this.mConnectionListener!=null) {
            this.mConnectionListener.networkElementOnDisconnect();
        }
    }

    private void onConnectionEstablished() {

        String timeStamp=new Timestamp(System.currentTimeMillis()).toInstant().toString();
        if (this.networkElement != null)
        {
            this.networkElement.setRemoteDeviceConnected(true);
            this.networkElement.removeFromProblemListNe(PROBLEM_NAME);
        }
        LOG.debug("send notification to odl (" + PROBLEM_NAME + ":non-alarmed)");
        this.networkElement.pushToExternalMessageQueue(new ProblemNotification(
        			PROBLEM_NAME,
        			this.networkElement.getDeviceName(),
        			"non-alarmed",
        			timeStamp,
        			String.valueOf(SNMPDevicePollingThread.connectionLossCounter)));
        SNMPDevicePollingThread.connectionLossCounter++;

        if(this.mConnectionListener!=null) {
            this.mConnectionListener.networkElementOnConnect();
        }
    }

    private void doPoll() {
        boolean found = false;
        lastState = state;
        NETWORK_TIMEOUT=Config.getInstance().DEVICEPING_TIMEOUT;
        // Inet4Address adr = (Inet4Address)
        // InetAddress.getByName(this.networkElement.getDeviceIp());
        // found = adr.isReachable(NETWORK_TIMEOUT);
        LOG.debug(String.format("polling %s on port %d with timeout %d ms",this.networkElement.getDeviceIp(), POLLPORT, NETWORK_TIMEOUT));
        found = isReachable(this.networkElement.getDeviceIp(), POLLPORT, NETWORK_TIMEOUT);
        LOG.debug(String.format("isreachable=%s", found?"true":"false"));
        if(!found && POLLPORT!=this.mAlternativePingPort) // try telnetport if pollport not working
        {
            LOG.debug(String.format("polling %s on port %d with timeout %d ms",this.networkElement.getDeviceIp(), this.mAlternativePingPort, NETWORK_TIMEOUT));
            found=isReachable(this.networkElement.getDeviceIp(),this.mAlternativePingPort,NETWORK_TIMEOUT);
            if(found)
            {
                LOG.debug("changing default pollport to "+ this.mAlternativePingPort);
                POLLPORT=this.mAlternativePingPort;
            }
            LOG.debug(String.format("isreachable=%s", found?"true":"false"));
        }
        // state machine logic
        if (found) {
            state = STATE_FOUND;
        } else {
            if (state == STATE_NOTFOUND_FIRSTTIME) {
                state = STATE_NOTFOUND_MORE;
            } else if(state != STATE_NOTFOUND_MORE) {
                state = STATE_NOTFOUND_FIRSTTIME;
            }
        }

        // state has changed? => push event
        if (lastState != state || Config.getInstance().sendConnectionStateContinously()) {
            if (state == STATE_NOTFOUND_FIRSTTIME) {
                LOG.debug("device lost for first time");
            }
            if (state == STATE_NOTFOUND_MORE) {
                LOG.debug("device lost for second time");
                onConnectionLost();
            } else if (state == STATE_FOUND) {
                LOG.debug("device is back");
                onConnectionEstablished();
            }
        }
    }

    public void trystop() {
        this.stop = true;
    }

    @Override
    public void run() {
        super.run();
        long currentTimePoll;
        if (this.runAsThread) {
            while (!stop) {
                currentTimePoll = new Date().getTime();
                if (currentTimePoll > lastTimePoll + POLLINTERVAL) {
                    lastTimePoll = currentTimePoll;
                    doPoll();
                } else if (state == STATE_NOTFOUND_FIRSTTIME) {
                    if (currentTimePoll > lastTimePoll + DOUBLECHECK_DELAY) {
                        doPoll();
                    }
                } else {

                    try {
                        Thread.sleep(1000);
                    } catch (Exception err) {
                    }
                }
            }
        } else {
            if (!stop) {
                currentTimePoll = new Date().getTime();
                if (currentTimePoll > lastTimePoll + POLLINTERVAL) {
                    lastTimePoll = currentTimePoll;
                    doPoll();
                } else if (state == STATE_NOTFOUND_FIRSTTIME) {
                    if (currentTimePoll > lastTimePoll + DOUBLECHECK_DELAY) {
                        doPoll();
                    }
                }
            }
        }

    }

    private static boolean isReachable(String addr, int openPort, int timeOutMillis) {
        try
        {
            Socket soc = new Socket();

            soc.connect(new InetSocketAddress(addr, openPort), timeOutMillis);
            soc.close();
        }
        catch (SocketTimeoutException e) {
            LOG.debug("timed out");
            return false;
        }
        catch (IOException e) {
            LOG.warn(e.getMessage());
            return false;
        }
        return true;
    }

    private static SNMPDevicePollingThread mObj;

    private static ScheduledExecutorService scheduler;

    //private static ScheduledFuture<?> taskHandle;

    public static SNMPDevicePollingThread GetInstance(MonitoredNetworkElement ne, NetconfStreamCodecThread ioCodec,
            int altPingPort, boolean pollasthread, MediatorConnectionListener connectionListener) {
        if(mObj==null) {
            mObj=new SNMPDevicePollingThread(ne, altPingPort, pollasthread,connectionListener);
        } else {
       }
        return mObj;
    }

    public static void scheduleAtFixedRate(SNMPDevicePollingThread thread, long interval) {
        if(scheduler==null)
        {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            /*taskHandle = */scheduler.scheduleAtFixedRate(thread,    interval, interval, TimeUnit.MILLISECONDS);
        }
    }

    public static boolean isRunning() {
        if(mObj==null) {
            return false;
        }
        return mObj.isAlive();
    }

    public static void stopThread() {
        if(mObj!=null)
        {
            try {
                mObj.trystop();
                if(scheduler!=null)
                {    scheduler.shutdown();
                    scheduler.awaitTermination(1000, TimeUnit.MILLISECONDS);
                }
                mObj.join(2000);
            } catch (InterruptedException e) {
                LOG.error("Error waiting for thread end: " + e.getMessage());
            }

            // kill thread if it don't finish naturally
            if (mObj != null && mObj.isAlive()) {
                LOG.info("Killing polling processor thread");
                mObj.interrupt();
            }
        }
    }
}
