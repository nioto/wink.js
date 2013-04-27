package org.nioto.ws;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Return implements Serializable{
	private static final long serialVersionUID = 1L;
	String content ;
	public Return(){			
	}
	public Return(String str) {
		this.content = str;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	public String getContent() {
		return this.content;
	}
	@Override
	public String toString() {
		return "Return.class  with content ="+this.content;
	}
}