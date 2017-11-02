package org.mouji.stub.java.stubs;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.mouji.common.errors.DatabaseNotSupported;
import org.mouji.common.errors.RPCException;
import org.mouji.common.errors.RPCProviderFailureException;
import org.mouji.common.errors.ResponseContentTypeCannotBePrasedException;
import org.mouji.common.errors.SerializationFormatNotSupported;
import org.mouji.common.info.ServiceProviderInfo;
import org.mouji.common.info.ServiceSupportInfo;
import org.mouji.common.serializer.Serializer;

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
		for (ServiceSupportInfo support : getProvider().listSupport()) {
			ns.unregisterAll(support.getProvider());
		}
	}

}
