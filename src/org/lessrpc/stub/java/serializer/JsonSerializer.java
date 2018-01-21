package org.lessrpc.stub.java.serializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.lessrpc.common.info.EnvironmentInfo;
import org.lessrpc.common.info.SerializationFormat;
import org.lessrpc.common.info.ServiceInfo;
import org.lessrpc.common.info.ServiceLocator;
import org.lessrpc.common.info.ServiceRequest;
import org.lessrpc.common.info.responses.ExecuteRequestResponse;
import org.lessrpc.common.info.responses.ServiceResponse;
import org.lessrpc.common.serializer.Serializer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSerializer extends Serializer {

	/**
	 * mapper pointer
	 */
	private ObjectMapper mapper;

	public JsonSerializer() {
		mapper = new ObjectMapper(new JsonFactory());
	}

	public JsonSerializer(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public <T> byte[] serialize(T obj, Class<T> cls) throws Exception {
		return mapper.writeValueAsBytes(obj);
	}

	@Override
	public <T> T deserialize(byte[] data, Class<T> cls) throws Exception {
		return mapper.readValue(data, cls);
	}

	@Override
	public <T> void serialize(T obj, Class<T> cls, OutputStream os) throws Exception {
		mapper.writeValue(os, obj);
	}

	@Override
	public <T> T deserialize(InputStream is, Class<T> cls) throws Exception {
		return mapper.readValue(is, cls);
	}

	@Override
	public Serializer prepare(String schema) {
		return this;
	}

	@Override
	public Serializer prepare(Module module) {
		ObjectMapper map = mapper.copy();
		map.registerModule(module);

		return new JsonSerializer(map);
	}

	@Override
	public Serializer prepare(Module module, String schema) {
		ObjectMapper map = mapper.copy();
		map.registerModule(module);

		return new JsonSerializer(map);
	}

	@Override
	public SerializationFormat getType() {
		return new SerializationFormat("JSON", "");
	}

	public Serializer copy() {
		return new JsonSerializer(mapper.copy());
	}

	@Override
	public <T, S> T deserialize(byte[] data, Class<T> cls, ServiceLocator locator) throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		return deserialize(in, cls, locator);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T, S> T deserialize(InputStream in, Class<T> cls, ServiceLocator locator) throws Exception {
		if (cls.equals(ServiceRequest.class)) {
			return (T) parseServiceRequest(in, locator);
		} else if (cls.equals(ExecuteRequestResponse.class)) {
			return (T) parseExecuteRequestResponse(in, locator);
		} else {
			throw new Error(
					"This function can only be used for ServiceRequest and  ExecuteRequestResponse but received: "
							+ cls);
		}
	}

	@SuppressWarnings("unchecked")
	private ExecuteRequestResponse<?> parseExecuteRequestResponse(InputStream in, ServiceLocator locator)
			throws JsonParseException, IOException {
		JsonParser parser = mapper.getFactory().createParser(in);
		ExecuteRequestResponse<?> response = new ExecuteRequestResponse<>();

		JsonToken token = null;
		String fieldName = null;
		while ((token = parser.nextToken()) != null) {
			if (token == JsonToken.FIELD_NAME) {
				fieldName = parser.getValueAsString();
			} else if (fieldName != null) {
				if (fieldName.equals("status")) {
					int status = parser.getIntValue();
					response.setStatus(status);
				} else if (fieldName.equals("content")) {
					@SuppressWarnings("rawtypes")
					ServiceResponse res = new ServiceResponse<>();
					String fieldName2 = null;
					while ((token = parser.nextToken()) != JsonToken.END_OBJECT) {
						if (token == JsonToken.FIELD_NAME) {
							fieldName2 = parser.getValueAsString();
						} else if (fieldName2 != null) {
							if (fieldName2.equals("rid")) {
								long rid = parser.getLongValue();
								res.setRequestId(rid);
							} else if (fieldName2.equals("service")) {
								ServiceInfo<?> service = parser.readValueAs(ServiceInfo.class);
								res.setService(service);
							} else if (fieldName2.equals("content")) {
								Object obj = parser
										.readValueAs(locator.locate(res.getService().getId()).getResultClspath());
								res.setObject(obj);
							}
							fieldName2 = null;
						}

					}
					response.setContent(res);
				} else {
					throw new Error("Unkown Field");
				}
				fieldName = null;
			}

		}

		return response;

	}

	private ServiceRequest parseServiceRequest(InputStream in, ServiceLocator locator)
			throws JsonParseException, IOException {
		JsonParser parser = mapper.getFactory().createParser(in);
		ServiceRequest request = new ServiceRequest();

		JsonToken token = null;
		String fieldName = null;
		while ((token = parser.nextToken()) != null) {
			if (token == JsonToken.FIELD_NAME) {
				fieldName = parser.getValueAsString();
			} else if (fieldName != null) {
				if (fieldName.equals("service")) {
					ServiceInfo<?> service = parser.readValueAs(ServiceInfo.class);
					request.setService(service);
				} else if (fieldName.equals("env")) {
					EnvironmentInfo env = parser.readValueAs(EnvironmentInfo.class);
					request.setEnv(env);
				} else if (fieldName.equals("rid")) {
					long rid = parser.getLongValue();
					request.setRequestId(rid);
				} else if (fieldName.equals("args")) {
					int idx = 0;
					List<Object> args = new ArrayList<Object>();
					while ((token = parser.nextToken()) != JsonToken.END_ARRAY) {
						args.add(
								parser.readValueAs(locator.locate(request.getService().getId()).getArgsClspath()[idx]));
						idx++;
					}
					request.setArgs(args.toArray());
				} else {
					throw new Error("Unkown Field");
				}
				fieldName = null;
			}

		}

		return request;

	}

}
