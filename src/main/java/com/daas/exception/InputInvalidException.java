package com.daas.exception;

/**
 * @author Vivek
 * InputInvalidException is the custom exception thrown
 */
public class InputInvalidException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param cause
	 */
	public InputInvalidException(Throwable cause) {
		super(cause);
	}

	/**
	 * 
	 * @param message
	 */
	public InputInvalidException(String message) {
		super(message);
	}



}