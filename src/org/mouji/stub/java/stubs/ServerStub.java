package org.mouji.stub.java.stubs;

import java.util.List;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.mouji.common.errors.ServerStubNotInitialized;
import org.mouji.common.info.SerializationFormat;
import org.mouji.common.serializer.Serializer;
import org.mouji.common.services.ServiceProvider;

public class ServerStub extends BasicStub {

	/**
	 * pointer to service provider used to generate response for requests
	 */
	private ServiceProvider provider;
	/**
	 * port for server stub
	 */
	private final int port;

	private Thread serverThread;

	private Server server;

	public ServerStub(List<Serializer> serializers, int port) {
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
		// serverThread.join();

	}

	public void stop() throws Exception {
		server.stop();
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