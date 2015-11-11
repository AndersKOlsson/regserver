package com.github.anderskolsson.regserver.rest.elements;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LogRequest {
	public JsonUser credentials;
	public int numLogs;
	
	public LogRequest() {
		super();
	}
	
	public LogRequest(final JsonUser credentials, final int numLogs){
		this.credentials = credentials;
		this.numLogs = numLogs;
	}
}
