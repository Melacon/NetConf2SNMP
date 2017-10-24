package com.technologies.highstreet.mediatorserver.files;

import java.io.File;
import java.io.FileFilter;


import com.technologies.highstreet.mediatorserver.data.StringCollection;

public class MediatorCoreFiles {

	//Windows
/*	public static final String DS="\\";
	public static final String HOME = "C:\\snmp"+DS;
	public static final String LR = "\r\n";
*/
	//Linux

	public static final String DS="/";
	public static String HOME = "";//EnvMap.GetInstance().get("NETCONF2SNMP_HOME");// /opt/snmp"+DS;
	public static final String LR = "\n";


	public static String BIN(){return HOME+"bin"+DS;}
	public static String YANGPATH(){return HOME+"yang"+DS;}
	public static String NEMODELPATHREL(){return "nemodel"+DS;}
	public static String NEMODELPATH(){return HOME+NEMODELPATHREL();}
	public static String MEDIATORSPATH(){return HOME+"mediators"+DS;}

	public static void SetHome(String dir)
	{
		if(!dir.endsWith(DS))
			dir+=DS;
		HOME = dir;
		MediatorFiles.HOME = MEDIATORSPATH();
	}
	public static StringCollection GetNeModelFilenames()
	{
		String p=NEMODELPATH();
		File f=new File(p);
		StringCollection list=new StringCollection();
		if(f.exists() && f.isDirectory())
		{
			File[] xmlfiles=f.listFiles(new FileFilter(){
				@Override
				public boolean accept(File pathname) {
					return pathname.getName().toLowerCase().endsWith(".xml");
				}
			});
			if(xmlfiles!=null && xmlfiles.length>0)
			{
				for(File fx:xmlfiles)
				{
					list.add(fx.getName());
				}
			}
		}
		return list;
	}

}
