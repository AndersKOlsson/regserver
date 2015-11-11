package com.github.anderskolsson.regserver.authentication;

/**
 * Responsible for handling authentication to the service.
 *
 */
public interface Authentication {
	/**
	 * Get required length of a string holding the generated hash.
	 * @return the hash string length
	 */
	public int getHashLength();
	
	/**
	 * Verifies a hashed password with the cleartext.
	 * @param password cleartext password
	 * @param hash hashed password
	 * @return true if correct, else false
	 */
	public boolean verifyHash(String password, String hash);
	
	/**
	 * Hash the password according to the selected algorithm.
	 * @param password hashed password in String format, with length as {@link getHashLength}
	 * @return the hashed password
	 */
	public String hashPassword(String password);
}
