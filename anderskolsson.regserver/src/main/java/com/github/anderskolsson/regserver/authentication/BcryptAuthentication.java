package com.github.anderskolsson.regserver.authentication;

import org.mindrot.jbcrypt.BCrypt;

/**
 * 
 */
public class BcryptAuthentication implements Authentication {
	private static final int HASH_LENGTH = 60;

	/**{@inheritDoc}*/
	@Override
	public int getHashLength() {
		return HASH_LENGTH;
	}

	/**{@inheritDoc}*/
	@Override
	public boolean verifyHash(String password, String hash) {
		return BCrypt.checkpw(password, hash);
	}

	/**{@inheritDoc}*/
	@Override
	public String hashPassword(String password) {
		return BCrypt.hashpw(password, BCrypt.gensalt());
	}
}
