package org.nioto.ws;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

@Path("login")
@Produces({ MediaType.APPLICATION_JSON})
public class LoginService {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger( LoginService.class.getSimpleName());

	@XmlRootElement
	public static class Return implements Serializable{
		private static final long serialVersionUID = 1L;
		String content ;
		public Return(){
			
		}
		public Return(String str) {
			this.content = str;
		}
		
		public void setContent(String content) {
			this.content = content;
		}
		public String getContent() {
			return this.content;
		}
	}
	
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
	public Return  getLogin2( int a ){
		return new Return("login with (int)" + a);
	}
	@Path("test3/{id}")
	@GET
	public Return getLogin3( @PathParam("id") int id ){
		return new Return("login with pathParam : " + id );
	}
}
