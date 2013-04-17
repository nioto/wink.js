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

public class WinkJsRestServlet extends RestServlet {

	private final static Logger logger = LoggerFactory.getLogger(WinkJsRestServlet.class);

	private static final long serialVersionUID = 5265749198410674747L;

	private static final String JS_URI = "/api-client.js";

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		logger.debug("init()");
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (logger.isDebugEnabled()) {
			logger.debug(" request uri : {} , url : {}, pathinfo :{} ", new Object[] { req.getRequestURI(), req.getRequestURL(),	req.getPathInfo() });
		}
		if ( JS_URI.equals(req.getPathInfo())) {
			sendJavascriptCode(req, resp);
		} else {
			super.service(req, resp);
		}
	}

	private void sendJavascriptCode(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
		String pathInfo = req.getPathInfo();
		String uri = req.getRequestURL().toString();
		uri = uri.substring(0, 1+uri.length() - JS_URI.length());
		if( logger.isDebugEnabled()) {
			logger.debug("Serving {} : ", pathInfo);
			logger.debug("Query {} " , req.getQueryString());
		}
		PrintWriter printWriter = null;
		resp.setContentType("application/javascript");
		DeploymentConfiguration conf = getRequestProcessor().getConfiguration();
		try {
			printWriter = resp.getWriter();
			printWriter.write( jswriter.generateJavaScript(uri, conf) .toString() );
		} finally{
			Utils.closeQuietly(printWriter);
		}
	}	
}