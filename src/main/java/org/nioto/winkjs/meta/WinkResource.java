package org.nioto.winkjs.meta;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.apache.wink.server.internal.registry.ResourceRecord;

public class WinkResource extends Resource {

	ResourceRecord record;
	public WinkResource(ResourceRecord record) {
		this.record = record;
	}

	@Override
	Method getMethod() {
		return this.record.get
	}

	@Override
	boolean isRoot() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	String getFunctionPrefix() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	Collection<String> getHttpMethods() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	String getUri() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	void collectResourceMethodsUntilRoot(List<Method> methods) {
		// TODO Auto-generated method stub
		
	}

}
