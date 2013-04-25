package org.nioto.winkjs;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.wink.common.internal.registry.metadata.MethodMetadata;
import org.apache.wink.server.internal.registry.ResourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities Class
 * 
 * @author nioto
 */
public class Utils {

	private static final Logger log = LoggerFactory.getLogger(Utils.class);
	
	/**
	 * private constructor to avoid multiples instances
	 */
	private Utils() {
	}

	/**
	 * Close a  {@link Closeable} discarding exception .
	 * @param  input Element to close. 
	 */
	public static final void closeQuietly(Closeable input) {
		try {
			if (input != null) {
				input.close();
			}
		} catch (Exception e) {
			// keep it quiet
		}
	}
	
	/**
	 * Return the name of the method
	 * @param methodMetadata
	 * @return the name of the Java method
	 */
	public static final String getFunctionName(MethodMetadata methodMetadata) {
		if (log.isDebugEnabled() ) {
			log.debug( methodMetadata.getReflectionMethod() .toGenericString() );
		}
		return methodMetadata.getReflectionMethod().getName();
	}
	/**
	 *  Used as the name of the Javascript function to call a WebService
	 * @param record
	 * @param methodMetadata
	 * @return return the Class of the record  + '.'  +  name of the Java method
	 */
	public static final String getFunctionName(ResourceRecord record, MethodMetadata methodMetadata) {
		String name  = record.getMetadata().getResourceClass().getSimpleName();
		if( methodMetadata != null  ) {
			return name + "."  + getFunctionName(methodMetadata);
		} else {
			return name ;
		}
	}
	/**
	 *  Append the content of an {@link InputStream} into a {@link StringBuilder}
	 * @param input {@link InputStream} to read
	 * @param sb {@link StringBuilder} to append to
	 * @throws IOException In case or IO read excetion
	 */
	public static void copyFileContent(InputStream input, StringBuilder sb) throws IOException {
		try {
			Reader reader = new InputStreamReader(input);
			char[] array = new char[1024];
			int read;
			while ((read = reader.read(array)) >= 0) {
				sb.append(array, 0, read);
			}
			reader.close();
		} finally {
			Utils.closeQuietly(input);
		}
	}
	
	/**
	 * Test is a String is null or empty
	 * @param s
	 * @return
	 */
	public static final boolean isEmpty(String s ) {
		return s== null || s.length()==0;
	}
	/**
	 * Retrieve a annotation by its class from a list of annotations instances
	 * @param searchList
	 * @param annotation
	 * @return
	 */
  @SuppressWarnings("unchecked")
	public final static <T> T findAnnotation(Annotation[] searchList, Class<T> annotation) {
     if (searchList == null) return null;
     for (Annotation ann : searchList) {
        if (ann.annotationType().equals(annotation)) {
           return (T) ann;
        }
     }
     return null;
  }
  /**
   *  Choose a {@link MediaType} from a list, in this order : 
   *   - application/json if present,
   *   - the first element of the list, if the list is not null
   *   - test/plain is the list is empty 
   * @param set List of {@link MediaType}
   * @return A (not null) string representation of the {@link MediaType}
   */
  public final static String getConsumes(Set<MediaType> set) {
  		if (set != null) {
  			if (set.contains(MediaType.APPLICATION_JSON_TYPE)) {
  				return MediaType.APPLICATION_JSON;
  			} else if ( ! set.isEmpty() ) {
  				set.iterator().next().getType();
  			}
  		}
  		return MediaType.TEXT_PLAIN;
  	}
  
	/**
	 *  Convert a list of mediatype to a string of comma separated 
	 * @param mediaTypes Set of {@link MediaType}
	 * @return
	 */
	public final static String getWants(Set<MediaType> mediaTypes) {
		return StringUtils.join(mediaTypes, ',');
	}
}