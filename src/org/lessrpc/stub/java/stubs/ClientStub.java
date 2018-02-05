package org.lessrpc.stub.java.stubs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64InputStream;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.client.util.OutputStreamContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.lessrpc.stub.java.StubConstants;
import org.lessrpc.common.errors.ApplicationSpecificErrorException;
import org.lessrpc.common.errors.RPCException;
import org.lessrpc.common.errors.RPCProviderFailureException;
import org.lessrpc.common.errors.ResponseContentTypeCannotBePrasedException;
import org.lessrpc.common.errors.SerializationFormatNotSupported;
import org.lessrpc.common.info.SerializationFormat;
import org.lessrpc.common.info.ServiceDescription;
import org.lessrpc.common.info.ServiceInfo;
import org.lessrpc.common.info.ServiceLocator;
import org.lessrpc.common.info.ServiceProviderInfo;
import org.lessrpc.common.info.ServiceRequest;
import org.lessrpc.common.info.ServiceSupportInfo;
import org.lessrpc.common.info.responses.ExecuteRequestResponse;
import org.lessrpc.common.info.responses.IntegerResponse;
import org.lessrpc.common.info.responses.ProviderInfoResponse;
import org.lessrpc.common.info.responses.RequestResponse;
import org.lessrpc.common.info.responses.ServiceResponse;
import org.lessrpc.common.info.responses.ServiceSupportResponse;
import org.lessrpc.common.info.responses.TextResponse;
import org.lessrpc.common.serializer.Serializer;

/**
 * Simple client stub class used in Less-RPC that doesn't support a NameServer
 * 
 * @author Salim
 *
 */
public class ClientStub extends Stub implements StubConstants {

	public ClientStub(List<Serializer> serializers) {
		super(serializers);
	}

	//

	/**
	 * pings a service provider to assure it working. It will return true of
	 * service provide is working and running propery. It will return false if
	 * provide doesn't exist on network or of the provider provides a false due
	 * to internal system error
	 * 
	 * @param info
	 * @return
	 * @throws Exception
	 */
	public boolean ping(ServiceProviderInfo info) throws Exception {
		boolean flag = true;
		HttpClient client = new HttpClient();
		client.start();

		InputStreamResponseListener listener = new InputStreamResponseListener();

		client.newRequest(HTTP_PROTOCOL + info.getURL() + ":" + info.getPort() + LESS_RPC_REQUEST_PING)
				.method(HttpMethod.GET)
				.accept(getAcceptedTypes(new SerializationFormat[] { SerializationFormat.defaultFotmat() }))
				.send(listener);
		IntegerResponse ping = null;
		try {
			ping = readResponse(listener, IntegerResponse.class, null, HTTP_WAIT_TIME_SHORT);
		} catch (ResponseContentTypeCannotBePrasedException e) {
			throw e;
		} catch (SerializationFormatNotSupported e) {
			throw e;
		} catch (RPCException e) {
			throw e;
		} catch (RPCProviderFailureException e) {
			throw e;
		} catch (IOException e) {
			flag = false;
		} catch (Exception e) {
			flag = false;
		}

		client.stop();

		if (!flag || ping == null || ping.getContent() != 1) {
			return false;
		} else {
			return true;
		}

	}

	/**
	 * calls /info service of a service provider
	 * 
	 * @param info
	 * @return
	 * @throws IOException
	 * @throws RPCProviderFailureException
	 * @throws RPCException
	 * @throws SerializationFormatNotSupported
	 * @throws ResponseContentTypeCannotBePrasedException
	 * @throws Exception
	 */
	public ServiceProviderInfo getInfo(String url, int port) throws ResponseContentTypeCannotBePrasedException,
			SerializationFormatNotSupported, RPCException, RPCProviderFailureException, IOException, Exception {
		HttpClient client = new HttpClient();
		client.start();

		InputStreamResponseListener listener = new InputStreamResponseListener();

		client.newRequest(HTTP_PROTOCOL + url + ":" + port + LESS_RPC_REQUEST_INFO).method(HttpMethod.GET)
				.accept(getAcceptedTypes(new SerializationFormat[] { SerializationFormat.defaultFotmat() }))
				.send(listener);

		ProviderInfoResponse infoResponse = readResponse(listener, ProviderInfoResponse.class, null,
				HTTP_WAIT_TIME_SHORT);

		client.stop();

		return infoResponse.getContent();
	}

	/**
	 * executes /service service for given service to given service provider
	 * 
	 * @param info
	 * @param service
	 * @return
	 * @throws Exception
	 */
	public ServiceSupportInfo getServiceSupport(ServiceProviderInfo info, ServiceInfo<?> service) throws Exception {
		Serializer serializer = getSerializer(SerializationFormat.defaultFotmat());

		HttpClient client = new HttpClient();
		client.start();

		InputStreamResponseListener listener = new InputStreamResponseListener();

		byte[] out = serializer.serialize(service, ServiceInfo.class);
		if (org.lessrpc.stub.java.io.Base64.DO_BASE64) {
			out = Base64.getEncoder().encode(out);
		}

		client.newRequest(HTTP_PROTOCOL + info.getURL() + ":" + info.getPort() + LESS_RPC_REQUEST_SERVICE)
				.method(HttpMethod.POST)
				.accept(getAcceptedTypes(new SerializationFormat[] { SerializationFormat.defaultFotmat() }))
				.header("content-type", SerializationFormat.defaultFotmat().httpFormat())
				.content(new BytesContentProvider(out)).send(listener);

		ServiceSupportResponse supportResponse = readResponse(listener, ServiceSupportResponse.class, null,
				HTTP_WAIT_TIME_SHORT);

		client.stop();

		return supportResponse.getContent();
	}

	/**
	 * calls execute service of given service provider
	 * 
	 * @param service
	 * @param info
	 * @param args
	 * @param serializer
	 * @return
	 * @throws ResponseContentTypeCannotBePrasedException
	 * @throws SerializationFormatNotSupported
	 * @throws RPCException
	 * @throws RPCProviderFailureException
	 * @throws IOException
	 * @throws Exception
	 */
	public <T> ServiceResponse<T> call(ServiceDescription<T> service, ServiceProviderInfo info, Object[] args,
			Serializer serializer) throws ResponseContentTypeCannotBePrasedException, SerializationFormatNotSupported,
					RPCException, RPCProviderFailureException, IOException, Exception {

		return this.call(service, info, args, serializer, null);

	}

	/**
	 * calls execute service of given service provider
	 * 
	 * @param service
	 * @param info
	 * @param args
	 * @param serializer
	 * @return
	 * @throws ResponseContentTypeCannotBePrasedException
	 * @throws SerializationFormatNotSupported
	 * @throws RPCException
	 * @throws RPCProviderFailureException
	 * @throws IOException
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public <T> ServiceResponse<T> call(ServiceDescription<T> service, ServiceProviderInfo info, Object[] args,
			Serializer serializer, SerializationFormat[] accept) throws ResponseContentTypeCannotBePrasedException,
					SerializationFormatNotSupported, RPCException, RPCProviderFailureException, IOException, Exception {

		ServiceRequest request = ServiceRequest.create(service.getInfo(), genRandomId(), args);

		HttpClient client = new HttpClient();
		client.start();

		ExecuteRequestResponse<T> execResponse = null;

		OutputStreamContentProvider content = new OutputStreamContentProvider();

		OutputStream out = null;
		try (OutputStream output = content.getOutputStream()) {
			out = output;
			InputStreamResponseListener listener = new InputStreamResponseListener();

			client.newRequest(HTTP_PROTOCOL + info.getURL() + ":" + info.getPort() + LESS_RPC_REQUEST_EXECUTE)
					.method(HttpMethod.POST).accept(getAcceptedTypes(accept))
					.header("content-type", serializer.getType().httpFormat()).content(content).send(listener);
			out.flush();
			out.close();

			execResponse = readResponse(listener, ExecuteRequestResponse.class, service, HTTP_WAIT_TIME_LONG);
		}

		client.stop();

		return execResponse.getContent();

	}

	/**
	 * 
	 * Read response from input stream
	 * 
	 * @param listener
	 * @param cls
	 * @param timeout
	 * @return
	 * @throws ResponseContentTypeCannotBePrasedException
	 * @throws SerializationFormatNotSupported
	 * @throws RPCException
	 * @throws RPCProviderFailureException
	 * @throws IOException
	 * @throws Exception
	 */
	protected <T extends RequestResponse<?>> T readResponse(InputStreamResponseListener listener, Class<T> cls,
			ServiceDescription<?> desc, long timeout) throws ResponseContentTypeCannotBePrasedException,
					SerializationFormatNotSupported, RPCException, RPCProviderFailureException, IOException, Exception {

		// Wait for the response headers to arrive
		Response response = listener.get(timeout, TimeUnit.SECONDS);

		// TODO handle response status 404, 200 and etc..

		String contentType = response.getHeaders().get("Content-type");

		if (contentType == null || contentType.length() < 1) {
			throw new ResponseContentTypeCannotBePrasedException(contentType);
		}
		// read format
		SerializationFormat format = null;
		try {
			format = SerializationFormat.parseHTTPFormat(contentType);
		} catch (Exception e) {
			throw new ResponseContentTypeCannotBePrasedException(contentType);
		}
		// if no format was read
		if (format == null) {
			throw new ResponseContentTypeCannotBePrasedException(contentType);
		}

		Serializer serializer = getSerializer(format);

		if (serializer == null) {
			throw new SerializationFormatNotSupported(format);
		}

		// checking status
		if (response.getStatus() != HttpServletResponse.SC_OK) {
			TextResponse error = readError(listener, serializer);
			if (error.getStatus() > 3000) {
				throw new ApplicationSpecificErrorException(error.getStatus(), error.getContent());
			} else {
				throw new RPCException(error.getStatus(), error.getContent());
			}
		}

		// status is OK so read response
		// Use try-with-resources to close input stream.
		try (InputStream responseContent = listener.getInputStream()) {

			InputStream in = responseContent;
			if (org.lessrpc.stub.java.io.Base64.DO_BASE64) {
				in = new Base64InputStream(responseContent, false);
			}

			if (desc != null) {
				return serializer.deserialize(in, cls, ServiceLocator.create(desc));
			} else {
				return serializer.deserialize(in, cls);
			}

		}

	}

	/**
	 * reads error message from input
	 * 
	 * @param listener
	 * @param serializer
	 * @return
	 * @throws RPCProviderFailureException
	 */
	private TextResponse readError(InputStreamResponseListener listener, Serializer serializer)
			throws RPCProviderFailureException {
		// Use try-with-resources to close input stream.
		try {
			try (InputStream responseContent = listener.getInputStream()) {
				InputStream in = responseContent;
				if (org.lessrpc.stub.java.io.Base64.DO_BASE64) {
					in = new Base64InputStream(responseContent, false);
				}
				return serializer.deserialize(in, TextResponse.class);
			}
		} catch (Exception e) {
			throw new RPCProviderFailureException();
		}
	}

}
