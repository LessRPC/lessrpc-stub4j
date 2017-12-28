package org.lessrpc.stub.java.cache;

import org.lessrpc.common.info.ServiceSupportInfo;

/**
 * This cache implementation just doesn't do anything. It works as nothing is
 * being cached
 * 
 * @author Salim
 *
 */
public class NoCache extends ServiceProviderCache {

	@Override
	public boolean cacheExists(int serviceId) {
		// ALWAYS FALSE
		return false;
	}

	@Override
	public ServiceSupportInfo get(int serviceId) {
		// ALWAYS NULL
		return null;
	}

	@Override
	public void clearCache(int serviceId) {
		// DO NOTHING
	}

	@Override
	public void reset() {
		// DO NOTHING
	}

	@Override
	public void cache(ServiceSupportInfo info) {
		// DO NOTHING

	}

}
