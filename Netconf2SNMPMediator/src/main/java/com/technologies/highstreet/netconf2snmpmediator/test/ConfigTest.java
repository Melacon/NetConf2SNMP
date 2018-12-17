package com.technologies.highstreet.netconf2snmpmediator.test;

import com.technologies.highstreet.netconf2snmpmediator.server.Program;

public class ConfigTest {

	public static void main(String[] args)
	{
		String configJsonfile="mediators/sim1/sim1.config";
		String statusFilename=Program.getStatusFilename(configJsonfile);
		System.out.println(statusFilename);
	}
}
