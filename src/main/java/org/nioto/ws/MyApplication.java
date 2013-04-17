package org.nioto.ws;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.apache.wink.common.WinkApplication;
import org.apache.wink.providers.jackson.WinkJacksonJaxbJsonProvider;

public class MyApplication extends WinkApplication {

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(LoginService.class);
		return classes;
	}

	@Override
	public Set<Object> getSingletons() {
		//return super.getSingletons();
		Set<Object> s = new HashSet<Object>();
		s.add( new WinkJacksonJaxbJsonProvider());
		return s;
	}
}
