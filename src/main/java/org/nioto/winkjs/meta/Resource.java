/**
 * 
 */
package org.nioto.winkjs.meta;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

/**
 * @author nioto
 *
 */
public abstract class Resource {

	
	abstract Method getMethod();
	
	abstract boolean isRoot();
	
	abstract String getFunctionPrefix();
	
	abstract Collection<String> getHttpMethods();
	
	abstract String getUri() ;

	abstract void collectResourceMethodsUntilRoot(List<Method> methods) ; /*{
			if(isRoot())
				return;
			methods.add(locator.getMethod());
			parent.collectResourceMethodsUntilRoot(methods);
		}
	}*/
}
