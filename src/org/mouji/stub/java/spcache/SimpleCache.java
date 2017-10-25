package org.mouji.stub.java.spcache;

import java.util.HashMap;

import org.mouji.common.info.ServiceSupportInfo;

/**
 * Simply keeps one instance of the provider per cache
 * 
 * @author Salim
 *
 */
public class SimpleCache extends ServiceProviderCache {

	/**
	 * service -> ServiceProviderInfo map
	 */
	private final HashMap<Integer, ServiceSupportInfo> map;

	public SimpleCache() {
		this.map = new HashMap<Integer, ServiceSupportInfo>();
	}

	@Override
	public boolean cacheExists(int serviceId) {
		return map.containsKey(serviceId);
	}

	@Override
	public ServiceSupportInfo get(int serviceId) {
		return map.get(serviceId);
	}

	@Override
	public void clearCache(int serviceId) {
		map.remove(serviceId);
	}

	@Override
	public void reset() {
		map.clear();
	}

	@Override
	public void cache(ServiceSupportInfo info) {
		this.map.put(info.getService().getId(), info);
	}

}
