package org.nioto;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.QueryParam;

import org.apache.wink.common.internal.registry.metadata.ClassMetadata;
import org.apache.wink.common.internal.registry.metadata.MethodMetadata;
import org.apache.wink.server.internal.DeploymentConfiguration;
import org.apache.wink.server.internal.RequestProcessor;
import org.apache.wink.server.internal.registry.ResourceRecord;
import org.apache.wink.server.internal.registry.SubResourceRecord;
import org.apache.wink.server.internal.resources.HtmlServiceDocumentResource;
import org.nioto.winkjs.Utils;

public class GenerateTestsServlet extends HttpServlet {

	private static final long serialVersionUID = 4198420930457281916L;

	@Override
	protected void service(HttpServletRequest res, HttpServletResponse resp) throws ServletException, IOException {
		RequestProcessor processor = RequestProcessor.getRequestProcessor(getServletContext(), null);
		DeploymentConfiguration conf = processor.getConfiguration();
		resp.setContentType("application/javascript");
		PrintWriter printWriter = resp.getWriter();
		try {
			List<ResourceRecord> resourceRecords = conf.getResourceRegistry().getRecords();
			for (ResourceRecord record : resourceRecords) {
				ClassMetadata classMetadata = record.getMetadata();
				String resourcePath = classMetadata.getPath();
				String declaringPrefix = Utils.getFunctionName(record, null);
				List<MethodMetadata> methods = classMetadata.getResourceMethods();
				for (MethodMetadata methodMetadata : methods) {
					if (isExposable(classMetadata, methodMetadata)) {
						generateMethod(printWriter, resourcePath, declaringPrefix, methodMetadata);
					}
				}

				for (SubResourceRecord subResourceRecord : record.getSubResourceRecords()) {
					MethodMetadata methodMetadata = subResourceRecord.getMetadata();
					if (isExposable(classMetadata, methodMetadata)) {
						StringBuilder path = new StringBuilder(resourcePath);
						if (!(resourcePath.endsWith("/"))) {
							path.append("/");
						}
						path.append(methodMetadata.getPath());
						generateMethod(printWriter, path.toString(), declaringPrefix, methodMetadata);
					}
				}
			}

		} finally {
			Utils.closeQuietly(printWriter);
		}
	}

	private void generateMethod(PrintWriter printWriter, String string, String declaringPrefix,
			MethodMetadata methodMetadata) {
		Method m = methodMetadata.getReflectionMethod();
		printWriter.append( 	m.getReturnType().getSimpleName() );
		printWriter.append( 	' ');
		printWriter.append( 	Utils.getFunctionName(methodMetadata) );
		printWriter.append( 	'(' );
		Class<?> pvec[] = m.getParameterTypes();
		Annotation anno [][] = m.getParameterAnnotations();
		for (int j = 0; j < pvec.length; j++) {
			String name = getName(pvec[j], anno[j]);
			printWriter.append( pvec[j].getSimpleName() );
			printWriter.append( ' ');
			printWriter.append( name );
		}
		printWriter.append( 	')' );
		printWriter.println("");
	}


	protected boolean isExposable(ClassMetadata classMetadata, MethodMetadata methodMetadata) {
		return (classMetadata.getResourceClass() != HtmlServiceDocumentResource.class);
	}

	private String getName(Class<?> class1, Annotation[] annotations) {
			QueryParam qa = Utils.findAnnotation( annotations, QueryParam.class);
			if ( qa !=null ) 	return  qa.value();
			HeaderParam ha = Utils.findAnnotation( annotations, HeaderParam.class);
			if ( ha !=null ) 	return  ha.value();
			CookieParam ca = Utils.findAnnotation( annotations, CookieParam.class);
			if ( ca !=null ) 	return  ca.value();
			MatrixParam ma = Utils.findAnnotation( annotations, MatrixParam.class);
			if ( ma !=null ) 	return  ma.value();
			FormParam fa = Utils.findAnnotation( annotations, FormParam.class);
			if ( fa !=null ) 	return  fa.value();
			return "$entity";
		}
}
