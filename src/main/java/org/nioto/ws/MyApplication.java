package org.nioto.ws;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;


public class MyApplication extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<Class<?>> ();
		classes.add(LoginService.class);
		return classes;
	}
}
