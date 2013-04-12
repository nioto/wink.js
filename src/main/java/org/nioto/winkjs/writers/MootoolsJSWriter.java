/**
 * 
 */
package org.nioto.winkjs.writers;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.wink.common.internal.registry.metadata.MethodMetadata;
import org.nioto.winkjs.Template;
import org.nioto.winkjs.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tonio
 *
 */
public class MootoolsJSWriter extends AbstractJSWriter {

	private final static Logger logger = LoggerFactory.getLogger(MootoolsJSWriter.class);
	private final static String TEMPLATE_FILE  = "tmpl/mootools.tmpl.js";
	private final static String TEMPLATE_STR = getTemplateContent(MootoolsJSWriter.class, TEMPLATE_FILE).toString();
	private final static Template TEMPLATE = new Template(TEMPLATE_STR);
	/**
	 * @param type
	 */
	public MootoolsJSWriter() {
		super(FRAMEWORK.MOOTOOLS);
		System.out.println("Find template : " + TEMPLATE_STR );
	}

	/* (non-Javadoc)
	 * @see org.nioto.winkjs.writers.AbstractJSWriter#generateMethod(java.lang.StringBuilder, java.lang.String, java.lang.String, org.apache.wink.common.internal.registry.metadata.MethodMetadata)
	 */
	@Override
	protected void generateMethod(StringBuilder script, String path, String declaringPrefix, MethodMetadata methodMetaData) {		
		String uri = path;
		if (logger.isDebugEnabled()) {
			logger.debug("method: {}", methodMetaData.getReflectionMethod());
			logger.debug("uri : {} // paths = {}", uri, methodMetaData.getPaths());
		}
		Map<String, String> data = new HashMap<String, String>();
		data.put( "httpmethod", methodMetaData.getHttpMethod() );
		data.put( "uri", uri );
		data.put("functionname", declaringPrefix +"." + Utils.getFunctionName(methodMetaData) );
		data.put("accepts", getWants(methodMetaData.getProduces()));
		data.put("contentType", getConsumes(methodMetaData.getConsumes()) );
		script.append( TEMPLATE.substitute(data) );
	}

	private void printURIParams(String uri, StringBuilder script) {
		String replacedCurlyURI = PathHelper.replaceEnclosedCurlyBraces(uri);
		Matcher matcher = PathHelper.URI_PARAM_PATTERN.matcher(replacedCurlyURI);
		int i = 0;
		while (matcher.find()) {
			if (matcher.start() > i) {
				script.append(" uri += '" + replacedCurlyURI.substring(i, matcher.start()) + "';\n");
			}
			String name = matcher.group(1);
			script.append(" uri += REST.Encoding.encodePathSegment(params." + name + ");\n");
			i = matcher.end();
		}
		if (i < replacedCurlyURI.length())
			script.append(" uri += '" + replacedCurlyURI.substring(i) + "';\n");
	}
}