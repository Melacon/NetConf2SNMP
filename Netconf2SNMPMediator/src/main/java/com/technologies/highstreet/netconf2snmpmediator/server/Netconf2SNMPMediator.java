package com.technologies.highstreet.netconf2snmpmediator.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sshd.common.FactoryManager;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.PropertyResolverUtils;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionListener;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.ServerFactoryManager;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.w3c.dom.Document;

import com.technologies.highstreet.mediatorlib.data.MediatorStatusFile;
import com.technologies.highstreet.mediatorlib.netconf.server.basetypes.Console;
import com.technologies.highstreet.mediatorlib.netconf.server.basetypes.SnmpKeyValuePairList;
import com.technologies.highstreet.mediatorlib.netconf.server.networkelement.NodeEditConfigCollection;
import com.technologies.highstreet.mediatorlib.netconf.server.types.NetconfTagList;
import com.technologies.highstreet.netconf.server.basetypes.Behaviour;
import com.technologies.highstreet.netconf.server.basetypes.BehaviourContainer;
import com.technologies.highstreet.netconf.server.basetypes.ConnectionInfo;
import com.technologies.highstreet.netconf.server.basetypes.MessageStore;
import com.technologies.highstreet.netconf.server.control.BaseNetconfController;
import com.technologies.highstreet.netconf.server.control.NetconfNotifyExecutor;
import com.technologies.highstreet.netconf.server.control.NetconfNotifyOriginator;
import com.technologies.highstreet.netconf.server.exceptions.ServerException;
import com.technologies.highstreet.netconf.server.ssh.AlwaysTruePasswordAuthenticator;
import com.technologies.highstreet.netconf.server.ssh.UserPasswordAuthenticator;
import com.technologies.highstreet.netconf2snmpmediator.server.control.Netconf2SNMPFactory;
import com.technologies.highstreet.netconf2snmpmediator.server.networkelement.IOnPluginEventListener;
import com.technologies.highstreet.netconf2snmpmediator.server.networkelement.IOnTrapReceivedListener;
import com.technologies.highstreet.netconf2snmpmediator.server.networkelement.Netconf2SNMPNetworkElement;
import com.technologies.highstreet.netconf2snmpmediator.server.streamProcessing.MediatorConnectionListener;
import com.technologies.highstreet.netconf2snmpmediator.server.streamProcessing.SNMPDevicePollingThread;

import net.i2cat.netconf.rpc.RPCElement;

/**
 *
 *
 * @author Micha
 */

public class Netconf2SNMPMediator extends PluginableMediator implements IOnPluginEventListener,IOnTrapReceivedListener,MessageStore, BehaviourContainer, NetconfNotifyOriginator {

    private static final long SSH_TIMEOUT_MS = 1000*60*60; //60 minuts

	private static final Object REKEY_LIMIT_2MIN_MS = 2*60*1000;

	protected static final boolean LIMIT = false;

	private static Log LOG = LogFactory.getLog(Netconf2SNMPMediator.class);;

    private SshServer sshd;

    // stored messages
    private List<RPCElement> messages;

    // behaviours
    private List<Behaviour> behaviours;


    //protected NetconfNotifyExecutor netconfNotifyExecutor = null;
    private final List<NetconfNotifyExecutor> netconfNotifyExecutorList;
    private DeviceConnectionMonitor deviceConnectionMonitor=null;
    private final SNMPMediatorConfig configFile;
    private boolean mIsRunning;

    private final MediatorStatusFile statusFile;

	private final Console console;


    public boolean isRunning() {
        return this.mIsRunning;
    }


    // hide default constructor, forcing using factory method
    private Netconf2SNMPMediator(SNMPMediatorConfig cfg, MediatorStatusFile status,Console console) {
        super();
        this.netconfNotifyExecutorList=new ArrayList<>();
        this.configFile = cfg;
        this.statusFile = status;
        this.console = console;
    }

    /**
     * Server factory creates a server
     * @param cfg configuration
     * @param status Statusfile for debugging
     * @return server
     */
    public static Netconf2SNMPMediator createServer(SNMPMediatorConfig cfg, MediatorStatusFile status, Console console) {
        Netconf2SNMPMediator server = new Netconf2SNMPMediator(cfg,status,console);
        server.messages = new ArrayList<>();
//        server.storeMessages = false;
        return server;
    }

    /**
     * listener for netconf and remote device connection status
     */
    private final MediatorConnectionListener mediatorConnectionListener = new MediatorConnectionListener() {

        @Override
        public void netconfOnDisconnect() {
            LOG.debug("netconf disconnected");
         /*   if (Netconf2SNMPMediator.this.configFile != null) {
                Netconf2SNMPMediator.this.configFile.setIsNetconfConnected(false);
                try {
                    Netconf2SNMPMediator.this.configFile.save();
                } catch (Exception e) {
                    LOG.error("error saving netconf status to config file");
                }
            }
		*/
        }

        @Override
        public void netconfOnConnect() {
            LOG.debug("netconf connected");
          /*  if (Netconf2SNMPMediator.this.configFile != null) {
                Netconf2SNMPMediator.this.configFile.setIsNetconfConnected(true);
                try {
                    Netconf2SNMPMediator.this.configFile.save();
                } catch (Exception e) {
                    LOG.error("error saving netconf status to config file");
                }
            }
            */
        }

        @Override
        public void networkElementOnConnect() {
          /*  if (Netconf2SNMPMediator.this.configFile != null) {
                Netconf2SNMPMediator.this.configFile.setIsNeConnected(true);
                try {
                    Netconf2SNMPMediator.this.configFile.save();
                } catch (Exception e) {
                    LOG.error("problem saving ne connection status to config file");
                }
            }
            */
            if (Netconf2SNMPMediator.this.statusFile != null) {
                Netconf2SNMPMediator.this.statusFile.setIsNeConnected(true);
                try {
                    Netconf2SNMPMediator.this.statusFile.save();
                } catch (Exception e) {
                    LOG.error("problem saving ne connection status to status file");
                }
            }
        }

        @Override
        public void networkElementOnDisconnect() {
            /*if (Netconf2SNMPMediator.this.configFile != null) {
                Netconf2SNMPMediator.this.configFile.setIsNeConnected(false);
                try {
                    Netconf2SNMPMediator.this.configFile.save();
                } catch (Exception e) {
                    LOG.error("problem saving ne connection status to config file");
                }
            }
            */
            if (Netconf2SNMPMediator.this.statusFile != null) {
                Netconf2SNMPMediator.this.statusFile.setIsNeConnected(false);
                try {
                    Netconf2SNMPMediator.this.statusFile.save();
                } catch (Exception e) {
                    LOG.error("problem saving ne connection status to status file");
                }
            }
        }
    };

    private final SessionListener sshSessionListener = new SessionListener() {
        @Override
        public void sessionCreated(Session session) {
        	String remoteIp=session.getIoSession().getRemoteAddress().toString();
            if(LIMIT)
        	{
        		if(Netconf2SNMPMediator.this.statusFile.getConnectionCount(remoteIp)>0)
        		{
        			LOG.debug("sdn controller tries to open a second session");
        			try {
						session.close();
						return;
					} catch (IOException e) {
						LOG.warn("problem closing unwanted session: "+e.getMessage());
					}
        		}
        	}
            SessionListener.super.sessionCreated(session);
            try {
                ConnectionInfo remoteConnection=new ConnectionInfo(remoteIp);
                remoteIp=remoteConnection.toString();
            } catch (Exception e1) {
                LOG.debug(e1.getMessage());
            }

            if (Netconf2SNMPMediator.this.statusFile != null) {
                Netconf2SNMPMediator.this.statusFile.addConnection(remoteIp);
                try {
                    Netconf2SNMPMediator.this.statusFile.save();
                } catch (Exception e) {
                    LOG.error("problem saving ne connection status to status file");
                }
            }
        }

        @Override
        public void sessionClosed(Session session) {

            String remoteIp=session.getIoSession().getRemoteAddress().toString();
            try {
                ConnectionInfo remoteConnection=new ConnectionInfo(remoteIp);
                remoteIp=remoteConnection.toString();
            } catch (Exception e1) {
                LOG.debug(e1.getMessage());
            }
            if (Netconf2SNMPMediator.this.statusFile != null) {
                Netconf2SNMPMediator.this.statusFile.removeConnection(remoteIp);
                try {
                    Netconf2SNMPMediator.this.statusFile.save();
                } catch (Exception e) {
                    LOG.error("problem saving ne connection status to status file");
                }
            }
        };
    };



    /**
     * Initializes the server
     *
     * @param host
     *            host name (use null to listen in all interfaces)
     * @param listeningPort
     *            where the server will listen for SSH connections
     * @param sne
     *            with NetworkElement model
     * @param devNum
     *            number of interface card
     *
     */
    public void initializeServer(String host, int listeningPort, Netconf2SNMPNetworkElement sne, int devNum, MonitoredNetworkElement mne) {
    	this._loadPlugins(sne,this.configFile);
        this._plgPreInit();
        String username = this.configFile.getNetconfUsername();
        String passwd = this.configFile.getNetconfPassword();
        LOG.info(Program.staticCliOutputNewLine("Configuring mediator ..."));
        //LOG.info("version: "+getVersion()+ " nexml: "+ sne.getXmlFilename()+ "(v: "+sne.getXmlVersion()+")");
        sshd = SshServer.setUpDefaultServer();
         if (sshd == null) {
            LOG.fatal(Program.staticCliOutputNewLine("No SSH Server ... exit"));
            System.exit(1);
        } else {
        	PropertyResolverUtils.updateProperty(sshd, FactoryManager.IDLE_TIMEOUT, SSH_TIMEOUT_MS);     	
        	//disable ssh rekeying
        	PropertyResolverUtils.updateProperty(sshd, FactoryManager.REKEY_BLOCKS_LIMIT, -1);
        	PropertyResolverUtils.updateProperty(sshd, FactoryManager.REKEY_BYTES_LIMIT, -1);
        	PropertyResolverUtils.updateProperty(sshd, FactoryManager.REKEY_PACKETS_LIMIT, -1);
        	PropertyResolverUtils.updateProperty(sshd, FactoryManager.REKEY_TIME_LIMIT, -1);
        	
        	sshd.setHost(host);
            sshd.setPort(listeningPort);
            LOG.info(Program.staticCliOutputNewLine("Host: '" + host + "', listenig port: " + listeningPort));
            sshd.setPasswordAuthenticator((username!=null || passwd!=null || username=="" || passwd=="")?new AlwaysTruePasswordAuthenticator():new UserPasswordAuthenticator(username,passwd));
            sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File(this.configFile.getHostKeyFilename()).toPath()));

            List<NamedFactory<Command>> subsystemFactories = new ArrayList<>();
            subsystemFactories
                    .add(Netconf2SNMPFactory.createFactory(this, this, this, sne, mediatorConnectionListener, this.console));
            sshd.setSubsystemFactories(subsystemFactories);
            this.deviceConnectionMonitor=new DeviceConnectionMonitor(mne,null,this.configFile.getAlternateivePingPort(),this.mediatorConnectionListener);
            LOG.info(Program.staticCliOutputNewLine("Mediator configured."));
        }
    }

    @Override
    public void defineBehaviour(Behaviour behaviour) {
        if (behaviours == null) {
            behaviours = new ArrayList<>();
        }
        synchronized (behaviours) {
            behaviours.add(behaviour);
        }
    }

    @Override
    public List<Behaviour> getBehaviours() {
        if (behaviours == null) {
            return null;
        }
        synchronized (behaviours) {
            return behaviours;
        }
    }

    public void startServer() throws ServerException {
        LOG.info(Program.staticCliOutputNewLine("Starting server..."));
        sshd.addSessionListener(this.sshSessionListener);
        try {
            sshd.start();
        } catch (IOException e) {
            LOG.error(Program.staticCliOutputNewLine("Error starting server!" + e.getMessage()));
            throw new ServerException("Error starting server", e);
        }
        this.mIsRunning=true;
        LOG.info(Program.staticCliOutputNewLine("Server started."));
        this._plgPostInit();
    }

    public void stopServer() throws ServerException {
        Program.staticCliOutputNewLine("Stopping server...");
        this._plgClose();
        sshd.removeSessionListener(this.sshSessionListener);
        try {
            sshd.stop();
            if(deviceConnectionMonitor!=null) {
                deviceConnectionMonitor.waitAndInterruptThreads();
            }

            stopSNMPThreads();
        } catch (IOException e) {
            Program.staticCliOutputNewLine("Error stopping server!" + e);
            throw new ServerException("Error stopping server", e);
        }
        this.mIsRunning=false;
        Program.clearPID();
        Program.staticCliOutputNewLine("Server stopped.");
    }

    private void stopSNMPThreads() {
        if(SNMPDevicePollingThread.isRunning()) {
            SNMPDevicePollingThread.stopThread();
        }
    }

    @Override
    public void storeMessage(RPCElement message) {
        if (messages != null) {
            synchronized (messages) {
                LOG.info("Storing message " + message.getMessageId());
                messages.add(message);
            }
        }
    }

    @Override
    public List<RPCElement> getStoredMessages() {
        synchronized (messages) {
            return Collections.unmodifiableList(messages);
        }
    }


    @Override
    public void addNetconfNotifyExecutor(BaseNetconfController newNetconfProcessor) {
        this.netconfNotifyExecutorList.add(newNetconfProcessor);

    }

    @Override
    public void removeNetconfNotifyExecutor(BaseNetconfController netconfProcessor) {
        this.netconfNotifyExecutorList.remove(netconfProcessor);
    }

    public void notify(String command) {
        if (this.netconfNotifyExecutorList.size() > 0) {
            for (NetconfNotifyExecutor e : this.netconfNotifyExecutorList) {
                if (e != null) {
                    e.notify(command);
                }
            }

        } else {
            System.out.println("No notifier registered.");
        }

    }

    public static String getVersion()
    {
    	return getVersion(null,null);
    }

    public static String getVersion(Class c,String propPath) {
        String version = null;

        Class<Netconf2SNMPMediator> cls = c==null?Netconf2SNMPMediator.class:c;
        String pp=propPath==null?"/META-INF/maven/com.technologies.highstreet/netconf2snmpmediator/pom.properties":propPath;
        // try to load from maven properties first
        try {
            Properties p = new Properties();
            InputStream is = cls.getResourceAsStream(pp);
            if (is != null) {
                p.load(is);
                version = p.getProperty("version", "unknown");
                is.close();
            }
        } catch (Exception e) {
            // ignore
        }

        // fallback to using Java API
        if (version == null) {
            Package aPackage = cls.getPackage();
            if (aPackage != null) {
                version = aPackage.getImplementationVersion();
                if (version == null) {
                    version = aPackage.getSpecificationVersion();
                }
            }
        }

        if (version == null) {
            // we could not compute the version so use a blank
            version = "";
        }

        return version;
        // return this.getClass().getPackage().getImplementationVersion();
    }

  


    @Override
    public boolean onTrapReceived(SnmpKeyValuePairList trap) {
        return this._plgOnTrapReceived(trap);
    }

    @Override
    public void onPreRequest(String messageId, NetconfTagList tags, NodeEditConfigCollection nodes) {
        this._plgPreRequest(messageId, tags, nodes);
    }

    @Override
    public void onPostRequest(String messageId, NetconfTagList tags, NodeEditConfigCollection nodes) {
        this._plgPostRequest(messageId, tags, nodes);
    }

    @Override
    public void onPreEditRequest(String messageId, NetconfTagList tags, Document sourceMessage) {
        this._plgPreEditRequest(messageId, tags, sourceMessage);
    }

    @Override
    public void onPostEditRequest(String messageId, NetconfTagList tags, Document sourceMessage) {
        this._plgPostEditRequest(messageId, tags, sourceMessage);
    }

    public void onDeviceConnectionStatusChanged(int before,int now)
    {
        this._plgOnDeviceConnectionStatusChanged(before,now);
    }





}
