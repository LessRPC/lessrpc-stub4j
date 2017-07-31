package me.salimm.jrns.stub.java;

import java.net.Inet4Address;
import java.net.MalformedURLException;
import java.net.URL;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;

import me.salimm.jrns.common.ExecutionResponse;
import me.salimm.jrns.common.info.ClientInfo;
import me.salimm.jrns.common.info.NameServerInfo;
import me.salimm.jrns.common.info.ServiceInfo;
import me.salimm.jrns.common.info.ServiceProviderInfo;
import me.salimm.jrns.common.services.JRNSProviderService;
import me.salimm.jrns.common.services.NSService;
import me.salimm.jrns.common.types.StubEnvType;
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
		ServiceProviderInfo provider = client.invoke("getServerById", new Object[] { service.getId() },
				ServiceProviderInfo.class);
		return provider;
	}

	@SuppressWarnings("unchecked")
	private <T> T call(ServiceInfo<T> service, ServiceProviderInfo provider, Object[] args) throws Throwable {
		JsonRpcHttpClient client = new JsonRpcHttpClient(new URL(RPC_PROTOCOL + provider.getIp() + ":"
				+ provider.getPort() + "/" + JRNSProviderService.class.getSimpleName() + ".json"));

		ExecutionResponse response = client.invoke("execute",
				new Object[] { new ClientInfo(Inet4Address.getLocalHost().getHostAddress(),StubEnvType.JAVA), service, args },
				ExecutionResponse.class);
		ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
		return (T) mapper.readValue(response.getResultJson(), Class.forName(response.getInfo().getOutputType()));
	}

}
