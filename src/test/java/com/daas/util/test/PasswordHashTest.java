package com.daas.util.test;

import org.junit.Test;

import com.daas.util.PasswordHash;

public class PasswordHashTest {

	
	@Test
	public void generateHash() {
		
		System.out.println(PasswordHash.hashPassword("test"));
		
	}	
}
