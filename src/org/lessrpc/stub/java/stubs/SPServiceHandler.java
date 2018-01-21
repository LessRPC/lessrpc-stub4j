
package org.lessrpc.stub.java.stubs;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.lessrpc.stub.java.StubConstants;
import org.lessrpc.common.errors.AcceptTypeHTTPFormatNotParsable;
import org.lessrpc.common.errors.AcceptTypeNotSupported;
import org.lessrpc.common.errors.ApplicationSpecificErrorException;
import org.lessrpc.common.errors.ContentTypeHTTPFormatNotParsable;
import org.lessrpc.common.errors.ContentTypeNotSupported;
import org.lessrpc.common.errors.ExecuteInternalError;
import org.lessrpc.common.errors.InvalidArgsException;
import org.lessrpc.common.errors.SerializationFormatHTTPNotParsable;
import org.lessrpc.common.errors.ServiceNotSupportedException;
import org.lessrpc.common.errors.UnderterminableCodeException;
import org.lessrpc.common.errors.WrongHTTPMethodException;
import org.lessrpc.common.info.SerializationFormat;
import org.lessrpc.common.info.ServiceInfo;
import org.lessrpc.common.info.ServiceLocator;
import org.lessrpc.common.info.ServiceRequest;
import org.lessrpc.common.info.ServiceSupportInfo;
import org.lessrpc.common.info.responses.ExecuteRequestResponse;
import org.lessrpc.common.info.responses.IntegerResponse;
import org.lessrpc.common.info.responses.ProviderInfoResponse;
import org.lessrpc.common.info.responses.ServiceResponse;
import org.lessrpc.common.info.responses.ServiceSupportResponse;
import org.lessrpc.common.info.responses.TextResponse;
import org.lessrpc.common.serializer.Serializer;
import org.lessrpc.common.services.ServiceProvider;
import org.lessrpc.common.types.StatusType;

public class SPServiceHandler extends AbstractHandler implements StubConstants {

	private final ServiceProvider provider;
	/**
	 * pointer to stub
	 */
	private ServerStub stub;

	public SPServiceHandler(ServiceProvider provider, ServerStub stub) {
		this.provider = provider;
		this.stub = stub;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		if (target.trim().toLowerCase().equals(LESS_RPC_REQUEST_PING)
				|| target.trim().toLowerCase().equals(LESS_RPC_REQUEST_SERVICE)
				|| target.trim().toLowerCase().equals(LESS_RPC_REQUEST_INFO)
				|| target.trim().toLowerCase().equals(LESS_RPC_REQUEST_EXECUTE)) {
		try {
				handleLessRPC(target, baseRequest, request, response);
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("error happened that wasn't handled");
				// response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				// baseRequest.setHandled(true);
				try {
					sendStatus(StatusType.INTERNAL_ERROR, baseRequest, response, SerializationFormat.defaultFotmat());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				return;
			}
		} else {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			baseRequest.setHandled(true);
		}

	}

	private void handlePing(String target, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response, Serializer responseSerializer, Serializer requestSerializer)
					throws UnderterminableCodeException, Exception {
		response.getOutputStream().write(responseSerializer.serialize(
				new IntegerResponse(StatusType.OK.toCode(), provider.ping() ? 1 : 0), IntegerResponse.class));
		baseRequest.setHandled(true);

		return;
	}

	private void handleLessRPC(String target, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) throws UnderterminableCodeException, Exception {
		boolean inputRequired = false;
		Serializer responseSerializer = null;
		Serializer requestSerializer = null;

		if (target.trim().toLowerCase().equals(LESS_RPC_REQUEST_EXECUTE)
				|| target.trim().toLowerCase().equals(LESS_RPC_REQUEST_SERVICE)) {
			inputRequired = true;
		}

		// check if accept type is parsable and supported
		try {
			responseSerializer = parseAcceptFormat(target, baseRequest, request, response);
		} catch (AcceptTypeHTTPFormatNotParsable e) {
			response.setContentType(SerializationFormat.defaultFotmat().httpFormat());
			sendStatus(StatusType.ACCEPT_TYPE_CANNOT_BE_PARSED, baseRequest, response,
					SerializationFormat.defaultFotmat());
			return;
		} catch (AcceptTypeNotSupported e) {
			response.setContentType(SerializationFormat.defaultFotmat().httpFormat());
			sendStatus(StatusType.ACCEPT_TYPE_NOT_SUPPORTED, baseRequest, response,
					SerializationFormat.defaultFotmat());
			return;
		}
		// setting response serialization format regardless if there is an error
		// or successful attempt
		response.setContentType(responseSerializer.getType().httpFormat());

		// check if get and post are being used properly
		try {
			checkHTTPMethodType(target, baseRequest, request, response, inputRequired);
		} catch (WrongHTTPMethodException e) {
			sendStatus(StatusType.WRONG_HTTP_METHOD, baseRequest, response, responseSerializer.getType());
			return;
		}

		// check if content-type exists and it is parsable and supported
		try {
			requestSerializer = parseContentType(target, baseRequest, request, response, inputRequired);
		} catch (ContentTypeHTTPFormatNotParsable e) {
			response.setContentType(SerializationFormat.defaultFotmat().httpFormat());
			sendStatus(StatusType.CONTENT_TYPE_CANNOT_BE_PARSED, baseRequest, response, responseSerializer.getType());
			return;
		} catch (ContentTypeNotSupported e) {
			response.setContentType(SerializationFormat.defaultFotmat().httpFormat());
			sendStatus(StatusType.CONTENT_TYPE_NOT_SUPPORTED, baseRequest, response, responseSerializer.getType());
			return;
		}

		response.setContentType(responseSerializer.getType().httpFormat());
		
		// -------- start handling response
		if (target.trim().toLowerCase().equals(LESS_RPC_REQUEST_PING)) {
			handlePing(target, baseRequest, request, response, responseSerializer, requestSerializer);
		} else if (target.trim().toLowerCase().equals(LESS_RPC_REQUEST_SERVICE)) {
			handleService(target, baseRequest, request, response, responseSerializer, requestSerializer);
		} else if (target.trim().toLowerCase().equals(LESS_RPC_REQUEST_INFO)) {
			handleInfo(target, baseRequest, request, response, responseSerializer, requestSerializer);
		} else if (target.trim().toLowerCase().equals(LESS_RPC_REQUEST_EXECUTE)) {
			handleExecute(target, baseRequest, request, response, responseSerializer, requestSerializer,
					ServiceLocator.create(provider.listServices()));
		} else {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			baseRequest.setHandled(true);
		}

	}

	private Serializer parseContentType(String target, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response, boolean inputRequired)
					throws ContentTypeHTTPFormatNotParsable, ContentTypeNotSupported {

		String contentType = request.getHeader("Content-type");

		SerializationFormat requestFormat = null;
		Serializer requestSerializer = null;

		if (contentType != null && contentType.length() > 0) {

			try {
				requestFormat = SerializationFormat.parseHTTPFormat(request.getHeader("Content-type"));
			} catch (Exception e) {
				throw new ContentTypeHTTPFormatNotParsable(contentType);
			}
			requestSerializer = stub.getSerializer(requestFormat);
			if (requestSerializer == null) {
				throw new ContentTypeNotSupported(requestFormat);
			}
		} else {
			if (inputRequired) {
				throw new ContentTypeHTTPFormatNotParsable(contentType);
			}
		}

		return requestSerializer;
	}

	private void checkHTTPMethodType(String target, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response, boolean inputRequired) throws WrongHTTPMethodException {
		// check method to be POST
		if (inputRequired) {
			if (!request.getMethod().toLowerCase().equals("post")) {
				throw new WrongHTTPMethodException();
			}
		}
	}

	private Serializer parseAcceptFormat(String target, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) throws AcceptTypeHTTPFormatNotParsable, AcceptTypeNotSupported {
		// checking accept header exists
		String acceptType = request.getHeader("Accept");
		if (acceptType == null || acceptType.length() == 0) {
			throw new AcceptTypeHTTPFormatNotParsable(acceptType);
		}
		// checking accepted formats
		SerializationFormat[] formats;
		try {
			formats = parseAcceptedFormats(request);
		} catch (Exception e) {
			throw new AcceptTypeHTTPFormatNotParsable(acceptType);
		}

		if (formats == null || formats.length == 0) {
			// TODO add AcceptType not available error
			throw new AcceptTypeHTTPFormatNotParsable(acceptType);
		}

		// getting the first supported serializer
		SerializationFormat responeFormat = stub.findFirstAcceptedFormat(formats);
		if (responeFormat == null) {
			throw new AcceptTypeNotSupported(responeFormat);
		}

		// getting first client's priority that is supported
		return stub.getSerializer(responeFormat);
	}

	private void handleInfo(String target, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response, Serializer responseSerializer, Serializer requestSerializer)
					throws Exception {
		response.getOutputStream().write(responseSerializer.serialize(
				new ProviderInfoResponse(StatusType.OK.toCode(), provider.info()), ProviderInfoResponse.class));
		baseRequest.setHandled(true);

		return;

	}

	private void handleExecute(String target, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response, Serializer responseSerializer, Serializer requestSerializer,
			ServiceLocator locator) throws Exception {

		// input to read
		ServiceRequest serviceRequest = null;

		ServletInputStream is = null;

		// checking if there is a content to POST request
		try {
			is = request.getInputStream();
		} catch (Exception e) {
			sendStatus(StatusType.POST_CONTENT_NOT_AVAILABLE, baseRequest, response,
					SerializationFormat.defaultFotmat());
			return;
		}
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String str = reader.readLine();
			ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes());
			serviceRequest = requestSerializer.deserialize(in, ServiceRequest.class, locator);
		} catch (Exception e) {
			e.printStackTrace();
			sendStatus(StatusType.PARSE_ERROR, baseRequest, response, responseSerializer.getType());
			return;
		}

		// execute service
		ServiceResponse<?> serviceResponse = null;
		try {
			// getting response
			serviceResponse = provider.execute(serviceRequest);

		} catch (ApplicationSpecificErrorException e) {
			// setting status of http
			response.setStatus(StatusType.APPLICATION_SPECIFIC_ERROR.httpMatchingStatus());

			// sending output
			response.getOutputStream().write(responseSerializer
					.serialize(new TextResponse(e.getErrorCode(), e.getContent()), TextResponse.class));
			// setting request as handled
			baseRequest.setHandled(true);
		} catch (ExecuteInternalError e) {
			sendStatus(StatusType.INTERNAL_ERROR, baseRequest, response, responseSerializer.getType());
			return;
		} catch (InvalidArgsException e) {
			sendStatus(StatusType.INVALID_ARGS, e.getMessage(), baseRequest, response, responseSerializer.getType());
			return;
		} catch (ServiceNotSupportedException e) {
			sendStatus(StatusType.SERVICE_NOT_SUPPORTED, baseRequest, response, responseSerializer.getType());
			return;
		}

		if (serviceResponse == null) {
			sendStatus(StatusType.INTERNAL_ERROR, baseRequest, response, responseSerializer.getType());
			return;
		}

		// writing service response
		response.setContentType(responseSerializer.getType().httpFormat());
		// serializing response
		responseSerializer.serialize(new ExecuteRequestResponse<>(StatusType.OK.toCode(), serviceResponse),
				ExecuteRequestResponse.class, response.getOutputStream());
		// setting as handled
		baseRequest.setHandled(true);

		return;

	}

	private void handleService(String target, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response, Serializer responseSerializer, Serializer requestSerializer)
					throws UnderterminableCodeException, Exception {
		// input to read
		ServiceInfo<?> info = null;

		ServletInputStream is = null;

		// checking if there is a content to POST request
		try {
			is = request.getInputStream();
		} catch (Exception e) {
			sendStatus(StatusType.POST_CONTENT_NOT_AVAILABLE, baseRequest, response, responseSerializer.getType());
			return;
		}

		try {
			info = requestSerializer.deserialize(is, ServiceInfo.class);
		} catch (Exception e) {
			sendStatus(StatusType.PARSE_ERROR, baseRequest, response, responseSerializer.getType());
			return;
		}

		ServiceSupportInfo service = null;
		try {
			service = provider.service(info);
		} catch (ServiceNotSupportedException e) {
			sendStatus(StatusType.SERVICE_NOT_SUPPORTED, baseRequest, response, responseSerializer.getType());
			return;
		}

		if (service == null) {
			sendStatus(StatusType.INTERNAL_ERROR, baseRequest, response, responseSerializer.getType());
			return;
		}

		responseSerializer.serialize(new ServiceSupportResponse(StatusType.OK.toCode(), service),
				ServiceSupportResponse.class, response.getOutputStream());

		baseRequest.setHandled(true);

		return;
	}

	public ServiceProvider getProvider() {
		return provider;
	}

	private void sendStatus(StatusType status, Request baseRequest, HttpServletResponse response,
			SerializationFormat format) throws Exception {
		sendStatus(status, status.name(), baseRequest, response, format);
	}

	private void sendStatus(StatusType status, String statusContent, Request baseRequest, HttpServletResponse response,
			SerializationFormat format) throws Exception {
		// setting status of http
		response.setStatus(status.httpMatchingStatus());
		
		response.setContentType(format.httpFormat());

		// sending output
		Serializer serializer = stub.getSerializer(format);

		response.getOutputStream()
				.write(serializer.serialize(new TextResponse(status.toCode(), statusContent), TextResponse.class));

		// setting request as handled
		baseRequest.setHandled(true);
		response.flushBuffer();
	}

	public SerializationFormat[] parseAcceptedFormats(HttpServletRequest request)
			throws SerializationFormatHTTPNotParsable {
		String acceptedTxt = request.getHeader("Accept");
		String[] parts = acceptedTxt.split(",");

		SerializationFormat[] formats = new SerializationFormat[parts.length];

		for (int i = 0; i < parts.length; i++) {
			formats[i] = SerializationFormat.parseHTTPFormat(parts[i]);
		}

		return formats;
	}

}
