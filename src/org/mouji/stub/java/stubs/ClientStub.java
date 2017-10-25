package org.mouji.stub.java.stubs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.mouji.stub.java.StubConstants;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.client.util.OutputStreamContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.mouji.common.serializer.Serializer;
import org.mouji.common.errors.RPCException;
import org.mouji.common.errors.RPCProviderFailureException;
import org.mouji.common.errors.ResponseContentTypeCannotBePrasedException;
import org.mouji.common.errors.SerializationFormatNotSupported;
import org.mouji.common.info.SerializationFormat;
import org.mouji.common.info.ServiceInfo;
import org.mouji.common.info.ServiceProviderInfo;
import org.mouji.common.info.ServiceRequest;
import org.mouji.common.info.ServiceSupportInfo;
import org.mouji.common.info.responses.ExecuteRequestResponse;
import org.mouji.common.info.responses.IntegerResponse;
import org.mouji.common.info.responses.ProviderInfoResponse;
import org.mouji.common.info.responses.RequestResponse;
import org.mouji.common.info.responses.ServiceResponse;
import org.mouji.common.info.responses.ServiceSupportResponse;
import org.mouji.common.info.responses.TextResponse;

/**
 * Simple client stub class used in Less-RPC that doesn't support a NameServer
 * 
 * @author Salim
 *
 */
public class ClientStub extends BasicStub implements StubConstants {

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

		HttpClient client = new HttpClient();
		client.start();

		InputStreamResponseListener listener = new InputStreamResponseListener();

		client.newRequest(HTTP_PROTOCOL + info.getURL() + ":" + info.getPort() + LESS_RPC_REQUEST_PING)
				.method(HttpMethod.GET).accept(getAcceptedTypes()).send(listener);

		IntegerResponse ping = readResponse(listener, IntegerResponse.class, HTTP_WAIT_TIME_SHORT);

		client.stop();

		if (ping.getContent() == 1) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * calls /info service of a service provider
	 * 
	 * @param info
	 * @return
	 * @throws Exception
	 */
	public ServiceProviderInfo getInfo(String url, int port) throws Exception {
		HttpClient client = new HttpClient();
		client.start();

		InputStreamResponseListener listener = new InputStreamResponseListener();

		client.newRequest(HTTP_PROTOCOL + url + ":" + port + LESS_RPC_REQUEST_INFO).method(HttpMethod.GET)
				.accept(getAcceptedTypes()).send(listener);

		ProviderInfoResponse infoResponse = readResponse(listener, ProviderInfoResponse.class, HTTP_WAIT_TIME_SHORT);

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

		client.newRequest(HTTP_PROTOCOL + info.getURL() + ":" + info.getPort() + LESS_RPC_REQUEST_SERVICE)
				.method(HttpMethod.POST).accept(getAcceptedTypes())
				.header("content-type", SerializationFormat.defaultFotmat().httpFormat())
				.content(new BytesContentProvider(serializer.serialize(service, ServiceInfo.class))).send(listener);

		ServiceSupportResponse supportResponse = readResponse(listener, ServiceSupportResponse.class,
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
	@SuppressWarnings("unchecked")
	public <T> ServiceResponse<T> call(ServiceInfo<T> service, ServiceProviderInfo info, Object[] args,
			Serializer serializer) throws ResponseContentTypeCannotBePrasedException, SerializationFormatNotSupported,
					RPCException, RPCProviderFailureException, IOException, Exception {

		ServiceRequest request = ServiceRequest.create(service, genRandomId(), args);

		HttpClient client = new HttpClient();
		client.start();

		ExecuteRequestResponse<T> execResponse = null;

		OutputStreamContentProvider content = new OutputStreamContentProvider();

		try (OutputStream output = content.getOutputStream()) {

			InputStreamResponseListener listener = new InputStreamResponseListener();

			client.newRequest(HTTP_PROTOCOL + info.getURL() + ":" + info.getPort() + LESS_RPC_REQUEST_EXECUTE)
					.method(HttpMethod.POST).accept(getAcceptedTypes())
					.header("content-type", SerializationFormat.defaultFotmat().httpFormat()).content(content)
					.send(listener);

			serializer.serialize(request, ServiceRequest.class, output);
			output.flush();
			output.close();

			execResponse = readResponse(listener, ExecuteRequestResponse.class, HTTP_WAIT_TIME_SHORT);
		}

		client.stop();

		// System.out.println(execResponse);
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
			long timeout) throws ResponseContentTypeCannotBePrasedException, SerializationFormatNotSupported,
					RPCException, RPCProviderFailureException, IOException, Exception {

		// Wait for the response headers to arrive
		Response response = listener.get(5, TimeUnit.SECONDS);

		String contentType = response.getHeaders().get("Content-type");

		System.out.println(contentType);
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
			System.out.println(error);
			throw new RPCException(error.getStatus(), error.getContent());
		}

		// status is OK so read response
		// Use try-with-resources to close input stream.
		try (InputStream responseContent = listener.getInputStream()) {
			// byte[] bytes = new
			// Scanner(responseContent).useDelimiter("\\Z").next().getBytes();
			// System.out.println(new String(bytes));
			return serializer.deserialize(responseContent, cls);
		}

	}

	/**
	 * reads error messasge from input
	 * 
	 * @param listener
	 * @param serializer
	 * @return
	 * @throws RPCProviderFailureException
	 */
	private TextResponse readError(InputStreamResponseListener listener, Serializer serializer)
			throws RPCProviderFailureException {
		// status is OK so read response
		// Use try-with-resources to close input stream.
		try {
			try (InputStream responseContent = listener.getInputStream()) {
				return serializer.deserialize(responseContent, TextResponse.class);
			}
		} catch (Exception e) {
			throw new RPCProviderFailureException();
		}
	}

}
