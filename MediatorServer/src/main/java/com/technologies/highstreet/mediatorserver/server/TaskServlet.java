package com.technologies.highstreet.mediatorserver.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.http.HttpStatus;

import com.technologies.highstreet.mediatorserver.data.MediatorLogEntryCollection;
import com.technologies.highstreet.mediatorserver.data.ServerSideMediatorConfig;
import com.technologies.highstreet.mediatorserver.data.StringCollection;
import com.technologies.highstreet.mediatorserver.files.MediatorCoreFiles;
import com.technologies.highstreet.mediatorserver.files.MediatorFiles;

public class TaskServlet extends HttpServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = -92317000086349966L;
	private static final String TASK_CREATE = "create";
	private static final String TASK_DELETE = "delete";
	private static final String TASK_GETCONFIG = "getconfig";
	private static final String TASK_GETLOG = "getlog";
	private static final String TASK_CLEARLOCK = "clearlock";
	private static final String TASK_GETNEMODELS = "getnemodels";
	private static final String TASK_GETDEVICES = "getdevices";
	private static final String TASK_START = "start";
	private static final String TASK_STOP = "stop";
	private static final String TASK_VERSION = "version";
	private static final String TASK_GETAVAILABLENCPORTS = "getncports";
	private static final String TASK_GETAVAILABLESNMPPORTS = "getsnmpports";
	private static final String TASK_GETSERVERCONFIG = "getserverconfig";
	private static final String TASK_REPAIR = "repair";

	private static final String UNKNOWN_TASK_RESPONSE = "{\"code\":0,\"message\":\"unknown task\"}";

	private static final Log LOG = LogFactory.getLog(TaskServlet.class);
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		doPost(req, resp);
	}
	private static final String LR = "\n";
    public static String executeInForeground(String script) throws IOException {
		ProcessBuilder pb = new ProcessBuilder(script.split(" "));
		Process p=pb.start();
		 InputStream is = p.getInputStream();
	     InputStreamReader isr = new InputStreamReader(is);
	     BufferedReader processOutput = new BufferedReader(isr);

	     InputStream errorStream = p.getErrorStream();
	     InputStreamReader inputStreamReader = new InputStreamReader(errorStream);
	     BufferedReader processErrorOutput = new BufferedReader(inputStreamReader);
	     String s="";
	     String output;
	     while( processErrorOutput.ready() &&
	        (output = processErrorOutput.readLine()) != null) {
	    	 s+=output+LR;
	     }
	     while ((output = processOutput.readLine()) != null) {
	    	 s+=output+LR;
	     }
	     processErrorOutput.close();
	     processOutput.close();
	     return s;
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setHeader("Access-Control-Allow-Origin", "*");
		resp.setHeader("Access-Control-Allow-Methods",  "GET, POST");
		resp.setStatus(HttpStatus.OK_200);
		String task = req.getParameter("task");
		if (task != null && task.length() > 0) {
			LOG.debug("on task: "+task);
			String response = "";
			if (task.equals(TASK_CREATE))
				response = this.onTaskCreate(req);
			else if (task.equals(TASK_DELETE))
				response = this.onTaskDelete(req);
			else if (task.equals(TASK_GETCONFIG))
				response = this.onTaskGetConfig(req);
			else if (task.equals(TASK_GETLOG))
				response = this.onTaskGetLog(req);
			else if (task.equals(TASK_CLEARLOCK))
				response = this.onTaskClearLock(req);
			else if (task.equals(TASK_GETNEMODELS))
				response = this.onTaskGetNEModels(req);
			else if (task.equals(TASK_GETDEVICES))
				response = this.onTaskGetDevices(req);
			else if (task.equals(TASK_START))
				response = this.onTaskStart(req);
			else if (task.equals(TASK_STOP))
				response = this.onTaskStop(req);
			else if (task.equals(TASK_VERSION))
				response = this.onTaskVersion(req);
			else if (task.equals(TASK_GETAVAILABLENCPORTS))
				response = this.onTaskGetAvailableNetconfPorts(req);
			else if (task.equals(TASK_GETAVAILABLESNMPPORTS))
				response = this.onTaskGetAvailableSNMPPorts(req);
			else if (task.equals(TASK_GETSERVERCONFIG))
				response = this.onTaskGetServerConfig(req);
			else if (task.equals(TASK_REPAIR))
				response = this.onTaskRepairConfigs(req);
			else
				response = UNKNOWN_TASK_RESPONSE;
			resp.getWriter().print(response);

		} else
			resp.getWriter().print(UNKNOWN_TASK_RESPONSE);

	}

	private String onTaskRepairConfigs(HttpServletRequest req) {

		String s;
		try {
			s="{\"code\":1,\"data\":"+MediatorFiles.RepairConfigs().toJSON()+"}";
		} catch (Exception e) {
			s="{\"code\":0,\"data\":\"error repairing config files\"}";
		}
		return s;
	}
	private String onTaskGetServerConfig(HttpServletRequest req) {

		return "{\"code\":1,\"data\":"+MediatorServerProperties.getInstance().toJSON()+"}";
	}
	private String onTaskVersion(HttpServletRequest req) {
		String bin="java -jar "+MediatorCoreFiles.MEDIATOR_JAR();
		String mediatorversion="",s="[";
		try {
			mediatorversion=executeInForeground(bin+" --version").trim();
			StringCollection nexmls=MediatorCoreFiles.GetNeModelFilenames();
			String nemodelpath=MediatorCoreFiles.NEMODELPATHREL();
			String komma="";
			for(String nexml : nexmls)
			{
				s+=String.format("%s{\"filename\":\"%s\",\"version\":\"%s\"}",komma,nexml,executeInForeground(bin+" --xmlversion "+nemodelpath+nexml).trim());
				komma=",";
			}
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}
		s+="]";
		return "{\"code\":1,\"data\":{\"server\":\""+WebAppServer.getVersionString()+"\",\"mediator\":\""+mediatorversion+"\",\"nexmls\":"+s+"}}";
	}

	private String onTaskGetAvailableSNMPPorts(HttpServletRequest req) {
		int limit = trygetInt(req, "limit", 10);
		int[] ports=ServerSideMediatorConfig.FindAvailableSNMPPorts(limit);
		String sPorts="";
		if(ports!=null && ports.length>0)
		{
			sPorts=String.format("%d",ports[0]);
			for(int i=1;i<ports.length;i++)
				sPorts+=String.format(",%d", ports[i]);
		}
		return "{\"code\":1,\"data\":["+sPorts+"]}";
	}

	private String onTaskGetAvailableNetconfPorts(HttpServletRequest req) {
		int limit = trygetInt(req, "limit", 10);
		int[] ports=ServerSideMediatorConfig.FindAvailableNetconfPorts(limit);
		String sPorts="";
		if(ports!=null && ports.length>0)
		{
			sPorts=String.format("%d",ports[0]);
			for(int i=1;i<ports.length;i++)
				sPorts+=String.format(",%d", ports[i]);
		}
		return "{\"code\":1,\"data\":["+sPorts+"]}";
	}


	private String onTaskGetNEModels(HttpServletRequest req) {
		return "{\"code\":1,\"data\":"
				+ MediatorCoreFiles.GetNeModelFilenames().toJSON() + "}";
	}
	private String onTaskGetDevices(HttpServletRequest req) {
		String devicesJSON="[]";
		String bin="java -jar "+MediatorCoreFiles.MEDIATOR_JAR();
		try {
			devicesJSON=executeInForeground(bin+" --devices").trim();
			if(devicesJSON==null || devicesJSON.trim()=="")
				devicesJSON="[]";
		}
		catch (IOException e) {
			LOG.error(e.getMessage());
		}
		return "{\"code\":1,\"data\":"
				+ devicesJSON + "}";
	}

	private String onTaskStop(HttpServletRequest req) {
		String msg;
		try {
			MediatorFiles.DeleteRun(req.getParameter("name"),true);

			msg = "{\"code\":1,\"data\":\"stopping mediator\"}";
		} catch (Exception err) {
			msg = "{\"code\":0,\"data\":\"" + err.getMessage() + "\"}";
		}
		return msg;
	}

	private String onTaskStart(HttpServletRequest req) {
		String msg;
		try {
			MediatorFiles.CreateRun(req.getParameter("name"),true);
			msg = "{\"code\":1,\"data\":\"starting mediator\"}";
		} catch (Exception err) {
			msg = "{\"code\":0,\"data\":\"" + err.getMessage() + "\"}";
		}
		return msg;
	}

	private String onTaskClearLock(HttpServletRequest req) {
		String msg;
		try {
			MediatorFiles.ClearLock(req.getParameter("name"));
			msg="{\"code\":1,\"data\":\"cleared succesfully\"}";
		} catch (Exception err) {
			msg = "{\"code\":0,\"data\":\"" + err.getMessage() + "\"}";
		}
		return msg;

	}

	private String onTaskGetLog(HttpServletRequest req) {
		String msg;
		try {
			MediatorLogEntryCollection c = MediatorFiles.GetLog(req.getParameter("name"), trygetInt(req,"limit",100));
			msg="{\"code\":1,\"data\":"+c.toJSON()+"}";
		} catch (Exception err) {
			msg = "{\"code\":0,\"data\":\"" + err.getMessage() + "\"}";
		}
		return msg;
	}

	private int trygetInt(HttpServletRequest req, String s, int def) {
		try
		{
			def=Integer.parseInt(get(req,s,String.format("%d",def)));
		}
		catch(Exception err)
		{

		}
		return def;
	}
	private String get(HttpServletRequest req,String p, String def) {
		String s=req.getParameter(p);
		if(s==null | s.trim().length()<=0)
			s=def;
		return s;
	}

	private String onTaskGetConfig(HttpServletRequest req) {
		String name = req.getParameter("name");
		String msg;
		try {
			if (name == null || name.length() <= 0)// return all data
			{
				msg = "{\"code\":1,\"data\":"+MediatorFiles.GetConfigs().toJSON()+ "}";

			} else // return filtered data
			{
				msg ="{\"code\":1,\"data\":"+ MediatorFiles.GetConfigsFiltered(name).toJSON()+ "}";

			}
		} catch (Exception err) {
			msg = "{\"code\":0,\"data\":\"" + err.getMessage() + "\"}";
		}
		return msg;
	}

	private String onTaskDelete(HttpServletRequest req) {
		String msg;
		try {
			MediatorFiles.Delete(req.getParameter("name"));
			msg = "{\"code\":1,\"data\":\"deleted successful\"}";
		} catch (Exception err) {
			msg = "{\"code\":0,\"data\":\"" + err.getMessage() + "\"}";
		}
		return msg;
	}

	private String onTaskCreate(HttpServletRequest req) {
		String msg;
		try {
			Map<String,String[]> params=req.getParameterMap();
			System.out.println(params);
			List<ServerSideMediatorConfig> configs = ServerSideMediatorConfig.FromJSON(req
					.getParameter("config"));
			if(configs.size()>0)
			{
				for(ServerSideMediatorConfig cfg : configs)
					MediatorFiles.Create(cfg.getName(), cfg);
			}
			msg = "{\"code\":1,\"data\":\"created successful\"}";
			
		} catch (Exception err) {
			msg = "{\"code\":0,\"data\":\"" + err.getMessage() + "\"}";
		}
		return msg;
	}
}
