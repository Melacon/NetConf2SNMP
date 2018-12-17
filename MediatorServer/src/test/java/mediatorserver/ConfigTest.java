package mediatorserver;

import java.util.List;

import com.technologies.highstreet.mediatorlib.data.PortRange;
import com.technologies.highstreet.mediatorserver.data.ServerSideMediatorConfig;

public class ConfigTest {

	private static final String JSON_TEST_TEMPLATE="[]";
	
	
	private static void testPortRange()
	{
		PortRange pr = new PortRange(1000, 2000);
		pr.AddException(1004);
		System.out.println("portrange="+pr.toString());
		System.out.println("1003 is allowed?"+pr.IsAvailable(1002));
	}
	private static void testConfig()
	{
		try {
			List<ServerSideMediatorConfig> configs=ServerSideMediatorConfig.FromJSON(JSON_TEST_TEMPLATE);
			
			
			System.out.println("found "+configs.size()+" configs");
			for(ServerSideMediatorConfig config:configs)
				System.out.println(config.toString());
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	public static void main(String[] args)
	{
		testConfig();
		testPortRange();
	}
}
