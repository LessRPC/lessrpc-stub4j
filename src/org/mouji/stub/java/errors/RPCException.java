package org.mouji.stub.java.errors;

import org.mouji.common.types.StatusType;

public class RPCException extends Exception{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	private int status;
	
	private String content;

	
	public RPCException(int status, String content) {
		this.status = status;
		this.content = content;
	}
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	
	public StatusType getStatusType(){
		return StatusType.fromCode(status);
	}
	
	

}
