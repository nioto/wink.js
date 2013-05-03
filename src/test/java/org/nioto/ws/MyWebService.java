package org.nioto.ws;

import java.util.logging.Logger;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("test")
@Produces({ MediaType.APPLICATION_JSON})
public class MyWebService {

	@SuppressWarnings("unused")
	private final static Logger logger = Logger.getLogger( MyWebService.class.getSimpleName());

	@Path("static")
	@GET
	public Return getStaticText(){
		return new Return("some static text");
	}
	
	@Path("test")
	@GET
	public Return getLogin(@QueryParam("test") String str){
		return new Return("login  with " + str) ;
	}

	@Path("test2")
	@POST // http://www.michaelwilliams.co.za/no-content-to-map-to-object-due-to-end-of-input/
	public Return  getLogin2( int aa){
		return new Return("login with (int)" + aa);
	}
	@Path("test3/{id}")
	@GET
	public Return getLogin3( @PathParam("id") int id ){
		return new Return("login with pathParam : " + id );
	}
	@Path("test4")
	@POST
	public Return getLogin4( Return id ){
		return new Return("login with pathParam : " + id );
	}
	@Path("test5")
	@GET
	public Return getLogin5( @CookieParam(value="cookie") int id ){
		return new Return("login with pathParam : " + id );
	}
	@Path("test6")
	@GET
	public Return getLogin6( @MatrixParam("toto") String id ){
		return new Return("login with pathParam : " + id );
	}
	@Path("test7")
	@POST
	public Return getLogin7( @FormParam("toto") String id ){
		return new Return("login with pathParam : " + id );
	}
}
