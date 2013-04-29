package org.nioto;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.apache.wink.common.internal.registry.Injectable;
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

	
	HtmlHelper html = new HtmlHelper();
	String headPage;
	String footPage;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		StringBuilder sb ;
		try {
			sb = Utils.getContent( getClass(), "headPage.tmpl");
			this.headPage = sb.toString();
			sb = Utils.getContent( getClass(), "footPage.tmpl");
			this.footPage = sb.toString();			
		} catch (IOException e) {
			throw new ServletException(e);
		}
	}
	@Override
	protected void service(HttpServletRequest res, HttpServletResponse resp) throws ServletException, IOException {
		RequestProcessor processor = RequestProcessor.getRequestProcessor(getServletContext(), null);
		DeploymentConfiguration conf = processor.getConfiguration();
		resp.setContentType("text/html");
		Writer writer =  resp.getWriter();
		// headPage of html page
		writer.append( this.headPage );
		try {
			List<ResourceRecord> resourceRecords = conf.getResourceRegistry().getRecords();
			for (ResourceRecord record : resourceRecords) {
				ClassMetadata classMetadata = record.getMetadata();
				String resourcePath = classMetadata.getPath();
				String declaringPrefix = Utils.getFunctionName(record, null);
				List<MethodMetadata> methods = classMetadata.getResourceMethods();
				for (MethodMetadata methodMetadata : methods) {
					if (isExposable(classMetadata, methodMetadata)) {
						generateMethod(writer, resourcePath, declaringPrefix, methodMetadata);
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
						generateMethod(writer, path.toString(), declaringPrefix, methodMetadata);
					}
				}
			}
			writer.append( this.footPage );
		} finally {
			Utils.closeQuietly(writer);
		}
	}
	
	private void generateMethod(Writer printWriter, String path, String declaringPrefix,
			MethodMetadata methodMetadata) throws IOException {
		
		String functionName = "Winkjs."+declaringPrefix + "." + Utils.getFunctionName(methodMetadata)  ;
		html.incCounter();		
		Method m = methodMetadata.getReflectionMethod();
/*		printWriter.append( 	"//" );
		printWriter.append( 	m.getReturnType().getSimpleName() );
		printWriter.append( 	' ');
		printWriter.append( functionName );
		printWriter.append( 	'(' );
		*/
		List<Injectable> list = methodMetadata.getFormalParameters();
		List<FieldInfo> fields = new ArrayList<FieldInfo>();
 		for (Injectable injectable : list) {
			FieldInfo info = getFieldInfo(injectable);
			if( info != null ) {
				fields.add(info);
			}
		}
		//printWriter.append( 	")\n" );
		printWriter.append(html.getScript( functionName, fields, m));
		printWriter.append( 	"\n" );
		printWriter.append( 	html.getHtml( functionName ));
	}


	protected boolean isExposable(ClassMetadata classMetadata, MethodMetadata methodMetadata) {
		return (classMetadata.getResourceClass() != HtmlServiceDocumentResource.class);
	}

	private FieldInfo getFieldInfo(Injectable metaData) {
		String paramName = null;
		Class<? extends Annotation> ann = null;
		switch (metaData.getParamType()) {
			case QUERY:
				QueryParam qa = Utils.findAnnotation( metaData.getAnnotations(), QueryParam.class);
				paramName  = ( qa ==null ? 	paramName = metaData.getMember().getName() : qa.value() );
				ann  = QueryParam.class;
				break;
			case HEADER:
				HeaderParam ha = Utils.findAnnotation( metaData.getAnnotations(), HeaderParam.class);
				paramName  = ( ha ==null ? 	paramName = metaData.getMember().getName() : ha.value() );
				ann = HeaderParam.class;
				break;
			case COOKIE:
				CookieParam ca = Utils.findAnnotation( metaData.getAnnotations(), CookieParam.class);
				paramName  = ( ca ==null ? 	paramName = metaData.getMember().getName() : ca.value() );
				ann = CookieParam.class;
				break;
			case MATRIX:
				MatrixParam ma = Utils.findAnnotation( metaData.getAnnotations(), MatrixParam.class);
				paramName  = ( ma ==null ? 	paramName = metaData.getMember().getName() : ma.value() );
				ann = MatrixParam.class;
				break;
			case FORM:
				FormParam fa = Utils.findAnnotation( metaData.getAnnotations(), FormParam.class);
				paramName  = ( fa ==null ? 	paramName = metaData.getMember().getName() : fa.value() );
				ann = FormParam.class;
				break;
			case PATH:
				PathParam pa = Utils.findAnnotation( metaData.getAnnotations(), PathParam.class);
				paramName  = ( pa ==null ? 	paramName = metaData.getMember().getName() : pa.value() );
				ann = PathParam.class;
				break;
			case ENTITY:
				paramName="entity";
				// the entity
				break;
		}
		if( Utils.isEmpty( paramName )) {
			return null;
		} else {
			return new FieldInfo(metaData.getType(), paramName, ann );
		}
	}
	
	protected static class FieldInfo {
		Class<?> clazz ; 
		String name;
		Class<? extends Annotation> ann;
		public FieldInfo(Class<?> clazz, String name, Class<? extends Annotation> ann ) {
			this.clazz = clazz;
			this.name = name;
			this.ann = ann;
		}
		public Class<?> getClazz() {
			return this.clazz;
		}
		public String getName() {
			return this.name;
		}
		public Class<? extends Annotation> getAnn() {
			return this.ann;
		}
		@Override
		public String toString() {
			return  ( this.ann !=null ? this.ann.getSimpleName() : "") + " " + this.clazz.getSimpleName() + " " +  this.name;
		}
	}
}
