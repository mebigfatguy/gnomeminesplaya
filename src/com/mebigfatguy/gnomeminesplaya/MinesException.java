package com.mebigfatguy.gnomeminesplaya;

public class MinesException extends Exception {

	private static final long serialVersionUID = -3134493813809521762L;

	public MinesException(String message) {
		super(message);
	}

	public MinesException(String message, Throwable t) {
		super(message, t);
	}
}
