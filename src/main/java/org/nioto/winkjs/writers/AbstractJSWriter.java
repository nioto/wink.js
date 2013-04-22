/**
 * 
 */
package org.nioto.winkjs.writers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wink.common.internal.registry.metadata.ClassMetadata;
import org.apache.wink.common.internal.registry.metadata.MethodMetadata;
import org.apache.wink.server.internal.DeploymentConfiguration;
import org.apache.wink.server.internal.registry.ResourceRecord;
import org.apache.wink.server.internal.registry.SubResourceRecord;
import org.apache.wink.server.internal.resources.HtmlServiceDocumentResource;
import org.nioto.winkjs.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Abstract Class for JS Client code generation
 * 
 * @author nioto
 */
public abstract class AbstractJSWriter {

	
	private final static Logger log = LoggerFactory.getLogger(AbstractJSWriter.class);
	
	/**
	 * Available Javascript Frameworks supported
	 * ( mootools, RestEasyJS client, JQuery )
	 * @author tonio
	 */
	public static enum FRAMEWORK {
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
		generateServices(script, conf );
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
	private void generateServices(StringBuilder script, DeploymentConfiguration conf) {
		List<ResourceRecord> resourceRecords = conf.getResourceRegistry().getRecords();
		Set<String> declaredPrefixes = new HashSet<String>();
		for (ResourceRecord record : resourceRecords) {
			ClassMetadata classMetadata = record.getMetadata();
			String resourcePath = classMetadata.getPath();
			String declaringPrefix = Utils.getFunctionName(record, null);
			boolean declaredPrefix = false;
			List<MethodMetadata> methods = classMetadata.getResourceMethods();
			for (MethodMetadata methodMetadata : methods) {
				if( isExposable(classMetadata, methodMetadata )) {
					if( ! declaredPrefix ) {
						declarePrefix(script, declaringPrefix, declaredPrefixes);
						declaredPrefix=true;
					}
					generateMethod(script, resourcePath, declaringPrefix, methodMetadata);
				}
			}
			
			for (SubResourceRecord subResourceRecord : record.getSubResourceRecords()) {
				MethodMetadata methodMetadata = subResourceRecord.getMetadata();
				if( isExposable(classMetadata, methodMetadata )) {
					if( ! declaredPrefix ) {
						declarePrefix(script, declaringPrefix, declaredPrefixes);
						declaredPrefix=true;
					}
					StringBuilder path = new StringBuilder(resourcePath);
					if (!(resourcePath.endsWith("/"))) {
						path.append("/");
					}
					path.append(methodMetadata.getPath());
					generateMethod(script, path.toString(), declaringPrefix, methodMetadata);
				}
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
	protected abstract void generateMethod(StringBuilder script, String path,  String declaringPrefix, MethodMetadata methodMetadata);
	
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
	 * Retrieve the content of a file template as a StringBuilder.
	 *  The file is loaded using {@link Class#getResource(String)} 
	 * @param clazz  A {@link Class} 
	 * @param relativePathToClass the path of a file relative to the param clazz
	 * @return The content of the the file as a {@link StringBuilder}
	 */
	protected final static StringBuilder getTemplateContent(Class<?> clazz, String relativePathToClass) {
			URL url = clazz.getResource(relativePathToClass);
			StringBuilder content = new StringBuilder();
			InputStream input = null;
			try {
				input = url.openStream();
				Utils.copyFileContent(input, content);
			} catch (IOException e) {
				log.error("unable to getTemplateContent for "+ relativePathToClass +" on " + clazz, e);
				content.setLength(0);
				content.append( "Error opening file " + relativePathToClass );
			} finally {
				Utils.closeQuietly(input);
			}
			return content;
		}
	
	/**
	 * Retrieve a AbstractJSWriter by his name
	 * @param name name of jswriter (must be in {@link FRAMEWORK} values ) 
	 * @return  null if name not found 
	 */
	public final static AbstractJSWriter getWriter(String name) {
		if( FRAMEWORK.RESTEASY.name().equals(name) ) {
			return new RestEasyJSWriter();
		} else {
			return null;
		}
	}
	/**
	 * Retrieve the default {@link AbstractJSWriter}
	 * @return
	 */
	public final static AbstractJSWriter getDefaultWriter() {
		return getWriter( FRAMEWORK.RESTEASY.name() );
	}
	/**
	 *  Check if a method can be exposed on the Javascript client API.
	 *  Default implementation : remove default service {@link HtmlServiceDocumentResource}
	 * @param classMetadata ClassMetadata of the exposed service
	 * @param methodMetadata MethodMetadata of the exposed service
	 * @return
	 */
	protected boolean isExposable( ClassMetadata classMetadata, MethodMetadata methodMetadata) {
		return  ( classMetadata.getResourceClass() != HtmlServiceDocumentResource.class );
	}
}