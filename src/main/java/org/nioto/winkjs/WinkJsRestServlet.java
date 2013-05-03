package org.nioto.winkjs;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wink.server.internal.DeploymentConfiguration;
import org.apache.wink.server.internal.servlet.RestServlet;
import org.nioto.winkjs.writers.AbstractJSWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet extending the WInk {@link RestServlet}.
 * 
 * By default, the Javascript Code can be obtain by the url   /<ContextPath>/api-client.js
 * is no jsapiurl Init param is set.
 * 
 * @author nioto
 *
 */
public class WinkJsRestServlet extends RestServlet {

	private final static Logger logger = LoggerFactory.getLogger(WinkJsRestServlet.class);

	private static final long serialVersionUID = 5265749198410674747L;

	/**
	 * Define the default relative ( to context) url of the client JS API
	 */
	public static final String DEFAULT_JS_URL = "/api-client.js";
	/**
	 * Name of the init parameter to change JS API Client url
	 */
	public static final String URL_INITPARAM = "jsapiurl";
	/**
	 * Define the relative ( to context) url of the client JS API
	 */
	private String jsUri;

	/**
	 * (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 * 
	 * Initialize  the Url for the JS Api Client, if is set using Init param or use the default one
	 */
	@Override
	public void init(final ServletConfig config) throws ServletException {
		super.init(config);
		logger.debug("init()");
		final String tmp = getInitParameter( URL_INITPARAM );
		
		if (Utils.isEmpty(tmp)) {
			this.jsUri = DEFAULT_JS_URL;
		} else {
			if ( tmp.charAt(0) == '/') {
				this.jsUri = tmp;
			} else {
				this.jsUri = "/" +tmp;
			}
		}
	}

	/**
	 * (non-Javadoc)
	 * @see org.apache.wink.server.internal.servlet.RestServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 * 
	 * Check if the request path is the JS API URL , if not call let {@link RestServlet} handle the request, otherwise then send the javascript
	 */
	@Override
	protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		if (logger.isDebugEnabled()) {
			logger.debug(" request uri : {} , url : {}, pathinfo :{} ", new Object[] { req.getRequestURI(), req.getRequestURL(),	req.getPathInfo() });
		}
		if ( this.jsUri.equals(req.getPathInfo())) {
			sendJavascriptCode(req, resp);
		} else {
			super.service(req, resp);
		}
	}
	/**
	 * Write the javascript code to the response
	 */
	private void sendJavascriptCode(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		String framework = req.getParameter("framework");
		AbstractJSWriter jswriter ;
		if ( Utils.isEmpty(framework)) {
			jswriter = AbstractJSWriter.getDefaultWriter();
		} else {
			jswriter = AbstractJSWriter.getWriter( framework );
		}
		if( jswriter == null ) {
			throw new ServletException(" framework : " + framework + " not supported"); 
		}
		String uri = req.getRequestURL().toString();
		uri = uri.substring(0, 1+uri.length() - DEFAULT_JS_URL.length());
		if( logger.isDebugEnabled()) {
			logger.debug("Serving {} : ", req.getPathInfo());
			logger.debug("Query {} " , req.getQueryString());
		}
		PrintWriter printWriter = null;
		resp.setContentType("application/javascript");
		final DeploymentConfiguration conf = getRequestProcessor().getConfiguration();
		try {
			printWriter = resp.getWriter();
			printWriter.write( jswriter.generateJavaScript(uri, conf) .toString() );
		} finally{
			Utils.closeQuietly(printWriter);
		}
	}	
}