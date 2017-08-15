package org.mouji.stub.java.stubs;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.jetty.server.Server;

import org.mouji.common.info.NameServerInfo;
import org.mouji.common.info.ServiceInfo;
import org.mouji.common.serializer.Serializer;

public class NSServerStub extends BasicStub {

	public NSServerStub(List<Serializer> serializers) {
		super(serializers);
	}

	// /**
	// * port number that the server will be listening to
	// */
	// private final int port;
	// /**
	// * pointer to provider
	// */
	// private JRNSProvider<?> provider;
	//
	// public NSServerStub(int port) {
	// this.port = port;
	// }
	//
	// public void init(JRNSProvider<?> provider) {
	// this.provider = provider;
	//
	// }
	//
	// public void start() throws Exception {
	// Object compositeService = getCompositeService();
	// // creating json rpc service
	// JsonRpcServer jsonRpcServer = new JsonRpcServer(compositeService);
	// // default port
	// // server started
	// Server server = new Server(port);
	// server.setHandler(new SPServiceHandler(jsonRpcServer));
	// server.start();
	// server.join();
	//
	// }
	//
	// public boolean register(NameServerInfo ns) throws MalformedURLException,
	// Throwable {
	// JsonRpcHttpClient client = new JsonRpcHttpClient(new URL(
	// RPC_PROTOCOL + ns.getAddress() + ":" + ns.getPort() + "/" +
	// NSService.class.getSimpleName() + ".json"));
	// System.out.println(provider.getServiceInfo());
	// Boolean registered = client.invoke("registerServiceProvider",
	// new Object[] { provider.getServiceInfo(), provider.getInfo() },
	// Boolean.class);
	// return registered;
	// }
	//
	// private Object getCompositeService() throws Exception {
	//
	// // creating the compose service
	// Object compositeService =
	// ProxyUtil.createCompositeServiceProxy(this.getClass().getClassLoader(),
	// new Object[] { provider }, new Class[] { ProviderService.class }, true);
	// return compositeService;
	// }
	//
	// public ServiceInfo<?> getService() {
	// return provider.getServiceInfo();
	// }
	//
	// public JRNSProvider<?> getProvider() {
	// return provider;
	// }

}
