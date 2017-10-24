package com.technologies.highstreet.mediatorserver.files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.technologies.highstreet.mediatorserver.data.MediatorConfig;

public class MediatorStopScript {

	public static final String LR = MediatorCoreFiles.LR;

	public static void Create(String filename, MediatorConfig cfg) throws IOException {

		File file=new File(filename);
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.write("#kill process"+LR);
		bw.write("MED_HOME=\"mediators/"+cfg.getName()+"/\""+LR);
		bw.write("PIDFILE=$MED_HOME\""+cfg.getName()+".pid\""+LR);
		bw.write("kill -9 `cat $PIDFILE`");
		bw.flush();
		bw.close();
		file.setExecutable(true);
	}

}
