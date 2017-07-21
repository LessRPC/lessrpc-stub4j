package me.salimm.jrns.stub.java;

import java.net.URL;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;

import me.salimm.jrns.common.info.NameServerInfo;
import me.salimm.jrns.common.info.ServiceInfo;
import me.salimm.jrns.common.services.NSService;

public class JRNSSTub implements StubConstants {

	public JRNSSTub() {
	}

	public ServiceInfo<Integer> getServiceInfo(NameServerInfo info, int id) throws Throwable {
		JsonRpcHttpClient client = new JsonRpcHttpClient(
				new URL(RPC_PROTOCOL+info.getAddress() + ":" + info.getPort() + "/" + NSService.class.getSimpleName() + ".json"));
		return client.invoke("getServiceInfo", id, ServiceInfo.class);
	}
	

	public ServiceInfo<Integer> getServiceInfo(NameServerInfo info, String name) throws Throwable {
		JsonRpcHttpClient client = new JsonRpcHttpClient(
				new URL(RPC_PROTOCOL+info.getAddress() + ":" + info.getPort() + "/" + NSService.class.getSimpleName() + ".json"));
		return client.invoke("getServiceInfo", new Object[]{name}, ServiceInfo.class);
	}
}
