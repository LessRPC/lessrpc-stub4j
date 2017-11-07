package org.mouji.stub.java.serializer;

import java.io.InputStream;
import java.io.OutputStream;

import org.mouji.common.info.SerializationFormat;
import org.mouji.common.serializer.Serializer;

import com.fasterxml.jackson.core.JsonFactory;
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
		return mapper.writeValueAsBytes( obj);
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
}
