package namewebserver;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.IPAccessHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import settings.Settings;

public class WebService {

	public Server server;
	
	public WebService()
	{
		//CREATE CONFIG
		Set<Class<?>> s = new HashSet<Class<?>>();
        s.add(NamesWebResource.class);
        
        ResourceConfig config = new ResourceConfig(s);
		
        //CREATE CONTAINER
        ServletContainer container = new ServletContainer(config);
		
		//CREATE CONTEXT
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(new ServletHolder(container),"/*");
        
        //CREATE WHITELIST
        IPAccessHandler accessHandler = new IPAccessHandler();
        accessHandler.setWhite(Settings.getInstance().getWebAllowed());
        accessHandler.setHandler(context);
        
        //CREATE WEB SERVER
      	this.server = new Server(Settings.getInstance().getWebPort());
      	this.server.setHandler(accessHandler);
	}
	
	public void start()
	{
		try
        {
        	//START WEB 
			server.start();
		} 
        catch (Exception e) 
		{
        	//FAILED TO START WEB
		}
	}
	
	public void stop()
	{
		try 
        {
			//STOP RPC  
			server.stop();
		} 
        catch (Exception e) 
		{
        	//FAILED TO STOP WEB
		}
	}
}
