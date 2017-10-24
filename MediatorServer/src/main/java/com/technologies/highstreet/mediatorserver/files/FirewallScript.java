package com.technologies.highstreet.mediatorserver.files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.technologies.highstreet.mediatorserver.data.MediatorConfig;

public class FirewallScript {

	private static final String LR = MediatorCoreFiles.LR;

	public static void Create(String fnAdd,String fnRemove, MediatorConfig cfg) throws IOException {

		File file=new File(fnAdd);
		//fw_add.sh
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.write("#!/bin/bash"+LR);
		bw.write("#add iptables rule if not exists");
		bw.write("x=(`iptables -t nat -L | grep "+cfg.getDeviceIp()+"`)");
		bw.write("if [ -z $x ]; then ");
		bw.write("iptables -t nat -A PREROUTING -s "+cfg.getDeviceIp()+" -p udp --dport 162 -j REDIRECT --to-port "+cfg.getTrapPort());
		bw.write("fi");
		bw.flush();
		bw.close();
		file.setExecutable(true);
		//fw_remove.sh
		file=new File(fnRemove);
		bw = new BufferedWriter(new FileWriter(file));
		bw.write("#!/bin/bash"+LR);
		bw.write("#remove iptables rule if exists");
		bw.write("x=(`iptables -t nat -L | grep "+cfg.getDeviceIp()+"`)");
		bw.write("if ! [ -z $x ]; then ");
		bw.write("iptables -t nat -D PREROUTING -s "+cfg.getDeviceIp()+" -p udp --dport 162 -j REDIRECT --to-port "+cfg.getTrapPort());
		bw.write("fi");
		bw.flush();
		bw.close();
		file.setExecutable(true);


	}

}
