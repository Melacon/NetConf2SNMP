package com.technologies.highstreet.mediatorserver.files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.technologies.highstreet.mediatorserver.data.ServerSideMediatorConfig;

public class FirewallScript {

	private static final String LR = MediatorCoreFiles.LR;

	public static void Create(String fnAdd,String fnRemove, ServerSideMediatorConfig cfg) throws IOException {

		File file=new File(fnAdd);
		//fw_add.sh
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.write("#!/bin/bash"+LR);
		bw.write("#add iptables rule if not exists"+LR);
		bw.write("x=(`iptables -t nat -L -n | grep "+cfg.getDeviceIp()+"`)"+LR);
		bw.write("if [ -z $x ]; then "+LR);
		bw.write("iptables -t nat -A PREROUTING -s "+cfg.getDeviceIp()+" -p udp --dport 162 -j REDIRECT --to-port "+cfg.getTrapPort()+LR);
		bw.write("fi"+LR);
		bw.flush();
		bw.close();
		file.setExecutable(true);
		//fw_remove.sh
		file=new File(fnRemove);
		bw = new BufferedWriter(new FileWriter(file));
		bw.write("#!/bin/bash"+LR);
		bw.write("#remove iptables rule if exists"+LR);
		bw.write("x=(`iptables -t nat -L -n | grep "+cfg.getDeviceIp()+"`)"+LR);
		bw.write("if ! [ -z $x ]; then "+LR);
		bw.write("iptables -t nat -D PREROUTING -s "+cfg.getDeviceIp()+" -p udp --dport 162 -j REDIRECT --to-port "+cfg.getTrapPort()+LR);
		bw.write("fi"+LR);
		bw.flush();
		bw.close();
		file.setExecutable(true);


	}

}
