package org.lessrpc.stub.java.stubs;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.lessrpc.stub.java.serializer.JsonSerializer;
import org.lessrpc.common.errors.DatabaseNotSupported;
import org.lessrpc.common.errors.RPCException;
import org.lessrpc.common.errors.RPCProviderFailureException;
import org.lessrpc.common.errors.ResponseContentTypeCannotBePrasedException;
import org.lessrpc.common.errors.SerializationFormatNotSupported;
import org.lessrpc.common.info.ServiceInfo;
import org.lessrpc.common.info.ServiceProviderInfo;
import org.lessrpc.common.info.ServiceSupportInfo;
import org.lessrpc.common.info.responses.ServiceResponse;
import org.lessrpc.common.serializer.Serializer;
import org.lessrpc.common.services.NameServerFunctions;
import org.lessrpc.common.services.NameServerServices;

public class NSClient extends ClientStub implements NameServerServices, NameServerFunctions {

	/**
	 * Pointer to ServiceProviderInfo instance pointing to the ServiceProvider
	 * within the Name Server
	 */
	private final ServiceProviderInfo nsSPInfo;

	public NSClient(ServiceProviderInfo nsInfo, List<Serializer> serializers)  {
		super(serializers);
		this.nsSPInfo = nsInfo;

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
