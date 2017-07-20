package me.salimm.jrns.stub.java;

import java.net.MalformedURLException;
import java.net.URL;

import com.allConfig.conf.AbstractConfig;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;

import me.salimm.jrns.common.info.ServiceInfo;
import me.salimm.jrns.common.info.ServiceProviderInfo;
import me.salimm.jrns.common.services.JRNSProviderService;
import me.salimm.jrns.common.services.NSService;
import me.salimm.jrns.stub.java.errors.ServiceProviderNotAvailable;

public class JRNSClientStub implements StubConstants {

	private AbstractConfig conf;

	public JRNSClientStub(AbstractConfig conf) {
		this.conf = conf;
	}

	public <T> T call(ServiceInfo<T> service, Object[] args)
			throws MalformedURLException, ServiceProviderNotAvailable, Throwable {
		ServiceProviderInfo provider = findProvider(service);
		if (provider == null)
			throw new ServiceProviderNotAvailable(service);
		return call(service, provider, args);
	}

	private ServiceProviderInfo findProvider(ServiceInfo<?> service) throws Throwable {
		JsonRpcHttpClient client = new JsonRpcHttpClient(new URL(conf.getValue(CONF_PARAM_NAME_SERVER_URL) + ":"
				+ conf.getValue(CONF_PARAM_NAME_SERVER_PORT) + "/" + NSService.class.getSimpleName() + ".json"));
		ServiceProviderInfo provider = client.invoke("getServer", service, ServiceProviderInfo.class);
		return provider;
	}

	private <T> T call(ServiceInfo<T> service, ServiceProviderInfo provider, Object[] args) throws Throwable {
		JsonRpcHttpClient client = new JsonRpcHttpClient(new URL(provider.getIp() + ":" + provider.getPort() + "/"
				+ JRNSProviderService.class.getSimpleName() + ".json"));
		return client.invoke("execute", new Object[] { service, args }, service.getOutputType());

	}

}
