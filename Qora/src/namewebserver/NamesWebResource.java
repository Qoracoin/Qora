package namewebserver;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import qora.naming.Name;
import controller.Controller;


@Path("{name}")
public class NamesWebResource 
{
	@GET
	public Response getNames(@PathParam("name") String nameName)
	{
		Name name = Controller.getInstance().getName(nameName);
		
		//CHECK IF NAME EXISTS
		if(name == null)
		{
			return Response.status(404)
					.header("Content-Type", "text/html; charset=utf-8")
					.entity("<html><title>404 Not Found</title><body><h1>name does not exist</h1><hr>© <a href=http://www.qora.org>Qora</a></body></html>")
					.build();
		}
		
		String Value = name.getValue().toString();
		
		//REDIRECT
		if(Value.startsWith("http://") || Value.startsWith("https://"))
		{
			return Response.status(302)
					.header("Location", Value)
					.build();
		}
		
		//SHOW WEB-PAGE
		return Response.status(200)
				.header("Content-Type", "text/html; charset=utf-8")
				.entity(Value)
				.build();
	}	
}
