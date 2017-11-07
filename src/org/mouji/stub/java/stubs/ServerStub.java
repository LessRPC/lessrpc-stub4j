package org.mouji.stub.java.stubs;

import java.util.List;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.mouji.common.errors.ServerStubNotInitialized;
import org.mouji.common.info.SerializationFormat;
import org.mouji.common.serializer.Serializer;
import org.mouji.common.services.ServiceProvider;

public class ServerStub extends Stub {

	/**
	 * pointer to service provider used to generate response for requests
	 */
	protected ServiceProvider provider;
	/**
	 * port for server stub
	 */
	protected final int port;

	protected Thread serverThread;

	protected Server server;

	public ServerStub(int port, List<Serializer> serializers) {
		super(serializers);
		this.port = port;
	}

	public void init(ServiceProvider provider) {
		this.provider = provider;

	}

	public void start() throws Exception {

		if (provider == null) {
			throw new ServerStubNotInitialized();
		}

		beforeStart();

		// server started
		// QueuedThreadPool threadPool = new QueuedThreadPool();
		// threadPool.setMaxThreads(20);
		server = new Server();

		ServerConnector http = new ServerConnector(server);
		http.setPort(port);
		server.addConnector(http);

		server.setHandler(new SPServiceHandler(provider, this));

		serverThread = new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					server.start();
					server.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		serverThread.start();

		afterStart();

	}

	protected void afterStart() throws Exception{
		// do nothing in this version
	}

	protected void beforeStart() throws Exception{
		// do nothing in this version
	}

	public void stop() throws Exception {
		beforeStop();
		server.stop();
		afterStop();
	}

	protected void afterStop() throws Exception{
		// do nothing in this version
	}

	protected void beforeStop() throws Exception {
		// Do nothing for this version
	}

	public ServiceProvider getProvider() {
		return provider;
	}

	public boolean accepts(SerializationFormat format) {
		return serializerMap.containsKey(format);
	}

	public SerializationFormat findFirstAcceptedFormat(SerializationFormat[] formats) {
		// formats is either not provided or empty or null
		if (formats == null || formats.length == 0)
			return null;

		for (SerializationFormat format : formats) {
			if (accepts(format)) {
				return format;
			}
		}

		return null;
	}

}