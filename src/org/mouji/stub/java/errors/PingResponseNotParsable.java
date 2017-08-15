package org.mouji.stub.java.errors;

public class PingResponseNotParsable extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public PingResponseNotParsable(String responseText) {
		super("Response \""+responseText+"\" is not parsable as boolean. Ping response should be 0 or 1");
	}

}
