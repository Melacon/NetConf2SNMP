/**
 * Netconf Message processor.
 *
 * Reads the message queue and executes related actions within a Thread.
 * Owns the network element class which simulates the NETCONF NE behavior.
 * Processes also other messages, like user commands.
 *
 * @author Herbert (herbert.eiselt@highstreet-technologies.com)
 *
 */

package com.technologies.highstreet.netconf2snmpmediator.server.streamProcessing;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.technologies.highstreet.mediatorlib.netconf.server.basetypes.Console;
import com.technologies.highstreet.mediatorlib.netconf.server.basetypes.SnmpKeyValuePairList;
import com.technologies.highstreet.mediatorlib.netconf.server.basetypes.SnmpTrapNotification;
import com.technologies.highstreet.netconf.server.basetypes.MessageStore;
import com.technologies.highstreet.netconf.server.streamprocessing.NetconfMessageProcessorThread;
import com.technologies.highstreet.netconf.server.types.NetconfSender;
import com.technologies.highstreet.netconf.server.types.NetconfSessionStatusHolder;
import com.technologies.highstreet.netconf2snmpmediator.server.networkelement.Netconf2SNMPNetworkElement;

import net.i2cat.netconf.messageQueue.MessageQueue;
import net.i2cat.netconf.rpc.RPCElement;

public class Netconf2SNMPMessageProcessorThread extends NetconfMessageProcessorThread  {

    private static final Log log  = LogFactory.getLog(Netconf2SNMPMessageProcessorThread.class);

    private final Netconf2SNMPNetworkElement sne;

    public Netconf2SNMPMessageProcessorThread(String name, NetconfSessionStatusHolder status, NetconfSender sender,
            MessageQueue messageQueue, MessageStore messageStore, Netconf2SNMPNetworkElement ne, Console console) {

        super(name, status, sender,  messageQueue,  messageStore,  ne,  console);
        this.sne = ne;
        this.consoleMessage("SNMP Thread created");
        log.info("SNMP Thread created");

    }
    @Override
    protected void finalize() throws Throwable {
        super.finalize();

    }
    @Override
    public boolean doMessageProcessingForSpecificMessageClass(RPCElement message) throws IOException {

        boolean handled = super.doMessageProcessingForSpecificMessageClass(message);

        if (! handled) {
            if (message instanceof SnmpKeyValuePairList) {
                 String msg = sne.doProcessSnmpTrapAction(((SnmpKeyValuePairList)message).get());
                 if (msg != null) {
                     send( msg );
                 }
                handled = true;
            }
        }

        
        if (!handled) {
            if (message instanceof SnmpTrapNotification) {
                String netconfNotificationXml = this.sne.assembleRpcNotification( ((SnmpTrapNotification) message).toXML() );
                log.debug("sending trap notification: "+netconfNotificationXml);
                send(netconfNotificationXml);
                handled = true;
            }
        }
        
        if(!handled && message !=null) {
			log.debug("message not handled cls="+message.getClass().getSimpleName()+" obj="+message.toString());
		}
        return handled;
    }

    /*********************************************************************
     * Private message processing
     */




}
