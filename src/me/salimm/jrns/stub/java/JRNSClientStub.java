package me.salimm.jrns.stub.java;

import java.net.MalformedURLException;
import java.net.URL;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;

import me.salimm.jrns.common.info.NameServerInfo;
import me.salimm.jrns.common.info.ServiceInfo;
import me.salimm.jrns.common.info.ServiceProviderInfo;
import me.salimm.jrns.common.services.JRNSProviderService;
import me.salimm.jrns.common.services.NSService;
import me.salimm.jrns.stub.java.errors.ServiceProviderNotAvailable;

public class JRNSClientStub extends JRNSSTub implements StubConstants {

	private NameServerInfo nsInfo;

	public JRNSClientStub(NameServerInfo nsInfo) {
		this.nsInfo = nsInfo;
	}

	public <T> T call(ServiceInfo<T> service, Object[] args)
			throws MalformedURLException, ServiceProviderNotAvailable, Throwable {
		ServiceProviderInfo provider = findProvider(service);
		if (provider == null)
			throw new ServiceProviderNotAvailable(service);
		return call(service, provider, args);
	}

	private ServiceProviderInfo findProvider(ServiceInfo<?> service) throws Throwable {
		JsonRpcHttpClient client = new JsonRpcHttpClient(new URL(RPC_PROTOCOL + nsInfo.getAddress() + ":"
				+ nsInfo.getPort() + "/" + NSService.class.getSimpleName() + ".json"));
		ServiceProviderInfo provider = client.invoke("getServer", new Object[]{service.getId()}, ServiceProviderInfo.class);
		return provider;
	}

	private <T> T call(ServiceInfo<T> service, ServiceProviderInfo provider, Object[] args) throws Throwable {
		System.out.println(provider);
		JsonRpcHttpClient client = new JsonRpcHttpClient(new URL(RPC_PROTOCOL+provider.getIp() + ":" + provider.getPort() + "/"
				+ JRNSProviderService.class.getSimpleName() + ".json"));
		return client.invoke("execute", new Object[] { args }, service.getOutputType());

	}

}
