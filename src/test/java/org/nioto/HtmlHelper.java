package org.nioto;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;

import javax.ws.rs.FormParam;

import org.codehaus.jackson.map.ObjectMapper;
import org.nioto.GenerateTestsServlet.FieldInfo;
import org.nioto.ws.Return;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlHelper {

	private final static Logger log = LoggerFactory.getLogger(HtmlHelper.class);

	Random rand = new Random();
  static ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
	int counter;
	private final static String METHOD_NAME_PREFIX="__run";
	
	public HtmlHelper(){
		counter = 0;
	}
	
	public void incCounter(){
		this.counter++;
	}
	
	public StringBuilder getScript(String wsFunctionName, List<FieldInfo> list, Method m){
		StringBuilder  sb = new StringBuilder ("<script>");
		sb.append( "var ").append( METHOD_NAME_PREFIX).append( this.counter) .append("=function(id){")
			.append( "var params = {  }; ");
		Object[] params = new Object[list.size()];
		int i =0;
		boolean hasFormParam =false;
		for (FieldInfo fieldInfo : list) {
			Class<?> clazz = fieldInfo.getAnn();
			params[i] = getDummy(fieldInfo.getClazz()) ; 
			String data = toJson(params[i]);
			i++;
			String name;
			if( clazz== null ) {
				name= "$entity";
			} else {
				name = fieldInfo.getName();
			}
			sb.append("params.").append( name).append("=").append( data ).append( ';' );
			if( ! hasFormParam && fieldInfo.getAnn() !=null && fieldInfo.getAnn().isAssignableFrom(FormParam.class)) {
				hasFormParam =true;
			}
		}
		sb.append( "params.$contentType = ");
		if( hasFormParam ) {
			sb.append( "'application/x-www-form-urlencoded'");
		} else {
			sb.append( "'application/json'");
		}
		sb.append( ";\n");
		sb.append( "var result = " + toJson(call(m, params))).append( ";\n");
		sb.append( "setContent(id+'-result', '<pre>'+JSON.stringify(result)+'</pre>');" );
		sb.append( "var liveResult = ").append(wsFunctionName).append("(params);").append( ";\n");
		sb.append( "setContent(id,  '<pre>'+JSON.stringify(liveResult)+'</pre>');" );
		sb.append( " if (Object.identical(result, liveResult))")
			.append("{ setContent(id+'-check', '<span class=\"label success\">PASS</span>');}")
			.append(" else { setContent(id+'-check', '<span class=\"label error\">FAIL</span>');}");
		sb.append('}');
		sb.append("</script>");
		return sb;
	}
	
	private Object call( Method m, Object[] args) {
		Class<?> clazz = m.getDeclaringClass();
		Object res ;
		try {
			Object obj = clazz.getConstructor().newInstance();
		  res = m.invoke(obj, args);
		} catch (Exception e) {
			log.error( "Error invoking dynamicly a method " ,e);
			res = e.getMessage();
		}
		return res;
	}
	private Object getDummy(Class<?> clazz) {
		Object data =null;
		if ( clazz.equals( int.class)) {
			data = rand.nextInt();
		}  else  if ( clazz.equals( long.class)) {
			data =  rand.nextLong() ;
		} else  if ( clazz.equals( float.class)) {
			data =rand.nextFloat();
		}  else  if ( clazz.equals( double.class)) {
			data = rand.nextDouble();
		}  else if ( clazz.equals( String.class)) {
			data =  new BigInteger(130, rand).toString(32);
		} else if ( clazz.equals(Return.class) ){
			data = new Return(new BigInteger(130, rand).toString(32) );
		}
		return data;
	}
	private String toJson(Object data) {
		String res;
		try {
			res = mapper.writeValueAsString( data);
		} catch (Exception e) {
			log.error("Error converting to Json ",e);
			res = e.getMessage();
		}
		return res;
	}

	public StringBuilder getHtml(String functionName) {
		StringBuilder sb = new StringBuilder();
		String id = "id"+this.counter;
		sb.append( "<div class='row'>")
				.append("<div class='span5'><h4>").append( functionName).append("</h4></div>")
				.append("<div class='span1'><button class='btn success' onclick=\"").append( METHOD_NAME_PREFIX).append( this.counter) .append("('").append(id).append("');\">Run</button></div>")
				.append( "<div class='span4' id='").append(id).append( "'></div>")
				.append( "<div class='span4' id='").append(id).append( "-result'></div>")
				.append( "<div class='span1' id='").append(id).append( "-check'></div>")
				.append("</div>\n");
		return sb;
	}	
} 
