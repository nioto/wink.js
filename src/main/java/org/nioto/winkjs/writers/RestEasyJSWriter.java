package org.nioto.winkjs.writers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.wink.common.internal.registry.Injectable;
import org.apache.wink.common.internal.registry.metadata.ClassMetadata;
import org.apache.wink.common.internal.registry.metadata.MethodMetadata;
import org.apache.wink.server.internal.DeploymentConfiguration;
import org.apache.wink.server.internal.registry.ResourceRecord;
import org.apache.wink.server.internal.registry.SubResourceRecord;
import org.nioto.winkjs.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stéphane Épardaud <stef@epardaud.fr>
 */
public class RestEasyJSWriter {

	private final static Logger logger = LoggerFactory.getLogger(RestEasyJSWriter.class);
	private ServletContext sc;

	public RestEasyJSWriter(ServletContext sc) {
		this.sc = sc;
	}

	InputStream getStream(String path) throws IOException {
		URL url = sc.getResource(path);
		logger.debug("url : {} for path {}", url, path);
		return url.openStream();
	}

	public void writeJavaScript(String uri, PrintWriter writer, DeploymentConfiguration conf) throws IOException {
		logger.info("rest path: " + uri);
		writer.println("// start RESTEasy client API");
		// copyResource("/WEB-INF/resteasy-client.js", writer);
		writer.println("// start JAX-RS API");
		writer.println("REST.apiURL = '" + uri + "';");
		Set<String> declaredPrefixes = new HashSet<String>();
		printService(writer, conf, declaredPrefixes);
	}

	private void printService(PrintWriter writer, DeploymentConfiguration conf, Set<String> declaredPrefixes) {
		List<ResourceRecord> resourceRecords = conf.getResourceRegistry().getRecords();
		for (ResourceRecord record : resourceRecords) {
			ClassMetadata resourceMetadata = record.getMetadata();
			String resourcePath = resourceMetadata.getPath();
			String declaringPrefix; 
			if( record.hasSubResources() ) {
				declaringPrefix = resourceMetadata.getResourceClass().getSimpleName();
			} else {
				declaringPrefix = record.getClass().getSimpleName();
			}
			declarePrefix(writer, declaringPrefix, declaredPrefixes);
			List<MethodMetadata> methods = resourceMetadata.getResourceMethods();
			for (MethodMetadata methodMetadata : methods) {
				print(writer, resourcePath, methodMetadata);
			}
			for (SubResourceRecord subResourceRecord : record.getSubResourceRecords()) {
				MethodMetadata method = subResourceRecord.getMetadata();
				StringBuilder path = new StringBuilder(resourcePath);
				if (!(resourcePath.endsWith("/"))) {
					path.append("/");
				}
				path.append(method.getPath());
				print(writer, path.toString(), method);
			}
		}
	}

	private void declarePrefix(PrintWriter writer, String declaringPrefix, Set<String> declaredPrefixes) {
		if (declaredPrefixes.add(declaringPrefix)) {
			int lastDot = declaringPrefix.lastIndexOf(".");
			if (lastDot == -1)
				writer.println("var " + declaringPrefix + " = {};");
			else {
				declarePrefix(writer, declaringPrefix.substring(0, lastDot), declaredPrefixes);
				writer.println(declaringPrefix + " = {};");
			}
		}
	}

	private void copyResource(String path, PrintWriter writer) throws IOException {
		InputStream input = getStream(path);
		try {
			Reader reader = new InputStreamReader(input);
			char[] array = new char[1024];
			int read;
			while ((read = reader.read(array)) >= 0) {
				writer.write(array, 0, read);
			}
			reader.close();
		} finally {
			Utils.closeQuietly(input);
		}
	}

	private String getFunctionName(MethodMetadata methodMetadata) {
		return methodMetadata.getReflectionMethod().getName();
	}

	private String getWants(Set<MediaType> mediaTypes) {
		if (mediaTypes == null)
			return null;
		return StringUtils.join(mediaTypes, ',');
	}

	private String getConsumes(Set<MediaType> set) {
		if (set == null)
			return "text/plain";
		if (set.size() > 0)
			return set.iterator().next().toString();
		return "text/plain";
	}

	private void print(PrintWriter writer, String path, MethodMetadata methodMetaData) {
		String uri = path;
		if (logger.isDebugEnabled()) {
			logger.debug("method: {}", methodMetaData.getReflectionMethod());
			logger.debug("uri : {} // paths = {}", uri, methodMetaData.getPaths());
		}
		writer.println("// " + methodMetaData.getHttpMethod() + " " + uri);
		writer.println(getFunctionName(methodMetaData) + " = function(_params){");
		writer.println(" var params = _params ? _params : {};");
		writer.println(" var request = new REST.Request();");
		writer.println(" request.setMethod('" + methodMetaData.getHttpMethod() + "');");
		writer.println(" var uri = params.$apiURL ? params.$apiURL : REST.apiURL;");
		if (uri.contains("{")) {
			printURIParams(uri, writer);
		} else {
			writer.println(" uri += '" + uri + "';");
		}
		printOtherParams(methodMetaData, writer);
		writer.println(" request.setURI(uri);");
		writer.println(" if(params.$username && params.$password)");
		writer.println("  request.setCredentials(params.$username, params.$password);");
		writer.println(" if(params.$accepts)");
		writer.println("  request.setAccepts(params.$accepts);");
		String wants = getWants(methodMetaData.getProduces());
		if (wants != null) {
			writer.println(" else");
			writer.println("  request.setAccepts('" + wants + "');");
		}
		writer.println(" if(params.$contentType)");
		writer.println("  request.setContentType(params.$contentType);");
		writer.println(" else");
		writer.println("  request.setContentType('" + getConsumes(methodMetaData.getConsumes()) + "');");
		writer.println(" if(params.$callback){");
		writer.println("  request.execute(params.$callback);");
		writer.println(" }else{");
		writer.println("  var returnValue;");
		writer.println("  request.setAsync(false);");
		writer.println("  var callback = function(httpCode, xmlHttpRequest, value){ returnValue = value;};");
		writer.println("  request.execute(callback);");
		writer.println("  return returnValue;");
		writer.println(" }");
		writer.println("};");
	}

	private void printOtherParams(MethodMetadata methodMetaData, PrintWriter writer) {
		List<Injectable> params = methodMetaData.getFormalParameters();
		logger.debug("{} has {} params ", methodMetaData.getReflectionMethod(), params.size());
		for (Injectable methodParamMetaData : params) {
			printParameter(methodParamMetaData, writer);
		}
	}

	private void printParameter(Injectable metaData, PrintWriter writer) {
		if (logger.isDebugEnabled()) {
			logger.debug("param genericType  : {} // param type : {} ", metaData.getGenericType(), metaData.getParamType());
		}
		switch (metaData.getParamType()) {
			case QUERY:
				print(metaData, writer, "QueryParameter");
				break;
			case HEADER:
				print(metaData, writer, "Header");
				// FIXME: warn about forbidden headers:
				// http://www.w3.org/TR/XMLHttpRequest/#the-setrequestheader-method
				break;
			case COOKIE:
				print(metaData, writer, "Cookie");
				break;
			case MATRIX:
				print(metaData, writer, "MatrixParameter");
				break;
			case FORM:
				print(metaData, writer, "FormParameter");
				break;
			case ENTITY:
				// the entity
				writer.println(" if(params.$entity)");
				writer.println("  request.setEntity(params.$entity);");
				break;
		}
	}

	private void print(Injectable metaData, PrintWriter writer, String type) {
		String paramName = metaData.getMember().getName();
		writer.println(String.format(" if(params.%s)\n  request.add%s('%s', params.%s);", paramName, type, paramName,
				paramName));
	}

	private void printURIParams(String uri, PrintWriter writer) {
		String replacedCurlyURI = PathHelper.replaceEnclosedCurlyBraces(uri);
		Matcher matcher = PathHelper.URI_PARAM_PATTERN.matcher(replacedCurlyURI);
		int i = 0;
		while (matcher.find()) {
			if (matcher.start() > i) {
				writer.println(" uri += '" + replacedCurlyURI.substring(i, matcher.start()) + "';");
			}
			String name = matcher.group(1);
			writer.println(" uri += REST.Encoding.encodePathSegment(params." + name + ");");
			i = matcher.end();
		}
		if (i < replacedCurlyURI.length())
			writer.println(" uri += '" + replacedCurlyURI.substring(i) + "';");
	}
}
