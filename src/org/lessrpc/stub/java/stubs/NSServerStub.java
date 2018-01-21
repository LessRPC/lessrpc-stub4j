package org.lessrpc.stub.java.stubs;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.lessrpc.common.errors.DatabaseNotSupported;
import org.lessrpc.common.errors.RPCException;
import org.lessrpc.common.errors.RPCProviderFailureException;
import org.lessrpc.common.errors.ResponseContentTypeCannotBePrasedException;
import org.lessrpc.common.errors.SerializationFormatNotSupported;
import org.lessrpc.common.info.ServiceProviderInfo;
import org.lessrpc.common.info.ServiceSupportInfo;
import org.lessrpc.common.serializer.Serializer;

public class NSServerStub extends ServerStub {

	private NSClient ns;

	public NSServerStub(int port, ServiceProviderInfo nsInfo, List<Serializer> serializers) {
		super(port, serializers);
		ns = new NSClient(nsInfo, serializers);
	}

	/**
	 * Registers all supported services for this provider
	 */
	@Override
	protected void afterStart() throws ResponseContentTypeCannotBePrasedException, SerializationFormatNotSupported,
			RPCException, RPCProviderFailureException, IOException, Exception {
		for (ServiceSupportInfo support : getProvider().listSupport()) {
			ns.register(support);
		}
	}

	/**
	 * unregisters all services just before stopping the server stub
	 */
	protected void beforeStop() throws ClassNotFoundException, SQLException, DatabaseNotSupported,
			ResponseContentTypeCannotBePrasedException, SerializationFormatNotSupported, RPCException,
			RPCProviderFailureException, IOException, Exception {
		ns.unregisterAll(getProvider().info());
	}

}
