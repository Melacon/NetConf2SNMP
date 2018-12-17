package com.technologies.highstreet.netconf2snmpmediator.test;

public class MainClassTest {

	public static void main(String[] args)
	{
		System.out.println("started");
		while(!Thread.currentThread().isInterrupted()){
		    try{
		        Thread.sleep(10);
		    }
		    catch(InterruptedException e){
		        Thread.currentThread().interrupt();
		        System.out.println("interrupt");
		        break; //optional, since the while loop conditional should detect the interrupted state
		    }
		    catch(Exception e){
		        System.out.println("error:"+e.getMessage());
		    }
		}
		System.out.println("stopped");
	}
}
