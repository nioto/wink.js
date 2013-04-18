package org.nioto.winkjs;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wink.server.internal.DeploymentConfiguration;
import org.apache.wink.server.internal.RequestProcessor;
import org.apache.wink.server.internal.servlet.RestServlet;
import org.nioto.winkjs.writers.AbstractJSWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Servlet not extended {@link RestServlet}.
 *  This servlet must be loaded <b>after</b> the wink {@link RestServlet}, to allow access 
 *  to the {@link RequestProcessor} configuration.
 *  
 *   The init parameters supported are : 
 *   - "requestProcessorAttribute" must be the same value as for {@link RestServlet} init param 
 *   - "winkpath" contains the path defined for the Wink {@link RestServlet} path ( mandatory, for correct url for Rest Services)
 *   
 * @author tonio
 *
 */
public class WinkJsClientServlet extends HttpServlet  {

	private static final long serialVersionUID = -7175303343130983588L;

	private final static Logger logger = LoggerFactory.getLogger(WinkJsClientServlet.class);
	

	// from AbstractRestServket
  private static final String REQUEST_PROCESSOR_ATTRIBUTE = "requestProcessorAttribute";
  private String requestProcessorAttribute;
  // 
  private static final String WINK_PATH_ATTRIBUTE = "winkpath";
  private String winkPath;
  
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		logger.debug("init()");

		String path = getInitParameter( WINK_PATH_ATTRIBUTE );
		if ( Utils.isEmpty( path )) {
			throw new ServletException(" Wink path is not set  as an  init paremeter with key : " + WINK_PATH_ATTRIBUTE);
		}
		this.winkPath = getServletContext().getContextPath() + path;
		if( ! this.winkPath.endsWith("/") ) {
			this.winkPath += "/";
		}
		
    this.requestProcessorAttribute = getServletContext().getInitParameter( REQUEST_PROCESSOR_ATTRIBUTE);
		logger.debug("requestProcessorAttribute : {} ", requestProcessorAttribute );
		// Check if RequestProcessor is available 
		RequestProcessor processor = RequestProcessor.getRequestProcessor( getServletContext(), this.requestProcessorAttribute);
		if( processor == null ) {
			throw new ServletException(" Wink RequestProcessor not found !! Is Wink configured to load before WinkJsClientServlet in web.xml and/ or " + REQUEST_PROCESSOR_ATTRIBUTE + " init paremeter set properly");
		}
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (logger.isDebugEnabled()) {
			logger.debug(" request uri : {} , url : {}, pathinfo :{} ", new Object[] { req.getRequestURI(), req.getRequestURL(),	req.getPathInfo() });
		}
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
		RequestProcessor processor = RequestProcessor.getRequestProcessor( getServletContext(), this.requestProcessorAttribute );
		DeploymentConfiguration conf = processor.getConfiguration();
		PrintWriter printWriter = null;
		resp.setContentType("application/javascript");
		try {
			printWriter = resp.getWriter();
			printWriter.write( jswriter.generateJavaScript( this.winkPath, conf) .toString() );
		} finally{
			Utils.closeQuietly(printWriter);
		}
	}	
}