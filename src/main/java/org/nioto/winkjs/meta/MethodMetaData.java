package org.nioto.winkjs.meta;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.nioto.winkjs.Utils;
import org.nioto.winkjs.meta.MethodParamMetaData.MethodParamType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodMetaData {

	private final static Logger logger = LoggerFactory.getLogger(MethodMetaData.class);

	private Method method;
	private Class<?> klass;
	private String wants;
	private String consumesMIMEType;
	private String uri;
	private String functionName;
	private List<MethodParamMetaData> parameters = new ArrayList<MethodParamMetaData>();
	private Collection<String> httpMethods;
	private String functionPrefix;
	private boolean wantsForm;

	public MethodMetaData(Resource resource)	{
		this.method = resource.getMethod();
		this.klass = method.getDeclaringClass();
		Path methodPath = method.getAnnotation(Path.class);
		Path klassPath = klass.getAnnotation(Path.class);
		Produces produces = method.getAnnotation(Produces.class);
		if (produces == null)
			produces = klass.getAnnotation(Produces.class);
		this.wants = getWants(produces);
		Consumes consumes = method.getAnnotation(Consumes.class);
		if (consumes == null)
			consumes = klass.getAnnotation(Consumes.class);
		this.uri = appendURIFragments(resource, klassPath, methodPath);
		if(resource.isRoot())
			this.functionPrefix = klass.getSimpleName();
		else
			this.functionPrefix = resource.getFunctionPrefix();
		this.functionName = this.functionPrefix + "." + method.getName(); 
		httpMethods = resource.getHttpMethods();

		// we need to add all parameters from parent resource locators until the root
		List<Method> methodsUntilRoot = new ArrayList<Method>();
		methodsUntilRoot.add(method);
		resource.collectResourceMethodsUntilRoot(methodsUntilRoot);
		for (Method method : methodsUntilRoot) {
			Annotation[][] allAnnotations = method.getParameterAnnotations();
			Class<?>[] parameterTypes = method.getParameterTypes();
			for (int i = 0; i < parameterTypes.length; i++)	{
				processMetaData(parameterTypes[i], allAnnotations[i], true);
			}
		}
		// this must be after we scan the params in case of @Form
		this.consumesMIMEType = getConsumes(consumes);
		if(wantsForm && !"application/x-www-form-urlencoded".equals(consumesMIMEType)){
         logger.warn("Overriding @Consumes annotation in favour of application/x-www-form-urlencoded due to the presence of @FormParam");
			this.consumesMIMEType = "application/x-www-form-urlencoded";
		}
	}

	protected void processMetaData(Class<?> type, Annotation[] annotations, boolean useBody)	{
		QueryParam query;
		HeaderParam header;
		MatrixParam matrix;
		PathParam uriParam;
		CookieParam cookie;
		FormParam formParam;

		if ((query = Utils.findAnnotation(annotations, QueryParam.class)) != null) {
			addParameter(type, annotations, MethodParamType.QUERY_PARAMETER, query.value());
		} else if ((header = Utils.findAnnotation(annotations,HeaderParam.class)) != null)	{
			addParameter(type, annotations, MethodParamType.HEADER_PARAMETER, header.value());
		} else if ((cookie = Utils.findAnnotation(annotations,CookieParam.class)) != null)	{
			addParameter(type, annotations, MethodParamType.COOKIE_PARAMETER, cookie.value());
		} else if ((uriParam = Utils.findAnnotation(annotations, PathParam.class)) != null) {
			addParameter(type, annotations, MethodParamType.PATH_PARAMETER, uriParam.value());
		} else if ((matrix = Utils.findAnnotation(annotations, MatrixParam.class)) != null) 	{
			addParameter(type, annotations, MethodParamType.MATRIX_PARAMETER, matrix.value());
		} else if ((formParam = Utils.findAnnotation(annotations, FormParam.class)) != null) {
			addParameter(type, annotations, MethodParamType.FORM_PARAMETER, 	formParam.value());
			this.wantsForm = true;
		} else if ((Utils.findAnnotation(annotations, Context.class)) != null) {
			// righfully ignore
		} else if (useBody) {
			addParameter(type, annotations, MethodParamType.ENTITY_PARAMETER, null);
		}
	}

	private void addParameter(Class<?> type, Annotation[] annotations, MethodParamType paramType, String value) {
		this.parameters.add(new MethodParamMetaData(type, annotations, paramType, 	value));
	}

	private String getWants(Produces produces) {
		if (produces == null) {
			return null;
		}
		String[] value = produces.value();
		if (value.length == 0) {
			return null;
		}
		if (value.length == 1) {
			return value[0];
		}
		StringBuffer buf = new StringBuffer();
		for (String mime : produces.value()) {
			if (buf.length() != 0) {
				buf.append(",");
			}
			buf.append(mime);
		}
		return buf.toString();
	}

	private String getConsumes(Consumes consumes) {
		if (consumes == null) {
			return "text/plain";
		}
		if (consumes.value().length > 0) {
			return consumes.value()[0];
		}
		return "text/plain";
	}

	public static String appendURIFragments(String... fragments) {
		StringBuilder str = new StringBuilder();
		for (String fragment : fragments) {
			if (fragment == null || fragment.length() == 0 || fragment.equals("/"))
				continue;
			if (fragment.startsWith("/")) {
				fragment = fragment.substring(1);
			}
			if (fragment.endsWith("/")){
				fragment = fragment.substring(0, fragment.length() - 1);
			}
			str.append('/').append(fragment);
		}
		if (str.length() == 0) {
			return "/";
		}
		return str.toString();
	}

	public Method getMethod() {
		return method;
	}

	public Class<?> getKlass() {
		return klass;
	}

	public String getWants() 	{
		return wants;
	}

	public String getConsumesMIMEType() {
		return consumesMIMEType;
	}

	public String getUri() {
		return uri;
	}

	public String getFunctionName()	{
		return functionName;
	}

	public List<MethodParamMetaData> getParameters() {
		return parameters;
	}

	public Collection<String> getHttpMethods()	{
		return httpMethods;
	}

	public static String appendURIFragments(Resource resource, Path classPath, Path methodPath) {
		return appendURIFragments(resource == null ? null : resource.getUri(), 
				classPath != null ? classPath.value() : null,
				methodPath != null ? methodPath.value() : null);
	}

	public String getFunctionPrefix() {
		return functionPrefix;
	}
}