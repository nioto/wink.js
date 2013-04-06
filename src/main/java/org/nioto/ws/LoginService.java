package org.nioto.ws;

import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("login")
@Produces({ MediaType.APPLICATION_JSON})
public class LoginService {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger( LoginService.class.getSimpleName());

	@Path("test")
	@GET
	public String getLogin(@QueryParam("test") String str){
		return "login";
	}

	@Path("test2")
	@GET
	public String getLogin2( int a ){
		return "login";
	}
	@Path("test3/{id}")
	@GET
	public String getLogin3( @PathParam("id") int id ){
		return "login";
	}
}
