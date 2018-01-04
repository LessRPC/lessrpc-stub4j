package org.lessrpc.stub.java.cache;

import org.lessrpc.common.info.ServiceInfo;
import org.lessrpc.common.info.ServiceSupportInfo;

/**
 * Abstract ServiceProvider Cache
 * 
 * @author Salim
 *
 */
public abstract class ServiceProviderCache {

	/**
	 * indicates if there stub has a cached service provider info for the given
	 * service info
	 * 
	 * @param serviceId
	 * @return
	 */
	public abstract boolean cacheExists(int serviceId);

	/**
	 * indicates if there stub has a cached service provider info for the given
	 * service info
	 * 
	 * @param service
	 * @return
	 */
	public boolean cacheExists(ServiceInfo<?> service) {
		return cacheExists(service.getId());
	}

	/**
	 * returns the cached ServiceProviderInfo for the given service
	 * 
	 * @param serviceId
	 * @return
	 */
	public abstract ServiceSupportInfo get(int serviceId);

	/**
	 * returns the cached ServiceProviderInfo for the given service
	 * 
	 * @param service
	 * @return
	 */
	public ServiceSupportInfo get(ServiceInfo<?> service) {
		return get(service.getId());
	}

	/**
	 * 
	 * clears the cache for the given service
	 * 
	 * @param service
	 */
	public abstract void clear(int serviceId);

	/**
	 * 
	 * clears the cache for the given service
	 * 
	 * @param service
	 */
	public void clear(ServiceInfo<?> service) {
		clear(service.getId());
	}
	
	public abstract void reset();

	/**
	 * Caches a support entry for the service
	 * @param info
	 */
	public abstract void cache(ServiceSupportInfo info);
	
	
}
