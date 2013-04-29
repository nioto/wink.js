package org.nioto.ws;

import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("echo")
@Produces({ MediaType.APPLICATION_JSON})
public class EchoWebService {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger( EchoWebService.class.getSimpleName());

	@GET
	public Return getText(){
		return new Return("Hello there");
	}
}
