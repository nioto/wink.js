/**
 * 
 */
package org.nioto.winkjs.writers;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.wink.common.internal.registry.metadata.ClassMetadata;
import org.apache.wink.common.internal.registry.metadata.MethodMetadata;
import org.apache.wink.server.internal.DeploymentConfiguration;
import org.apache.wink.server.internal.registry.ResourceRecord;
import org.apache.wink.server.internal.registry.SubResourceRecord;
import org.nioto.winkjs.Utils;

/**
 *  Abstract Class for JS Client code generation
 * 
 * @author nioto
 */
public abstract class AbstractJSWriter {

	/**
	 * Available Javascript Frameworks supported
	 * ( mootools, RestEasyJS client, JQuery )
	 * @author tonio
	 */
	protected static enum FRAMEWORK {
		JQUERY("jquery"), RESTEASY("resteasy"), MOOTOOLS("mootools"); 
		private String jsFramework;
		private FRAMEWORK(String name) {
			this.jsFramework = name;
		}
		public String getJsFramework() {
			return jsFramework;
		}
		public static FRAMEWORK getFramework(String name){
			for (FRAMEWORK  f  : values()) {
				if( f.name().equalsIgnoreCase(name)) {
					return f;
				}
			}
			// return default : RESTEASY
			return RESTEASY;
		}
	}
	
	private FRAMEWORK type;
	
	protected  AbstractJSWriter(FRAMEWORK type){
		this.type = type ;
	}
	public  FRAMEWORK getType(){
		return this.type;
	}
	/**
	 *  Allow to prepend Javascript Code before generating the Client code.
	 * @param uri base URI for the webservices
	 * @param sb {@link StringBuilder} containing the JS script generated
	 */
	protected  void appendPreScript(String uri, StringBuilder sb) {
		sb.append( "// BEGIN of  ").append(this.type).append( " client generation\n");
	}
	/**
	 *  Allow to post append Javascript Code after full generated the Client code.
	 * @param uri base URI for the webservices
	 * @param sb {@link StringBuilder} containing the JS script generated
	 */
	protected void appendPostScript(String uri, StringBuilder sb) {
		sb.append( "// END of client generation");
	}
	/**
	 * Main method to generate the Javascript Client  code
	 * 
	 * @param uri base URI for the webservices
	 * @param conf  Wink Configuration
	 * @return The Javascript client script  
	 * @throws IOException 
	 */
	public final StringBuilder generateJavaScript(String uri, DeploymentConfiguration conf) throws IOException {
		StringBuilder script = new StringBuilder();
		appendPreScript(uri, script );
		// do 
		Set<String> declaredPrefixes = new HashSet<String>();
		generateServices(script, conf, declaredPrefixes);
		//end
		appendPostScript(uri, script);
		return script;
	}
	/**
	 *  generate all JS code for calling WS. 
	 * @param script Stringbuilder to append Javascript code 
	 * @param conf  Wink Configuration
	 * @param declaredPrefixes list of prefix from class 
	 */
	private void generateServices(StringBuilder script, DeploymentConfiguration conf, Set<String> declaredPrefixes) {
		List<ResourceRecord> resourceRecords = conf.getResourceRegistry().getRecords();
		for (ResourceRecord record : resourceRecords) {
			ClassMetadata resourceMetadata = record.getMetadata();
			String resourcePath = resourceMetadata.getPath();
			String declaringPrefix = Utils.getFunctionName(record, null);
			declarePrefix(script, declaringPrefix, declaredPrefixes);
			List<MethodMetadata> methods = resourceMetadata.getResourceMethods();
			for (MethodMetadata methodMetadata : methods) {
				generateMethod(script, resourcePath, declaringPrefix, methodMetadata);
			}
			for (SubResourceRecord subResourceRecord : record.getSubResourceRecords()) {
				MethodMetadata method = subResourceRecord.getMetadata();
				StringBuilder path = new StringBuilder(resourcePath);
				if (!(resourcePath.endsWith("/"))) {
					path.append("/");
				}
				path.append(method.getPath());
				generateMethod(script, path.toString(), declaringPrefix, method);
			}
		}
	}
	/**
	 *  Method to implement for generating a WS Call
	 * @param script Stringbuilder to append Javascript code 
	 * @param path  URI for the Ws Call
	 * @param declaringPrefix prefix to use for the definition of the function
	 * @param method Method Medadata
	 */
	protected abstract void generateMethod(StringBuilder script, String path,  String declaringPrefix, MethodMetadata method);
	
	/**
	 * Defines the necessary empty objects to create a hierachy of functions
	 * @param script Stringbuilder to append Javascript code 
	 * @param declaringPrefix prefix to create
	 * @param declaredPrefixes List of objects already defined
	 */
	private void declarePrefix(StringBuilder script, String declaringPrefix, Set<String> declaredPrefixes) {
		if (declaredPrefixes.add(declaringPrefix)) {
			int lastDot = declaringPrefix.lastIndexOf(".");
			if (lastDot == -1)
				script.append( "var " + declaringPrefix + " = {};\n");
			else {
				declarePrefix(script, declaringPrefix.substring(0, lastDot), declaredPrefixes);
				script.append( declaringPrefix + " = {}; \n");
			}
		}
	}
	/**
	 *  Convert a liste of mediatype to a string
	 * @param mediaTypes Set of {@link MediaType}
	 * @return
	 */
	protected String getWants(Set<MediaType> mediaTypes) {
		if (mediaTypes == null)
			return null;
		return StringUtils.join(mediaTypes, ',');
	}
/**
 *  Get the first element of a set of {@link MediaType} 
 * @param set
 * @return
 */
	protected String getConsumes(Set<MediaType> set) {
		if (set == null)
			return "text/plain";
		if (set.size() > 0)
			return set.iterator().next().toString();
		return "text/plain";
	}	
}