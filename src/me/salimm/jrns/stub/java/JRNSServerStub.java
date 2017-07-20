package me.salimm.jrns.stub.java;

import org.eclipse.jetty.server.Server;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.googlecode.jsonrpc4j.ProxyUtil;

import me.salimm.jrns.common.info.ServiceInfo;
import me.salimm.jrns.common.services.JRNSProviderService;

public class JRNSServerStub {

	/**
	 * service that is supported by the instance
	 */
	private final ServiceInfo<?> service;
	/**
	 * port number that the server will be listening to
	 */
	private final int port;
	/**
	 * pointer to provider
	 */
	private final JRNSProviderService provider;

	public JRNSServerStub(ServiceInfo<?> service, int port, JRNSProviderService provider) {
		this.service = service;
		this.port = port;
		this.provider = provider;
	}

	public void start() throws Exception {
		Object compositeService = getCompositeService();
		// creating json rpc service
		JsonRpcServer jsonRpcServer = new JsonRpcServer(compositeService);
		// default port
		// server started
		Server server = new Server(port);
		server.setHandler(new SPServiceHandler(jsonRpcServer));
		server.start();
		server.join();

	}

	private Object getCompositeService() throws Exception {

		// creating the compose service
		Object compositeService = ProxyUtil.createCompositeServiceProxy(this.getClass().getClassLoader(),
				new Object[] { provider }, new Class[] { JRNSProviderService.class }, true);
		return compositeService;
	}

	public ServiceInfo<?> getService() {
		return service;
	}

	public JRNSProviderService getProvider() {
		return provider;
	}

}
