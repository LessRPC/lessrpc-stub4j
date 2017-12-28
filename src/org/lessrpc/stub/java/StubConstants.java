package org.lessrpc.stub.java;

public interface StubConstants {

	public static final String CONF_PARAM_NAME_SERVER_URL = "CONF.NAMESERVER.URL";
	public static final String CONF_PARAM_NAME_SERVER_PORT = "CONF.NAMESERVER.PORT";

	public static final String RPC_PROTOCOL = "http://";

	public static final String LESS_RPC_REQUEST_PING = "/ping";
	public static final String LESS_RPC_REQUEST_EXECUTE = "/execute";
	public static final String LESS_RPC_REQUEST_SERVICE = "/service";
	public static final String LESS_RPC_REQUEST_INFO = "/info";

	public static final String HTTP_PROTOCOL = "http://";
	public static final String HTTPS_PROTOCOL = "http://";

	public static final long HTTP_WAIT_TIME_SHORT = 5;

	public static final long HTTP_WAIT_TIME_LONG = 60 * 60 * 5;
}
