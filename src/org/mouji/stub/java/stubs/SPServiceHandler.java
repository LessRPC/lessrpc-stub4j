
package org.mouji.stub.java.stubs;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.mouji.common.errors.ApplicationSpecificErrorException;
import org.mouji.common.errors.ExecuteInternalError;
import org.mouji.common.errors.InvalidArgsException;
import org.mouji.common.errors.SerializationFormatHTTPNotParsable;
import org.mouji.common.errors.ServiceNotSupportedException;
import org.mouji.common.errors.UnderterminableCodeException;
import org.mouji.common.info.SerializationFormat;
import org.mouji.common.info.ServiceInfo;
import org.mouji.common.info.ServiceRequest;
import org.mouji.common.info.ServiceSupportInfo;
import org.mouji.common.info.responses.ExecuteRequestResponse;
import org.mouji.common.info.responses.IntegerResponse;
import org.mouji.common.info.responses.ProviderInfoResponse;
import org.mouji.common.info.responses.ServiceResponse;
import org.mouji.common.info.responses.ServiceSupportResponse;
import org.mouji.common.info.responses.TextResponse;
import org.mouji.common.serializer.Serializer;
import org.mouji.common.services.ServiceProvider;
import org.mouji.common.types.StatusType;
import org.mouji.stub.java.StubConstants;

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
				System.err.println("error happened that wasn't handled");
				// response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				// baseRequest.setHandled(true);
				try {
					sendStatus(StatusType.ACCEPT_TYPE_NOT_SUPPORTED, baseRequest, response,
							SerializationFormat.defaultFotmat());
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
			HttpServletResponse response, Serializer serializer, Serializer requestSerializer)
					throws UnderterminableCodeException, Exception {
		response.setContentType(serializer.getType().httpFormat());
		response.getOutputStream().write(serializer.serialize(
				new IntegerResponse(StatusType.OK.toCode(), provider.ping() ? 1 : 0), IntegerResponse.class));
		baseRequest.setHandled(true);

		return;
	}

	private void handleLessRPC(String target, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		boolean inputRequired = false;

		if (target.trim().toLowerCase().equals(LESS_RPC_REQUEST_EXECUTE)
				|| target.trim().toLowerCase().equals(LESS_RPC_REQUEST_SERVICE)) {
			inputRequired = true;
		}

		Serializer requestSerializer = null;
		SerializationFormat requestFormat = null;

		// check method to be POST
		if (inputRequired) {
			if (!request.getMethod().toLowerCase().equals("post")) {
				sendStatus(StatusType.WRONG_HTTP_METHOD, baseRequest, response, SerializationFormat.defaultFotmat());
				return;
			}
		}

		// check if content-type exists and it is parsable
		if (request.getHeader("Content-type") != null && request.getHeader("Content-type").length() > 0) {

			try {
				requestFormat = SerializationFormat.parseHTTPFormat(request.getHeader("Content-type"));
			} catch (Exception e) {
				sendStatus(StatusType.CONTENT_TYPE_CANNOT_BE_PARSED, baseRequest, response,
						SerializationFormat.defaultFotmat());
				return;
			}
			requestSerializer = stub.getSerializer(requestFormat);
			if (requestSerializer == null) {
				sendStatus(StatusType.CONTENT_TYPE_NOT_SUPPORTED, baseRequest, response,
						SerializationFormat.defaultFotmat());
				return;
			}
		} else {
			if (inputRequired) {
				sendStatus(StatusType.CONTENT_TYPE_CANNOT_BE_PARSED, baseRequest, response,
						SerializationFormat.defaultFotmat());
				return;
			}
		}

		// checking accept header exists
		if (request.getHeader("Accept") == null || request.getHeader("Accept").length() == 0) {
			sendStatus(StatusType.ACCEPT_TYPE_CANNOT_BE_PARSED, baseRequest, response,
					SerializationFormat.defaultFotmat());
			return;
		}

		// checking accepted formats
		SerializationFormat[] formats;
		try {
			formats = parseAcceptedFormats(request);
		} catch (Exception e) {
			sendStatus(StatusType.ACCEPT_TYPE_CANNOT_BE_PARSED, baseRequest, response,
					SerializationFormat.defaultFotmat());
			return;
		}

		// getting the first supported serializer
		SerializationFormat responeFormat = stub.findFirstAcceptedFormat(formats);
		if (responeFormat == null) {
			// no serialization is supported
			sendStatus(StatusType.ACCEPT_TYPE_NOT_SUPPORTED, baseRequest, response,
					SerializationFormat.defaultFotmat());
			return;
		}

		// getting first client's priority that is supported
		Serializer responseSerializer = stub.getSerializer(responeFormat);

		// -------- start handling response
		if (target.trim().toLowerCase().equals(LESS_RPC_REQUEST_PING)) {
			handlePing(target, baseRequest, request, response, responseSerializer, requestSerializer);
		} else if (target.trim().toLowerCase().equals(LESS_RPC_REQUEST_SERVICE)) {
			handleService(target, baseRequest, request, response, responseSerializer, requestSerializer);
		} else if (target.trim().toLowerCase().equals(LESS_RPC_REQUEST_INFO)) {
			handleInfo(target, baseRequest, request, response, responseSerializer, requestSerializer);
		} else if (target.trim().toLowerCase().equals(LESS_RPC_REQUEST_EXECUTE)) {
			handleExecute(target, baseRequest, request, response, responseSerializer, requestSerializer);
		} else {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			baseRequest.setHandled(true);
		}

	}

	private void handleInfo(String target, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response, Serializer responseSerializer, Serializer requestSerializer)
					throws Exception {

		response.setContentType(responseSerializer.getType().httpFormat());
		response.getOutputStream().write(responseSerializer.serialize(
				new ProviderInfoResponse(StatusType.OK.toCode(), provider.info()), ProviderInfoResponse.class));
		baseRequest.setHandled(true);

		return;

	}

	@SuppressWarnings("unchecked")
	private void handleExecute(String target, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response, Serializer responseSerializer, Serializer requestSerializer)
					throws Exception {

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
			serviceRequest = requestSerializer.deserialize(is, ServiceRequest.class);
		} catch (Exception e) {
			e.printStackTrace();
			sendStatus(StatusType.PARSE_ERROR, baseRequest, response, SerializationFormat.defaultFotmat());
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
			sendStatus(StatusType.INTERNAL_ERROR, baseRequest, response, SerializationFormat.defaultFotmat());
			return;
		} catch (InvalidArgsException e) {
			sendStatus(StatusType.INVALID_ARGS, baseRequest, response, SerializationFormat.defaultFotmat());
			return;
		} catch (ServiceNotSupportedException e) {
			sendStatus(StatusType.SERVICE_NOT_SUPPORTED, baseRequest, response, SerializationFormat.defaultFotmat());
			return;
		}

		if (serviceResponse == null) {
			sendStatus(StatusType.INTERNAL_ERROR, baseRequest, response, SerializationFormat.defaultFotmat());
			return;
		}

		// writing service response
		response.setContentType(responseSerializer.getType().httpFormat());
		responseSerializer.serialize(new ExecuteRequestResponse<>(StatusType.OK.toCode(), serviceResponse),
				ExecuteRequestResponse.class, System.out);
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
			sendStatus(StatusType.POST_CONTENT_NOT_AVAILABLE, baseRequest, response,
					SerializationFormat.defaultFotmat());
			return;
		}

		try {
			info = requestSerializer.deserialize(is, ServiceInfo.class);
		} catch (Exception e) {
			sendStatus(StatusType.PARSE_ERROR, baseRequest, response, SerializationFormat.defaultFotmat());
			return;
		}

		ServiceSupportInfo service = null;
		try {
			service = provider.service(info);
		} catch (ServiceNotSupportedException e) {
			sendStatus(StatusType.SERVICE_NOT_SUPPORTED, baseRequest, response, SerializationFormat.defaultFotmat());
			return;
		}

		if (service == null) {
			sendStatus(StatusType.INTERNAL_ERROR, baseRequest, response, SerializationFormat.defaultFotmat());
			return;
		}

		response.setContentType(responseSerializer.getType().httpFormat());

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
		// setting status of http
		response.setStatus(status.httpMatchingStatus());

		// sending output
		Serializer serializer = stub.getSerializer(format);

		response.getOutputStream()
				.write(serializer.serialize(new TextResponse(status.toCode(), status.name()), TextResponse.class));

		// setting request as handled
		baseRequest.setHandled(true);
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
