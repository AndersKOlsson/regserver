package com.github.anderskolsson.regserver.inputvalidation;

import java.util.regex.Pattern;

import com.github.anderskolsson.regserver.authentication.Authentication;

/**
 * Sane defaults for implementing {@link Authentication}
 */
public class DefaultInputDataValidator implements InputDataValidator {
	private final int minPassword;
	private final int minUsername;
	private final int maxUsername;
	// These are used as a regex character set
	private final String allowedCharsUsername;
	
	public DefaultInputDataValidator() {
		this.minPassword = 8;
		this.minUsername = 3;
		this.maxUsername = 127;
		this.allowedCharsUsername = "\\w-";
	}
	
	/**
	 * {@inheritDoc}
	 * Checks that the password is at least {@value minPassLength} characters long. 
	 */
	@Override
	public boolean checkPasswordStrength(String password) {
		return (null != password && password.length() >= this.minPassword);
	}

	/**
	 * {@inheritDoc}
	 * Checks that the username is following the expectations in min and max length, and characters
	 */
	@Override
	public boolean checkUsername(String userName) {
		if(null == userName || userName.length() < this.minUsername || userName.length() > this.maxUsername){
			return false;
		}
		return Pattern.matches("["+this.allowedCharsUsername+"]+", userName);
	}
}
