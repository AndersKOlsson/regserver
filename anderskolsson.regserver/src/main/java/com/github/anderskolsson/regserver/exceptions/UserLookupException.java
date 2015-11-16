package com.github.anderskolsson.regserver.exceptions;

/**
 * Thrown upon failure to look up a user in the {@link DataStore} 
 */
public class UserLookupException extends DataStoreException {
	private static final long serialVersionUID = -2078619095481498590L;
	public UserLookupException(final String message){
		super(message);
	}
	public UserLookupException(final String message, final Throwable cause){
		super(message, cause);
	}
}
