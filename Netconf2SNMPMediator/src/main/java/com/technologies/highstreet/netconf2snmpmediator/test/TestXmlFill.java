package com.technologies.highstreet.netconf2snmpmediator.test;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.technologies.highstreet.mediatorlib.netconf.server.basetypes.Console;
import com.technologies.highstreet.mediatorlib.netconf.server.networkelement.NodeEditConfigCollection;
import com.technologies.highstreet.netconf2snmpmediator.server.networkelement.Netconf2SNMPConnector;
import com.technologies.highstreet.netconf2snmpmediator.server.networkelement.Netconf2SNMPNetworkElement;
import com.technologies.highstreet.netconf2snmpmediator.server.networkelement.SimulatorNetworkElement;

public class TestXmlFill {

    public static void main(String[] args)
    {
        String path="/home/herbert/odl/Netconf2SNMP/Netconf2SNMP/Netconf2SNMPMediator/";
        String xmlFilename=path+"xmlNeModel/DVM_MWCore12_BasicAir.xml";
        String yangPath=path+"yang/yangNeModel";
        String uuid="";
        String deviceIp="172.16.199.101";//DWLab dw1
        long deviceType = 0;

        int trapPort = 0;
        Console server = msg -> {
		    System.out.println(msg);
		    return msg;
		};

        try {
            //find all Nodes with oids
            Netconf2SNMPNetworkElement ne = new SimulatorNetworkElement(xmlFilename, yangPath, uuid, deviceType ,
                    deviceIp,161, trapPort, server,null,null) ;
            Netconf2SNMPConnector connector = new Netconf2SNMPConnector(ne, server,null,null);
            NodeEditConfigCollection snmpNodes = ne.getSNMPNodes();
            //do snmp requests
            connector.onPreReplyMessage("", null, snmpNodes);
            //output filled xml file
            System.out.println(ne.getXmlSubTreeAsString("//data"));

        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }
}
