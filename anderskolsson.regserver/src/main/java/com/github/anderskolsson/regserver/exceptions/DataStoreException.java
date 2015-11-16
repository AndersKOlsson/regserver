package com.github.anderskolsson.regserver.exceptions;

public class DataStoreException extends Exception {
	private static final long serialVersionUID = 6979815778075265125L;
	
	public DataStoreException(final String message){
		super(message);
	}
	
	public DataStoreException(final String message, final Throwable cause){
		super(message, cause);
	}
}
