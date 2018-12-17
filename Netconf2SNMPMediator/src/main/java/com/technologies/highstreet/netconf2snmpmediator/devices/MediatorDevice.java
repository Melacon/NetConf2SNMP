package com.technologies.highstreet.netconf2snmpmediator.devices;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

import com.technologies.highstreet.mediatorlib.netconf.server.basetypes.Console;
import com.technologies.highstreet.netconf2snmpmediator.server.SNMPMediatorConfig;
import com.technologies.highstreet.netconf2snmpmediator.server.networkelement.IOnPluginEventListener;
import com.technologies.highstreet.netconf2snmpmediator.server.networkelement.IOnTrapReceivedListener;
import com.technologies.highstreet.netconf2snmpmediator.server.networkelement.Netconf2SNMPNetworkElement;

public class MediatorDevice {

	public static class DeviceInfos 
	{
		private final long id;
		private final String vendor;
		private final String device;
		private final String version;
		private final Class<?> neClass;
		private final String defaultXmlFilename;

		public DeviceInfos(String vendor,String device,String version,Class<?> necls, String defaultXml)
		{
			this.vendor = vendor;
			this.device = device;
			this.version = version;
			this.id = this.createHash(vendor,device,version);
			this.neClass = necls;
			this.defaultXmlFilename = defaultXml;
		}

		private long createHash(String a, String b,String c) {
			CRC32 x=new CRC32();
			x.update(a.getBytes());
			x.update(b.getBytes());
			x.update(c.getBytes());
			return x.getValue();
		}

		public long getId() {
			return this.id;
		}
		public String toJSON()
		{
			return "{"+
					"\"vendor\":\""+this.vendor+"\","+
					"\"device\":\""+this.device+"\","+
					"\"version\":\""+this.version+"\","+
					"\"xml\":\""+this.defaultXmlFilename+"\","+
					"\"id\":"+this.id+""+				
					"}";
		}
	}
	private static final List<DeviceInfos> devices =  new ArrayList<DeviceInfos>();
	public static DeviceInfos register(String vendor,String device,String version,Class<?> neClass, String defaultXml)
	{
		DeviceInfos i=new  DeviceInfos(vendor, device,version,neClass,defaultXml);
		devices.add(i);
		return i;
	}
	public static DeviceInfos getByHashOrDefault(long h)
	{
		for(DeviceInfos info:devices)
		{
			if(info.id==h)
				return info;
		}
		return null;
	}
	private static Class<?> getByHashOrDefault(long h, Class<?> def)
	{
		for(DeviceInfos info:devices)
		{
			if(info.id==h)
			{
				if(info.neClass!=null)
					def=info.neClass;
				break;
			}
		}
		return def;
	}
	public static String getJSON()
	{
		String s="[";
		if(devices.size()>0)
			s+=devices.get(0).toJSON();
		for(int i=1;i<devices.size();i++)
			s+=","+devices.get(i).toJSON();
		s+="]";
		return s;
	}
	public static Netconf2SNMPNetworkElement getNetworkElementInstance(SNMPMediatorConfig cfg, String xmlFilename,
			String yangPath, String uuid, Console server,IOnPluginEventListener l1,IOnTrapReceivedListener l2, Class<?> defaultClass) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		
		Class<?> cls = getByHashOrDefault(cfg.getDeviceType(), defaultClass );
		Class<?>[] cArg = new Class<?>[] {
			String.class,
			String.class,
			String.class,
			long.class,
			String.class,
			int.class,
			int.class,
			Console.class,
			IOnPluginEventListener.class,
			IOnTrapReceivedListener.class};
		Netconf2SNMPNetworkElement obj=(Netconf2SNMPNetworkElement) cls.getConstructor(cArg).newInstance(xmlFilename, yangPath, uuid, cfg.getDeviceType(),
                cfg.getDeviceIp(),cfg.getDevicePort(), cfg.getTrapPort(), server,l1,l2);
		return obj;
	}

}
