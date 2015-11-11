package com.github.anderskolsson.regserver;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.github.anderskolsson.regserver.inputvalidation.DefaultInputDataValidator;
import com.github.anderskolsson.regserver.inputvalidation.InputDataValidator;

public class DefaultInputValidationTest {
	private InputDataValidator validator;
	
	public void setValidator(InputDataValidator validator) {
		this.validator = validator;
	}
	
	@Before
	public void setValidator() {
		this.validator = new DefaultInputDataValidator();
	}
	
	@Test
	public void testPassword() {
		assertTrue(validator.checkPasswordStrength("12345678"));
		assertFalse(validator.checkPasswordStrength("1234567"));
	}
	
	@Test
	public void testUsername() {
		assertTrue(validator.checkUsername("ab2"));
		assertFalse(validator.checkUsername("ab"));
		assertFalse(validator.checkUsername("abc!"));
	}
}
