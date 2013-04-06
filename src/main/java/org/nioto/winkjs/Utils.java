package org.nioto.winkjs;

import java.io.Closeable;

public class Utils {

	private Utils(){
	}
	
	public static final void closeQuietly(Closeable input){
		try{
			if  (input!=null) {
				input.close();
			}
		} catch (Exception e) {
			// keep it quiet
		}
	}
}
