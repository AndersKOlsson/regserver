package com.github.anderskolsson.regserver;

import com.github.anderskolsson.regserver.authentication.BcryptAuthentication;

public class BcryptAuthenticationTest extends AuthenticationTest {

	@Override
	public void setAuth() {
		setAuth(new BcryptAuthentication());
	}

}
