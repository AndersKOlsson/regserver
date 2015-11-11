package com.github.anderskolsson.regserver.exceptions;

/**
 * Thrown upon failure to create a user in the {@link DataStore} 
 */
public class UserCreationException extends DataStoreException {
	private static final long serialVersionUID = 3159833237864229798L;
	public UserCreationException(String message){
		super(message);
	}
	public UserCreationException(String message, Throwable cause){
		super(message, cause);
	}
}
