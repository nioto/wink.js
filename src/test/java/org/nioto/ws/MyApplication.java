package org.nioto.ws;

import java.util.HashSet;
import java.util.Set;

import org.apache.wink.common.WinkApplication;
import org.apache.wink.providers.jackson.WinkJacksonJaxbJsonProvider;

public class MyApplication extends WinkApplication {

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(MyWebService.class);
		classes.add(EchoWebService.class);
		return classes;
	}

	@Override
	public Set<Object> getSingletons() {
		Set<Object> s = new HashSet<Object>();
		s.add( new WinkJacksonJaxbJsonProvider());
		return s;
	}
}
