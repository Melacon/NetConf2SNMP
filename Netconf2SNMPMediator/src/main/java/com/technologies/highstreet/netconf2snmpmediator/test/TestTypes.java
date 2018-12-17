/**
 *
 */
package com.technologies.highstreet.netconf2snmpmediator.test;

import java.util.ArrayList;
import java.util.List;

import com.technologies.highstreet.mediatorlib.netconf.server.networkelement.NodeEditConfig;

/**
 * @author herbert
 *
 */
public class TestTypes {

    private static void test(String converter) {

        NodeEditConfig node = new NodeEditConfig(null, null, "1.2.3", converter, "read-write","",null);
        System.out.println("Test pattern: "+converter);
        String value;
        value = "0";
        System.out.println("In: "+value+" Out:"+node.convertValueSnmp2Netconf(value));
        value = "1";
        System.out.println("In: "+value+" Out:"+node.convertValueSnmp2Netconf(value));
        value = "2";
        System.out.println("In: "+value+" Out:"+node.convertValueSnmp2Netconf(value));
        value = "3";
        System.out.println("In: "+value+" Out:"+node.convertValueSnmp2Netconf(value));
        value = "133";
        System.out.println("In: "+value+" Out:"+node.convertValueSnmp2Netconf(value));
    }

    private static void testValidators(String value,String regex)
    {
    	 NodeEditConfig node;
    	 node = new NodeEditConfig(null, null, "1.2.3", "", "read-write","",regex);
         System.out.println("val: "+value+"\t"+ "valid:"+node.validate(value)+"\t"+"validator:"+node.getValidatorString());
    }

 
    /**
     * @param args
     */
    public static void main(String[] args) {


        test("int-to-boolean");
        test("int-to-boolean-2,3-true");
        test("int-to-boolean-3-true");
        test("int-to-boolean-3-false");
        test("int-to-boolean.dsa");
        test("map-1-d1-d2");
        test("divide-10");

        testValidators("123", NodeEditConfig.CommonValidators.SignedNumeric);
        testValidators("-123", NodeEditConfig.CommonValidators.UnsignedNumeric);
        testValidators("123.34", NodeEditConfig.CommonValidators.SignedNumeric);
        testValidators("123.34",NodeEditConfig.CommonValidators.SignedDecimal);
        testValidators("-123.34", NodeEditConfig.CommonValidators.SignedDecimal);
        testValidators("-123.34", NodeEditConfig.CommonValidators.UnsignedDecimal);

        testStringArray();
        testSimpleFifo();
 }

	private static void testStringArray() {
		List<String> list= new ArrayList();
		String s1="String1",s2="String2",s3="String3",s4="String4";
		String stest="String1";
		list.add("String1");
		list.add("String2");
		list.add("String3");
		list.add("String4");
		
		if(list.contains("String3"))
			System.out.println("list of strings works as expected");
		else
			System.err.println("list of strings is not as easy as expected");
		
		list.clear();
		list.add(s1);
		list.add(s2);
		list.add(s3);
		list.add(s4);
		
		if(list.contains(stest))
			System.out.println("list of strings works as expected");
		else
			System.err.println("list of strings is not as easy as expected");
		
	}
	
	private static void testSimpleFifo() {
		int maxSize=5;
		List<String> fifo = new ArrayList<String>();
		//fill fifo to max
		for(int i=0;i<maxSize;i++)
			fifo.add("entry"+i);
		System.out.println("start="+fifo.toString());
		fifo.add("entry"+maxSize);
		if(fifo.size()>maxSize)
			fifo.remove(0);
		System.out.println("after rm="+fifo.toString());
		fifo.add("entry"+(maxSize+1));
		if(fifo.size()>maxSize)
			fifo.remove(0);
		System.out.println("after rm2="+fifo.toString());
		
		
		
	}



}
