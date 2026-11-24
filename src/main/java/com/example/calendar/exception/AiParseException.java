package com.example.calendar.exception;

public class AiParseException extends RuntimeException {

	private final boolean upstream;

	private AiParseException(String message, boolean upstream) {
		super(message);
		this.upstream = upstream;
	}

	public static AiParseException invalid(String message) {
		return new AiParseException(message, false);
	}

	public static AiParseException upstream(String message) {
		return new AiParseException(message, true);
	}

	public boolean isUpstream() {
		return upstream;
	}

}
