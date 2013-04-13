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
import org.nioto.winkjs.writers.MootoolsJSWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WinkJsRestServlet extends RestServlet {

	private final static Logger logger = LoggerFactory.getLogger(WinkJsRestServlet.class);

	private static final long serialVersionUID = 5265749198410674747L;

	private static final String JS_URI = "/api-client.js";

	private AbstractJSWriter apiWriter;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		logger.debug("init()");
		scanResources();
	}

	private void scanResources() {
		this.apiWriter = new MootoolsJSWriter();// new RestEasyJSWriter( );
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (logger.isDebugEnabled()) {
			logger.debug(" request uri : {} , url : {}, pathinfo :{} ", new Object[] { req.getRequestURI(), req.getRequestURL(),
					req.getPathInfo() });
		}
		if ( JS_URI.equals(req.getPathInfo())) {
			sendJavascriptCode(req, resp);
		} else {
			super.service(req, resp);
		}
	}

	private void sendJavascriptCode(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/javascript");
		String pathInfo = req.getPathInfo();
		String uri = req.getRequestURL().toString();
		uri = uri.substring(0, 1+uri.length() - JS_URI.length());
		if( logger.isDebugEnabled()) {
			logger.debug("Serving {} : ", pathInfo);
			logger.debug("Query {} " , req.getQueryString());
		}
		PrintWriter printWriter = null;
		DeploymentConfiguration conf = getRequestProcessor().getConfiguration();
		try {
			printWriter = resp.getWriter();
			printWriter.write( this.apiWriter.generateJavaScript(uri, conf) .toString() );
		} finally{
			Utils.closeQuietly(printWriter);
		}
	}
	
}