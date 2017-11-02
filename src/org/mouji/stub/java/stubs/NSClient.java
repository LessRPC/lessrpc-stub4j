package org.mouji.stub.java.stubs;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.mouji.common.errors.DatabaseNotSupported;
import org.mouji.common.errors.RPCException;
import org.mouji.common.errors.RPCProviderFailureException;
import org.mouji.common.errors.ResponseContentTypeCannotBePrasedException;
import org.mouji.common.errors.SerializationFormatNotSupported;
import org.mouji.common.info.NameServerInfo;
import org.mouji.common.info.ServiceInfo;
import org.mouji.common.info.ServiceProviderInfo;
import org.mouji.common.info.ServiceSupportInfo;
import org.mouji.common.info.responses.ServiceResponse;
import org.mouji.common.serializer.Serializer;
import org.mouji.common.services.NameServerFunctions;
import org.mouji.common.services.NameServerServices;
import org.mouji.stub.java.JsonSerializer;

public class NSClient extends ClientStub implements NameServerServices, NameServerFunctions {

	/**
	 * Pointer to ServiceProviderInfo instance pointing to the ServiceProvider
	 * within the Name Server
	 */
	private final ServiceProviderInfo nsSPInfo;

	public NSClient(NameServerInfo nsInfo, List<Serializer> serializers) throws Exception {
		super(serializers);
		this.nsSPInfo = getInfo(nsInfo.getAddress(), nsInfo.getPort());

	}

	@Override
	public ServiceSupportInfo getProvider(ServiceInfo<?> service) throws ResponseContentTypeCannotBePrasedException,
			SerializationFormatNotSupported, RPCException, RPCProviderFailureException, IOException, Exception {
		ServiceResponse<ServiceSupportInfo> response = this.call(GET_PROVIDER, nsSPInfo, new Object[] { service },
				new JsonSerializer());
		return response.getContent();
	}

	@Override
	public ServiceSupportInfo[] getProviders(ServiceInfo<?> service) throws ResponseContentTypeCannotBePrasedException,
			SerializationFormatNotSupported, RPCException, RPCProviderFailureException, IOException, Exception {
		ServiceResponse<ServiceSupportInfo[]> response = this.call(GET_PROVIDERS, nsSPInfo, new Object[] { service },
				new JsonSerializer());
		return response.getContent();
	}

	@Override
	public ServiceSupportInfo[] getAllProviders() throws ResponseContentTypeCannotBePrasedException,
			SerializationFormatNotSupported, RPCException, RPCProviderFailureException, IOException, Exception {
		ServiceResponse<ServiceSupportInfo[]> response = this.call(GET_ALL_PROVIDERS, nsSPInfo, new Object[] {},
				new JsonSerializer());
		return response.getContent();
	}

	@Override
	public ServiceInfo<?> getServiceInfoByName(String serviceName) throws ResponseContentTypeCannotBePrasedException,
			SerializationFormatNotSupported, RPCException, RPCProviderFailureException, IOException, Exception {
		ServiceResponse<ServiceInfo<?>> response = this.call(GET_SERVICE_INFO_BY_NAME, nsSPInfo,
				new Object[] { serviceName }, new JsonSerializer());
		return response.getContent();
	}

	@Override
	public ServiceInfo<?> getServiceInfoById(int serviceId) throws ResponseContentTypeCannotBePrasedException,
			SerializationFormatNotSupported, RPCException, RPCProviderFailureException, IOException, Exception {
		ServiceResponse<ServiceInfo<?>> response = this.call(GET_SERVICE_INFO_BY_ID, nsSPInfo,
				new Object[] { serviceId }, new JsonSerializer());
		return response.getContent();
	}

	@Override
	public boolean register(ServiceSupportInfo support) throws ResponseContentTypeCannotBePrasedException,
			SerializationFormatNotSupported, RPCException, RPCProviderFailureException, IOException, Exception {
		ServiceResponse<Boolean> response = this.call(REGISTER, nsSPInfo, new Object[] { support },
				new JsonSerializer());
		return response.getContent();
	}

	@Override
	public boolean unregister(ServiceInfo<?> service, ServiceProviderInfo provider)
			throws ResponseContentTypeCannotBePrasedException, SerializationFormatNotSupported, RPCException,
			RPCProviderFailureException, IOException, Exception {
		ServiceResponse<Boolean> response = this.call(UNREGISTER, nsSPInfo, new Object[] { service, provider },
				new JsonSerializer());
		return response.getContent();
	}

	@Override
	public boolean ping() throws Exception {
		return ping(nsSPInfo);
	}

	@Override
	public boolean unregisterAll(ServiceProviderInfo provider) throws ClassNotFoundException, SQLException,
			DatabaseNotSupported, ResponseContentTypeCannotBePrasedException, SerializationFormatNotSupported,
			RPCException, RPCProviderFailureException, IOException, Exception {
		ServiceResponse<Boolean> response = this.call(UNREGISTER_ALL, nsSPInfo, new Object[] { provider },
				new JsonSerializer());
		return response.getContent();
	}

	@Override
	public boolean checkProviderStatus(ServiceProviderInfo provider) throws ResponseContentTypeCannotBePrasedException,
			SerializationFormatNotSupported, RPCException, RPCProviderFailureException, IOException, Exception {
		ServiceResponse<Boolean> response = this.call(CHECK_PROVIDER_STATUS, nsSPInfo, new Object[] { provider },
				new JsonSerializer());
		return response.getContent();
	}

}
