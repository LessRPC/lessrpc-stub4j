package org.lessrpc.stub.java.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.lessrpc.common.serializer.Serializer;

/**
 * StreamServer class for RPC. This class creates a mutlithread pooled server
 * that can stream data.
 * 
 * @author Salim
 *
 */
public class StreamServer implements Runnable {

	/**
	 * contains the port number
	 */
	private final int port;

	/**
	 * number of threads that can work simultaneously
	 */
	private int numThread;

	/**
	 * pointer to server socket that will listen to port of the server
	 */
	private ServerSocket serverSocket;

	/**
	 * boolean indicating if the server is stopped
	 */
	@SuppressWarnings("unused")
	private boolean isStopped = false;

	/**
	 * fixed thread pool as executor service
	 */
	private ExecutorService threadPool;

	/**
	 * serializer used by the server
	 */
	private Serializer serializer;


	public StreamServer(int port, int numThread, Serializer serializer) {
		this.port = port;
		this.serializer = serializer;
		this.setNumThread(numThread);

	}

	public int getNumThread() {
		return numThread;
	}

	public void setNumThread(int numThread) {
		this.numThread = numThread;
		// one additional for the server
		((ThreadPoolExecutor) this.threadPool).setCorePoolSize(numThread + 1);
	}

	public int getPort() {
		return port;
	}

	/**
	 * Starts the StreamServer. This binds port for the server and starts the
	 * thread pool
	 */
	public void start() {
		// starting thread pool. One additional thread for the server
		this.threadPool = Executors.newFixedThreadPool(numThread + 1);
		// opening port
		openServerSocket();
		// starting server
		threadPool.execute(this);
	}

	@Override
	public void run() {

		// while server is not stopped
		while (!isStopped()) {
			// listen to port for new client
			Socket clientSocket = null;
			try {
				// accept connection and handle on new socket
				clientSocket = this.serverSocket.accept();
			} catch (IOException e) {
				if (isStopped()) {
					System.err.println("Server Stopped.");
					break;
				}
				throw new RuntimeException("Error accepting client connection", e);
			}
			this.threadPool.execute(new ServerWorker(clientSocket,serializer));
		}

		// shutdown thread pool
		this.threadPool.shutdown();
		System.out.println("Server Stopped.");
	}

	private boolean isStopped() {
		return false;
	}

	/**
	 * binds to the server port
	 */
	private void openServerSocket() {
		try {
			this.serverSocket = new ServerSocket(this.port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * stop server
	 */
	public synchronized void stop() {
		// marking stopped
		this.isStopped = true;
		try {
			// close connection
			this.serverSocket.close();
		} catch (IOException e) {
			throw new RuntimeException("Error closing server", e);
		}
	}
}
