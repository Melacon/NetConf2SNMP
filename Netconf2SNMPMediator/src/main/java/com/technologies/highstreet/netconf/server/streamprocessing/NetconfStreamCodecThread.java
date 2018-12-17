/**
 * Netconf stream codec
 *
 * Processing of incomming and outgoing netconf messages. Does support encoding of NETCONF 1.0 !
 *
 * @author Herbert (herbert.eiselt@highstreet-technologies.com)
 */

package com.technologies.highstreet.netconf.server.streamprocessing;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;

import javax.swing.plaf.SliderUI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sshd.server.ExitCallback;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.technologies.highstreet.netconf.server.transport.ServerTransportContentParser;
import com.technologies.highstreet.netconf.server.types.NetconfSender;
import com.technologies.highstreet.netconf.server.types.NetconfSessionStatus;
import com.technologies.highstreet.netconf.server.types.NetconfSessionStatusHolder;

import net.i2cat.netconf.messageQueue.MessageQueue;

public class NetconfStreamCodecThread implements Runnable, NetconfSender {

    private static final Log log = LogFactory.getLog(NetconfStreamCodecThread.class);

    private static final String                END_CHAR_SEQUENCE    = "]]>]]>"; //EOM old
    //private static final String                CHUNK                = "\n#";    //Chunk indicator

	private static final boolean CLOSE_STREAM_ON_ERROR = true;

    private final ExitCallback      callback;
    private final InputStream       in;
    private final InputStreamReader isr;
    private final OutputStream      out;

    private ServerTransportContentParser xmlParser;
    private XMLReader xmlReader;

    private int messageNo = 0;
    private final NetconfSessionStatusHolder status;
    private int r;

	private String remoteIp;


    public NetconfStreamCodecThread(InputStream in, OutputStream out, ExitCallback callback,
            NetconfSessionStatusHolder status, MessageQueue messageQueue) {

        this.in = in;
        this.out = out;
        this.status = status;
        this.callback = callback;
        this.xmlParser = new ServerTransportContentParser();
        this.xmlParser.setMessageQueue(messageQueue);

        this.isr = new InputStreamReader(this.in);

        // initialize XML parser & handler and message queue
        try {
            xmlReader = XMLReaderFactory.createXMLReader();
            xmlReader.setContentHandler(xmlParser);
            xmlReader.setErrorHandler(xmlParser);
        } catch (SAXException e) {
            log.error("Cannot instantiate XML parser", e);
            return;
        }

    }

    public Thread start() {
        Thread res = new Thread(this, "Client thread");
        res.start();
        return res;
    }

    @Override
    public void run() {
        readMessageLoop(new StringBuilder());
    }

    private void readMessageLoop(final StringBuilder message) {
        // process messages
        try {

            while (! status.equals(NetconfSessionStatus.SESSION_CLOSED)) {

                // read message

                while ( true ) {
                    r = isr.read();
                    if (r == -1) {
                    {
                    	log.trace("stop reading. current message: "+message.toString());
                    	break;
                    }
                    } else {                         //Add char to message
                        char c =(char)r;
                        //log.debug(getTS()+" Char: "+c);
                        if (message.length() == 0) {
                            log.debug(" Start reading new message "+messageNo);
                        }
                        log.trace("appending char: "+r+" "+c);
                        message.append(c);


                        String messageString = message.toString();
                        if (messageString.endsWith(END_CHAR_SEQUENCE)) {
                            //End of message
                            log.trace(" Detect end message 1 "+messageNo);
                            // process data
                            process(messageString);
                            // reset message
                            log.trace(" End reading+processing message "+messageNo+++"... "+message.length());
                            message.setLength(0);
                            break;
                        }
                     }
                 }

                // exit loop if stream closed
                //break;
                 try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                	log.warn("problem with threadsleep: "+e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Exception caught in Netconf subsystem", e);
        } finally {
        	if(CLOSE_STREAM_ON_ERROR)
        	{
        		status.change(NetconfSessionStatus.SESSION_CLOSED);
        		callback.onExit(0);		
        	}
        }
    }


    private void process(String message) throws IOException, SAXException {
        log.trace("Starting parser..");
        // remove end char sequence from message
        message = message.replace(END_CHAR_SEQUENCE, "");
        xmlParser.setXmlSourceMessage(message);

        try {
            //log.debug("Parsing message:\n" + message);
            xmlReader.parse(new InputSource(new StringReader(message)));
        } catch (SAXException e) {
            log.error("Error parsing. Message: \n" + message, e);
            status.change(NetconfSessionStatus.SESSION_CLOSED);
        } catch (Exception e) {
            log.error("Error parsing message", e);
            status.change(NetconfSessionStatus.SESSION_CLOSED);
        }
        log.trace("End of parsing.");
    }

    @Override
    synchronized public void send(String xmlMessage) throws IOException {
        log.trace("Sending message:\n" + xmlMessage);
        out.write(xmlMessage.getBytes("UTF-8"));
        // send final sequence
        out.write(END_CHAR_SEQUENCE.getBytes("UTF-8"));
        out.flush();
    }

	public String getRemoteIp() {
		return this.remoteIp;
	}

	@Override
	public void clearOutputStream() {
		//TODO 
		log.debug("clear output stream currently not implemented");
		
	}

}
