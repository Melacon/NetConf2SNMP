package com.technologies.highstreet.netconf2snmpmediator.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.xml.sax.SAXException;

import com.technologies.highstreet.mediatorlib.data.MediatorServerProperties;
import com.technologies.highstreet.mediatorlib.data.MediatorStatusFile;
import com.technologies.highstreet.mediatorlib.netconf.server.basetypes.Console;
import com.technologies.highstreet.netconf.server.exceptions.ServerException;
import com.technologies.highstreet.netconf2snmpmediator.devices.MediatorDevice;
import com.technologies.highstreet.netconf2snmpmediator.server.networkelement.Netconf2SNMPNetworkElement;
import com.technologies.highstreet.netconf2snmpmediator.server.networkelement.SimulatorNetworkElement;
import com.technologies.highstreet.netconf2snmpmediator.server.plugins.AutoLoginPlugin;
import net.i2cat.netconf.rpc.RPCElement;

public class Program {

    private static Log LOG;
    private static boolean CLIMODE = false;
    private static final String MEDIATORSERVER_CONFIGFILENAME = "/etc/mediatorserver.conf";
    private static final String STATUSFILENAME = "status.json";
    private static SNMPMediatorConfig cfg = null;
    private static MediatorStatusFile status=null;

    private static void initDebug(String debugFilename) {
        BasicConfigurator.configure();
        Logger.getRootLogger().getLoggerRepository().resetConfiguration();
        LOG = LogFactory.getLog(Netconf2SNMPMediator.class);
        if(CLIMODE)
        {
            ConsoleAppender console = new ConsoleAppender(); // create appender
            // configure the appender
            // String PATTERN = "%d [%p|%c|%C{1}] %m%n";
            String PATTERN = "%d [%p|%C{1}] %m%n";
            console.setLayout(new PatternLayout(PATTERN));
            console.setThreshold(Config.getInstance().LogLevel);
            console.activateOptions();
            // add appender to any Logger (here is root)
            Logger.getRootLogger().addAppender(console);
        }

        RollingFileAppender fa = new RollingFileAppender();
        fa.setName("FileLogger");
        fa.setFile(debugFilename);
        fa.setLayout(new PatternLayout("%d %-5p [%c] %m%n"));
        fa.setThreshold(Config.getInstance().LogLevel);
        fa.setMaximumFileSize(10000000);
        fa.setAppend(true);
        fa.setMaxBackupIndex(5);
        fa.activateOptions();
        // add appender to any Logger (here is root)
        Logger.getRootLogger().addAppender(fa);
        // repeat with all other desired appenders

    }
    private static final Console console = new Console() {
		
		@Override
		public String cliOutput(String msg) {
			   return Program.staticCliOutputNewLine(msg);
		}
	};
    public static void main(String[] args)
    {
    	SimulatorNetworkElement.registerDevices();
        String title = "Netconf NE SNMP Mediator\n";
        int optIdx=0;
        if (args.length < 1) {
            System.err.println("To less parameters. Command: Server configFilename [pathToYang]");
            return;
        }
        if(args[0].equals("--version"))
        {
            System.out.println(Netconf2SNMPMediator.getVersion());
            System.exit(0);
        }
        if(args[0].equals("--xmlversion"))
        {
            if(args.length<2) {
                System.exit(1);
            }
            System.out.println(Netconf2SNMPNetworkElement.detectXMLVersion(args[1]));
            System.exit(0);
        }
        if(args[0].equals("--devices"))
        {
        	System.out.println(MediatorDevice.getJSON());
        	System.exit(0);
        }
        if(args[0].equals("--cli"))
        {
            CLIMODE=true;
            optIdx++;
        }
        String jsonFilename = args[optIdx++];
        try {
            cfg = new SNMPMediatorConfig(jsonFilename);
            staticCliOutputNewLine("loaded config file successfully");
            status  = new MediatorStatusFile(getStatusFilename(jsonFilename));
            staticCliOutputNewLine("status saved into "+status.getFilename());
            status.setPID(cfg.getCurrentPID());
            cfg.writePIDFile();
        } catch (Exception e) {
            staticCliOutputNewLine("Error loading config file " + jsonFilename);
            return;
        }
        Config.getInstance().tryLoad(MEDIATORSERVER_CONFIGFILENAME);
        String debugFile = cfg.getLogFilename();
        String yangPath = args.length >= optIdx+1 ? args[optIdx++] : "yang/yangNeModel";
        String uuid = args.length >= optIdx+1 ? args[optIdx] : "";
        String xmlFilename = cfg.getNeXMLFilename();
        String rootPath = "";
        int port = cfg.getNetconfPort();
        status.setStarted();
        status.resetNetconfConnections();
        /*
         * if (Config.DEBUG) { rootPath = "build/"; } else { rootPath = ""; }
         */
        xmlFilename = rootPath + xmlFilename;
        yangPath = rootPath + yangPath;

        staticCliOutputNewLine(title);
        staticCliOutputNewLine("Version: " + getVersion());
        staticCliOutputNewLine("Start parameters are:");
        staticCliOutputNewLine("\tFilename: " + xmlFilename);
        staticCliOutputNewLine("\tPort: " + port);
        staticCliOutputNewLine("\tDebuginfo and communication is in file: " + debugFile);
        staticCliOutputNewLine("\tYang files in directory: " + yangPath);
        staticCliOutputNewLine("\tUuid: " + uuid);
        staticCliOutputNewLine("\tCli: " + CLIMODE);

        initDebug(debugFile);

        LOG.info(title);

        try {
            final Netconf2SNMPMediator server = Netconf2SNMPMediator.createServer(cfg,status,console);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    staticCliOutput("received shutdown signal");
                    if(server!=null) {
                        try {    server.stopServer();}
                        catch(Exception e) {staticCliOutput("problem while shutting down: "+e.getMessage());}
                    }
                }
            });
            LOG.info("Version="+Netconf2SNMPMediator.getVersion());
            Netconf2SNMPNetworkElement ne = new SimulatorNetworkElement(xmlFilename, yangPath, uuid, cfg.getDeviceType(),
                    cfg.getDeviceIp(),cfg.getDevicePort(), cfg.getTrapPort(), console,server,server);
            
            ne.setDeviceName(cfg.getName());
           
            String hostip=null;
            try {
                if(MediatorServerProperties.exists()) {
                    hostip=MediatorServerProperties.Instantiate().getHostIp();
                }
            } catch(Exception e) {}

            if(hostip!=null) {
                server.addPlugin(new AutoLoginPlugin(ne, cfg, hostip));
            } else {
                LOG.warn("cannot autologin. no hostip found");
            }
            server.initializeServer("0.0.0.0", port, ne, Config.getInstance().MediatorDefaultNetworkInterfaceNum, ne);

            server.startServer();
            if (CLIMODE == true) {
                // read lines form input
                BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
                String command;
                    while (true) {
                    staticCliOutput(port + ":" + xmlFilename + "> ");
                    command = buffer.readLine();
                    if (command != null) {
                        command = command.toLowerCase();
                    } else {
                        command = "<null>";
                    }

                    if (command.equals("list")) {
                        staticCliOutputNewLine("Messages received(" + server.getStoredMessages().size() + "):");
                        for (RPCElement rpcElement : server.getStoredMessages()) {
                            staticCliOutputNewLine("#####  BEGIN message #####\n" + rpcElement.toXML() + '\n'
                                    + "#####   END message  #####");
                        }
                    } else if (command.equals("size")) {
                        staticCliOutputNewLine("Messages received(" + server.getStoredMessages().size() + "):");

                    } else if (command.equals("quit")) {
                        staticCliOutputNewLine("Stop server");
                        server.stopServer();
                        break;
                    } else if (command.equals("info")) {
                        staticCliOutputNewLine("Version: " + Netconf2SNMPMediator.getVersion() + " Port: " + port + " File: " + xmlFilename + " (v: "+ne.getXmlVersion()+")");
                    } else if (command.equals("status")) {
                        staticCliOutputNewLine("Status: not implemented");
                    } else if (command.startsWith("n")) {
                        String notifyCommand = command.substring(1);
                        staticCliOutputNewLine("Notification: " + notifyCommand);
                        server.notify(notifyCommand);
                    } else if (command.length() == 0) {
                    } else {
                        staticCliOutputNewLine("NETCONF Simulator V3.0");
                        staticCliOutputNewLine("Available commands: status, quit, info, list, size, nZZ, nl, nx");
                        staticCliOutputNewLine("\tnx: list internal XML doc tree");
                        staticCliOutputNewLine("\tnl: list available notifications");
                        staticCliOutputNewLine("\tnZZ: send notification with number ZZ");
                    }
                }
            } else {
                while (server.isRunning()) {
                    Thread.sleep(1000);
                }

            }
        } catch (SAXException e) {
            LOG.error(staticCliOutputNewLine("(..something..) failed " + e.getMessage()));
        } catch (ParserConfigurationException e) {
            LOG.error(staticCliOutputNewLine("(..something..) failed " + e.getMessage()));
        } catch (TransformerConfigurationException e) {
            LOG.error("(..something..) failed " + e.getMessage());
        } catch (ServerException e) {
            LOG.error("(..something..) failed " + e.getMessage());
        } catch (XPathExpressionException e) {
            LOG.error("(..something..) failed " + e.getMessage());
        } catch (IOException e) {
            LOG.error("(..something..) failed " + e.getMessage());
        } catch (InterruptedException e) {
            LOG.error("(..something..) failed " + e.getMessage());
        }
        staticCliOutputNewLine("clearing pid file");
        clearPID();
        staticCliOutputNewLine("Exiting");
        System.exit(0);
    }
    private static String getVersion()
    {
    	return Netconf2SNMPMediator.getVersion();
    }
    public static void clearPID()
    {
        if(status!=null) {
            status.clear();
        }
        try
        {
            if(cfg!=null) {
                cfg.deletePIDFile();
            }
        }
        catch(SecurityException e)
        {

        }
    }
    public static String getStatusFilename(String configFilename) {
        String s=null;
        final String regex = "^.*\\/(.*\\.config)";


        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(configFilename);

        //try to find the folder of the mediator (where the config file lives)
        if (matcher.find() && matcher.groupCount()>0) {
            staticCliOutputNewLine("Full match: " + matcher.group(0));
            s=configFilename.substring(0, matcher.start(1))+STATUSFILENAME;
        } else {
            s=STATUSFILENAME;
        }
        return s;
    }

    /*
     * get IPv4 Address of LAN for ETHERNET Device <devNum>
     */
    public static String getNetworkIp(int devNum) throws SocketException, Exception {
        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
        while (e.hasMoreElements()) {
            if (devNum-- >= 0) {
                continue;
            }
            NetworkInterface n = e.nextElement();
            Enumeration<InetAddress> ee = n.getInetAddresses();
            while (ee.hasMoreElements()) {
                InetAddress i = ee.nextElement();
                if (!(i instanceof Inet6Address)) {
                    return i.getHostAddress();
                }
            }
        }
        throw new Exception("no ip address found for selected network interface");
    }
    static String staticCliOutputNewLine(String msg) {
        if(CLIMODE || LOG==null) {
            System.out.println(msg);
        } else {
            LOG.info(msg);
        }
        return msg;
    }

    static String staticCliOutput(String msg) {
        if(CLIMODE || LOG==null) {
            System.out.print(msg);
        } else {
            LOG.info(msg);
        }
        return msg;
    }
}
