package com.github.anderskolsson.regserver.rest.elements;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LogResponse {
	public String user;
	public String[] logins;
	
	public LogResponse() {
		super();
	}
	
	public LogResponse(final String user, final String[] logins){
		this.user = user;
		this.logins = logins;
	}
}
