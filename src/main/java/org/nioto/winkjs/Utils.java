package org.nioto.winkjs;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.wink.common.internal.registry.metadata.MethodMetadata;
import org.apache.wink.server.internal.registry.ResourceRecord;

public class Utils {

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
	public static final String getFunctionName(MethodMetadata methodMetadata) {
		return methodMetadata.getReflectionMethod().getName();
	}

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
}