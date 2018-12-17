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

package com.technologies.highstreet.netconf.server.streamprocessing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.technologies.highstreet.mediatorlib.netconf.server.basetypes.Console;
import com.technologies.highstreet.mediatorlib.netconf.server.networkelement.NetworkElement;
import com.technologies.highstreet.mediatorlib.netconf.server.types.NetconfTagList;
import com.technologies.highstreet.netconf.server.basetypes.MessageStore;
import com.technologies.highstreet.netconf.server.basetypes.UserCommand;
import com.technologies.highstreet.netconf.server.types.NetconfSender;
import com.technologies.highstreet.netconf.server.types.NetconfSessionStatus;
import com.technologies.highstreet.netconf.server.types.NetconfSessionStatusHolder;

import net.i2cat.netconf.messageQueue.MessageQueue;
import net.i2cat.netconf.rpc.Query;
import net.i2cat.netconf.rpc.QueryFactory;
import net.i2cat.netconf.rpc.RPCElement;

public class NetconfMessageProcessorThread extends Thread implements NetconfSender {

	private static final Log log = LogFactory.getLog(NetconfMessageProcessorThread.class);

	private static final long MESSAGE_TIMEOUT_MS = 1000*60*5;	//5 minutes

	// status fields
	protected final NetconfSessionStatusHolder status;
	private final NetconfSender sender;
	protected final MessageQueue messageQueue;
	protected final MessageStore messageStore;
	// message counter

	protected final NetworkElement theNe;
	protected final Console console;

	private int msgDelaySeconds = 0;
	private int msgToDiscardCounter = 0;
	private Pattern msgPattern = setPattern(null);
	private int messageCounter = 100;

	private long lastmsg;

	private boolean blockMessageQueueSend=false;
	public boolean doBlockMessageQueueSend() {return this.blockMessageQueueSend;}
	
	public NetconfSender getNetconfSender() {
		return this.sender;
	}

	public NetconfMessageProcessorThread(String name, NetconfSessionStatusHolder status, NetconfSender sender,
			MessageQueue messageQueue, MessageStore messageStore, NetworkElement ne, Console console) {
		super(name);
		this.status = status;
		this.sender = sender;
		this.messageQueue = messageQueue;
		this.messageStore = messageStore;
		this.theNe = ne;
		this.console = console;
		this.blockMessageQueue();
		// ne.setMessageQueue(messageQueue);
		this.theNe.addExternalMessageQueue(messageQueue);

	}

	/**
	 * Needs to be override for subclasses
	 * 
	 * @param message to be handled
	 * @return true if handled
	 * @throws IOException if IO problem
	 */

	public boolean doMessageProcessingForSpecificMessageClass(RPCElement message) throws IOException {

		boolean handled = false;

		if (message instanceof NetconfIncommingMessageRepresentation) {
			doMessageProcessing((NetconfIncommingMessageRepresentation) message);
			handled = true;

		} else if (this.blockMessageQueueSend) {
			log.debug("channel not fully initialized for notifications. ignore message");
			handled=true;
		} else {

			
			if (message instanceof UserCommand) {
				doNotificationProcessing((UserCommand) message);
				handled = true;
			} else if (message instanceof ProblemNotification) {
				doNotificationProcessing((ProblemNotification) message);
				handled = true;
			} else if (message instanceof AttributeValueChangedNotification) {
				doNotificationProcessing((AttributeValueChangedNotification) message);
				handled = true;
			}
			
		}
		return handled;
	}

	@Override
	public void run() {
		MyStringCollection lastMessageIds = new MyStringCollection();
		long now = System.currentTimeMillis();
		lastmsg=now;
		long error_max=MESSAGE_TIMEOUT_MS/200;
		long error=0;
		while (status.less(NetconfSessionStatus.SESSION_CLOSED)) {
			RPCElement message = messageQueue.consume();
			
			if (message == null) { 	//if no message in queue
				this.sleepMilliSeconds(200);
				//check if no message received fot MESSAGE_TIMEOUT_MS millis
				if ((now - lastmsg) > MESSAGE_TIMEOUT_MS) {
					log.warn("no message received for "+MESSAGE_TIMEOUT_MS+" ms. closing session.");
					status.change(NetconfSessionStatus.SESSION_CLOSED);
				}
				error++;
				if(error>error_max)
				{
					log.warn("no message received for "+error+" times. closing session.");
					status.change(NetconfSessionStatus.SESSION_CLOSED);
				}
			} else {
				String mid=message.getMessageId();
				if(lastMessageIds.counts(mid)>1)
				{
					log.warn("looping message found for "+mid+": "+message.toXML());
					continue;
				}
				else
				{
					lastMessageIds.add(message.getMessageId());
					if(lastMessageIds.size()>10)
						lastMessageIds.remove(0);
				}
				error=0;
				lastmsg=now;
				if(log.isTraceEnabled())
					log.trace("Message received: "+ message.getClass().getSimpleName() +" "+ message.toString());

				// store message if necessary
				if (messageStore != null) {
					messageStore.storeMessage(message);
				}

				// avoid message processing when session is already closed
				if (status.equals(NetconfSessionStatus.SESSION_CLOSED)) {
					log.warn("Session is closing or is already closed, message will not be processed");
					return;
				}

				// process message
				try {
						if (!doMessageProcessingForSpecificMessageClass(message)) {
							log.warn("Unhandled message: " + message.toString());
						}
					
					/*
					 * if (message instanceof NetconfIncommingMessageRepresentation) {
					 * doMessageProcessing((NetconfIncommingMessageRepresentation)message); } else
					 * if (message instanceof UserCommand) {
					 * doNotificationProcessing((UserCommand)message); } else {
					 * log.warn("Unknown message: " + message.toString()); }
					 */
				} catch (IOException e) {
					log.error("Error sending reply", e);
					break;
				}
			}
		}
		log.debug("Message processor ended");
		this.theNe.removeExternalMessageQueue(messageQueue);
	}

	/*********************************************************************
	 * Private message processing
	 */

	@Override
	public void send(String xmlMessage) throws IOException {
		log.debug("NETCONF:" + xmlMessage);// trace
		sender.send(xmlMessage);
	}
	@Override
	public void clearOutputStream() {
		sender.clearOutputStream();
		
	}

	private void send(List<String> xmlMessages) throws IOException {
		for (String xmlMessage : xmlMessages) {
			send(xmlMessage);
		}
	}

	private void doNotificationProcessing(AttributeValueChangedNotification message) throws IOException {
		this.send(theNe.assembleRpcNotification(message.toXML()));
	}

	private void doNotificationProcessing(ProblemNotification message) throws IOException {
		this.send(theNe.assembleRpcNotification(message.toXML()));
	}

	/**
	 * received messages through netconf
	 * block push messages (messageQueue) until rpcsubscription received 
	 * @param receivedMessage
	 * @throws IOException
	 */
	protected void doMessageProcessing(NetconfIncommingMessageRepresentation receivedMessage) throws IOException {

		if (receivedMessage.isHello()) {
			this.clearOutputStream();
			this.blockMessageQueue();
			consoleMessage("Hello");
			if (status.less(NetconfSessionStatus.HELLO_RECEIVED)) {
				status.change(NetconfSessionStatus.HELLO_RECEIVED);
				// send hello
				log.debug("Sending answer to hello...");
				String sessionId = String.valueOf((int) (Math.random() * Integer.MAX_VALUE));
				send(theNe.assembleHelloReply(sessionId));

			} else {
				log.error("Hello already received. Aborting");
				sendCloseSession();
				status.change(NetconfSessionStatus.CLOSING_SESSION);
			}

		} else if (receivedMessage.isRpcCreateSubscription()) {
				consoleMessage("CreateSubscription[" + receivedMessage.getMessageId() + "]"
					+ receivedMessage.getFilterTags().asCompactString());
			send(theNe.assembleRpcReplyEmptyDataOk(receivedMessage.getMessageId()));
			this.unblockMessageQueue();	

		} else if (receivedMessage.isRpcGetFilter()) {
			String tagString = receivedMessage.getFilterTags().asCompactString();
			boolean matches = msgPattern.matcher(tagString).matches();
			consoleMessage("Get[" + receivedMessage.getMessageId() + "]  " + (matches ? "matches " : "") + tagString);

			if (matches && msgToDiscardCounter > 0) {
				consoleMessage("Discard message: " + receivedMessage.getMessageId());
				msgToDiscardCounter--;
			} else {
				if (matches && msgDelaySeconds > 0) {
					consoleMessage("Wait seconds: " + msgDelaySeconds + " for msg " + receivedMessage.getMessageId());
					sleepMilliSeconds(msgDelaySeconds*1000);
					msgDelaySeconds = 0;
					consoleMessage("Proceed");
				}
				send(theNe.assembleRpcReplyFromFilterMessage(receivedMessage.getMessageId(),
						receivedMessage.getFilterTags()));
			}

		} else if (receivedMessage.isRpcGetConfigSourceRunningFilter()) {
			NetconfTagList tags = receivedMessage.getFilterTags();
			if (!tags.isEmtpy()) { // Do not indicate polls
				consoleMessage("Get-config [" + receivedMessage.getMessageId() + "] running "
						+ receivedMessage.getFilterTags().asCompactString());

			}
			send(theNe.assembleRpcReplyFromFilterMessage(receivedMessage.getMessageId(),
					receivedMessage.getFilterTags()));

		} else if (receivedMessage.isRpcLockTargetRunning()) {
			consoleMessage("Lock [" + receivedMessage.getMessageId() + "] running");
			send(theNe.assembleRpcReplyOk(receivedMessage.getMessageId()));

		} else if (receivedMessage.isRpcUnlockTargetRunning()) {
			consoleMessage("Unlock [" + receivedMessage.getMessageId() + "] running");
			send(theNe.assembleRpcReplyOk(receivedMessage.getMessageId()));

		} else if (receivedMessage.isRpcEditConfigTargetRunningDefaultOperationConfig()) {
			consoleMessage("Edit-config [" + receivedMessage.getMessageId() + "] message");
			send(theNe.assembleEditConfigElementReplyList(receivedMessage.getMessageId(),
					receivedMessage.getFilterTags(), receivedMessage.getXmlSourceMessage()));

		} else if (receivedMessage.isRpcGetSchema()) {
			consoleMessage("get-schema [" + receivedMessage.getMessageId() + "] message");
			send(theNe.assembleGetSchemaReply(receivedMessage.getMessageId(), receivedMessage.getXmlSourceMessage()));
		} else {
			consoleMessage("NO RULE for source message with id " + receivedMessage.getMessageId());
			consoleMessage(receivedMessage.getXmlSourceMessage());
		}
	}

	

	private void blockMessageQueue() {
		log.debug("block message queue for notifications");
		this.blockMessageQueueSend=true;
	}
	private void unblockMessageQueue() {
		log.debug("deblock message queue for notifications");
		this.blockMessageQueueSend=false;
	}

	private void sleepMilliSeconds(int delayMillis) {
		if (delayMillis > 0) {
			try {
				Thread.sleep(delayMillis);
			} catch (InterruptedException e) {
				log.warn("problem with threadsleep ("+this.getId()+"): "+e.getMessage());
			}
		}
	}

	private void sendCloseSession() throws IOException {
		log.debug("Sending close session.");
		Query query = QueryFactory.newCloseSession();
		query.setMessageId(String.valueOf(messageCounter++));
		send(query.toXML());
	}

	protected void doNotificationProcessing(UserCommand receivedMessage) throws IOException {
		
		log.info("User initiated Notification: " + receivedMessage.toString());
		String command = receivedMessage.getCommand();

		if (command.startsWith("dl")) {
			consoleMessage("Delay in seconds: " + msgDelaySeconds);
			consoleMessage("Message pattern: '" + msgPattern.pattern() + "'");

		} else if (command.startsWith("dp")) {
			msgPattern = setPattern(command.substring(2));
			consoleMessage("Set message pattern to '" + msgPattern.pattern() + "'");

		} else if (command.startsWith("dn")) {

			consoleMessage("Discard next incoming filtered get-message using pattern: '" + msgPattern.pattern() + "'");
			msgToDiscardCounter = 1;

		} else if (command.startsWith("d")) {

			try {
				msgDelaySeconds = Integer.valueOf(command.substring(1));
				consoleMessage(
						"New delay in seconds: " + msgDelaySeconds + " using pattern: '" + msgPattern.pattern() + "'");
			} catch (NumberFormatException e) {
				consoleMessage("Not a number. Unchanged delay in seconds: " + msgDelaySeconds);
			}

		} else {
			String msg = theNe.doProcessUserAction(receivedMessage.getCommand());
			if (msg != null) {
				send(msg); // Test purpose
				consoleMessage("Notification: " + msg);
			}
		}
		
	}

	/**
	 * Message to console
	 * 
	 * @param msg content
	 * @return again the msg
	 */
	protected String consoleMessage(String msg) {
		return console.cliOutput("MP" + this.hashCode() + ":" + msg);
	}

	/**
	 * Return the selected pattern
	 * 
	 * @param regex that should be used
	 * @return selected
	 */
	private static Pattern setPattern(String regex) {
		if (regex == null || regex.isEmpty()) {
			regex = ".*";
		}
		return Pattern.compile(regex);

	}
	private static class MyStringCollection extends ArrayList<String>
	{

		public int counts(String mid) {
			int c=0;
			for(String s: this)
			{
				if(mid.equals(s))
					c++;
			}
			return c;
		}
		
	}
	
}
