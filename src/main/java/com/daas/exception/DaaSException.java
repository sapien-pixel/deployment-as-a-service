package com.daas.exception;

/**
 * @author Vivek
 * DaaSException is the custom exception thrown across DaaS
 */
public class DaaSException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param cause
	 */
	public DaaSException(Throwable cause) {
		super(cause);
	}

	/**
	 * 
	 * @param message
	 */
	public DaaSException(String message) {
		super(message);
	}


}
