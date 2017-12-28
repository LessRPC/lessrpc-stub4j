package org.lessrpc.stub.java.server;

import java.net.Socket;

import org.lessrpc.common.serializer.Serializer;

/**
 * Runnable class used to handle requests in multithread StreamServer. It will
 * receive a request and parse accordingly.
 * 
 * @author Salim
 *
 */
public class ServerWorker implements Runnable {

	/**
	 * pointer to socket to communicate with the client
	 */
	private final Socket socket;
	/**
	 * Pointer to serializer used
	 */
	private Serializer serializer;

	/**
	 * This constructs the worker by receiving the client socket
	 */
	public ServerWorker(Socket socket, Serializer serializer) {
		this.socket = socket;
		this.setSerializer(serializer);
	}

	/**
	 * Reads requests and processes the response. This will use the appropriate
	 * Serializer
	 */
	@Override
	public void run() {

	}

	public Socket getSocket() {
		return socket;
	}

	public Serializer getSerializer() {
		return serializer;
	}

	public void setSerializer(Serializer serializer) {
		this.serializer = serializer;
	}

}
