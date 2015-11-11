package com.github.anderskolsson.regserver;

import org.junit.Test;

import com.github.anderskolsson.regserver.authentication.Authentication;

import static org.junit.Assert.*;

import org.junit.Before;

public abstract class AuthenticationTest {
	private Authentication auth;
	
	void setAuth(Authentication auth){
		this.auth = auth;
	}
	
	@Before
	abstract public void setAuth();
	
	@Test
	public void testHashLength() {
		assertEquals(auth.getHashLength(), auth.hashPassword("test").length());
	}
	
	@Test
	public void testValidation() {
		String testPass = "test";
		String hash = auth.hashPassword(testPass);
		assertTrue(auth.verifyHash(testPass, hash));
		
		String hash2 = auth.hashPassword(testPass+2);
		assertFalse(auth.verifyHash(testPass, hash2));
	}
	
}
