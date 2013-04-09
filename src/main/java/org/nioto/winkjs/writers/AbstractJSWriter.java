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
 * @author tonio
 *
 */
public abstract class AbstractJSWriter {

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
	
	protected  void appendPreScript(String uri, StringBuilder sb) {
		sb.append( "// BEGIN of  ").append(this.type).append( " client generation\n");
	}
	protected void appendPostScript(String uri, StringBuilder sb) {
		sb.append( "// END of client generation");
	}
	
	public final StringBuilder generateJavaScript(String uri, DeploymentConfiguration conf) throws IOException {
		StringBuilder script = new StringBuilder();
		appendPreScript(uri, script );
		// do 
		generateScript( script, conf );
		//end
		appendPostScript(uri, script);
		return script;
	}
	
	private void generateScript(StringBuilder script, DeploymentConfiguration conf) {
		Set<String> declaredPrefixes = new HashSet<String>();
		generateService(script, conf, declaredPrefixes);
	}
	
	private void generateService(StringBuilder script, DeploymentConfiguration conf, Set<String> declaredPrefixes) {
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
	
	protected abstract void generateMethod(StringBuilder script, String path,  String declaringPrefix, MethodMetadata method);
	
	
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
	
	protected String getWants(Set<MediaType> mediaTypes) {
		if (mediaTypes == null)
			return null;
		return StringUtils.join(mediaTypes, ',');
	}

	protected String getConsumes(Set<MediaType> set) {
		if (set == null)
			return "text/plain";
		if (set.size() > 0)
			return set.iterator().next().toString();
		return "text/plain";
	}	
}