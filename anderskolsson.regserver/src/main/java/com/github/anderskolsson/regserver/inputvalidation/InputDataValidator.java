package com.github.anderskolsson.regserver.inputvalidation;

/**
 * Verifies data supplied by users of the service
 */
public interface InputDataValidator {
	/**
	 * Verifies that the password is of good enough quality to be used.
	 * @param password the selected password
	 * @return true if the password is good enough, else false
	 */
	public boolean checkPasswordStrength(String password);
	
	/**
	 * Verifies that the username is according to expectations.
	 * @param userName the username to be verified
	 * @return true if the username is according to expectations, else false
	 */
	public boolean checkUsername(String userName);
}
