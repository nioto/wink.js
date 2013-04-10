package org.nioto.winkjs.writers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.wink.common.internal.registry.Injectable;
import org.apache.wink.common.internal.registry.metadata.MethodMetadata;
import org.nioto.winkjs.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestEasyJSWriter extends AbstractJSWriter {

	private final static Logger logger = LoggerFactory.getLogger(RestEasyJSWriter.class);

	public RestEasyJSWriter() {
		super( FRAMEWORK.RESTEASY);
	}

	private InputStream getStream(String path) throws IOException {
		URL url = getClass().getResource(path);
		logger.debug("url : {} for path {}", url, path);
		return url != null ? url.openStream() : null;
	}

	@Override
	protected void appendPreScript(String uri, StringBuilder sb) {
		super.appendPreScript(uri, sb);
		InputStream input = null;
		try {
				input = getStream("resteasy-client.js");
				if( input != null) {
					Utils.copyFileContent(input, sb);
				}
		} catch (IOException e) {
			logger.error( "unable to read resteasy-client.js ", e);
		}
		sb.append( "REST.apiURL = '" + uri + "';\n");
	}

	protected void generateMethod(StringBuilder script, String path, String fnName, MethodMetadata methodMetaData) {
		String uri = path;
		if (logger.isDebugEnabled()) {
			logger.debug("method: {}", methodMetaData.getReflectionMethod());
			logger.debug("uri : {} // paths = {}", uri, methodMetaData.getPaths());
		}
		script.append("// " + methodMetaData.getHttpMethod() + " " + uri + "\n");
		script.append( fnName +"." + Utils.getFunctionName(methodMetaData) + " = function(_params){"+ "\n");
		script.append(" var params = _params ? _params : {};\n");
		script.append(" var request = new REST.Request();\n");
		script.append(" request.setMethod('" + methodMetaData.getHttpMethod() + "');\n");
		script.append(" var uri = params.$apiURL ? params.$apiURL : REST.apiURL;\n");
		if (uri.contains("{")) {
			printURIParams(uri, script);
		} else {
			script.append(" uri += '" + uri + "';\n");
		}
		printOtherParams(methodMetaData, script);
		script.append(" request.setURI(uri);\n");
		script.append(" if(params.$username && params.$password) \n");
		script.append("  request.setCredentials(params.$username, params.$password);\n");
		script.append(" if(params.$accepts)\n");
		script.append("  request.setAccepts(params.$accepts);\n");
		String wants = getWants(methodMetaData.getProduces());
		if (wants != null) {
			script.append(" else \n");
			script.append("  request.setAccepts('" + wants + "');\n");
		}
		script.append(" if(params.$contentType)\n");
		script.append("  request.setContentType(params.$contentType);\n");
		script.append(" else\n");
		script.append("  request.setContentType('" + getConsumes(methodMetaData.getConsumes()) + "');\n");
		script.append(" if(params.$callback){\n");
		script.append("  request.execute(params.$callback);\n");
		script.append(" }else{\n");
		script.append("  var returnValue;\n");
		script.append("  request.setAsync(false);\n");
		script.append("  var callback = function(httpCode, xmlHttpRequest, value){ returnValue = value;};\n");
		script.append("  request.execute(callback);\n");
		script.append("  return returnValue;\n");
		script.append(" }\n");
		script.append("};\n");
	}

	private void printOtherParams(MethodMetadata methodMetaData, StringBuilder script) {
		List<Injectable> params = methodMetaData.getFormalParameters();
		logger.debug("{} has {} params ", methodMetaData.getReflectionMethod(), params.size());
		for (Injectable methodParamMetaData : params) {
			printParameter(methodParamMetaData, script);
		}
	}

	private void printParameter(Injectable metaData, StringBuilder script) {
		if (logger.isDebugEnabled()) {
			logger.debug("param genericType  : {} // param type : {} ", metaData.getGenericType(), metaData.getParamType());
		}
		switch (metaData.getParamType()) {
			case QUERY:
				print(metaData, script, "QueryParameter");
				break;
			case HEADER:
				print(metaData, script, "Header");
				// FIXME: warn about forbidden headers:
				// http://www.w3.org/TR/XMLHttpRequest/#the-setrequestheader-method
				break;
			case COOKIE:
				print(metaData, script, "Cookie");
				break;
			case MATRIX:
				print(metaData, script, "MatrixParameter");
				break;
			case FORM:
				print(metaData, script, "FormParameter");
				break;
			case ENTITY:
				// the entity
				script.append(" if(params.$entity)\n");
				script.append("  request.setEntity(params.$entity);\n");
				break;
		}
	}

	private void print(Injectable metaData, StringBuilder script, String type) {
		String paramName = metaData.getMember().getName();
		script.append(String.format(" if(params.%s)\n  request.add%s('%s', params.%s);\n", paramName, type, paramName,
				paramName));
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