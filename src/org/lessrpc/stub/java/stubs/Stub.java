package org.lessrpc.stub.java.stubs;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.lessrpc.stub.java.serializer.JsonSerializer;
import org.lessrpc.common.info.SerializationFormat;
import org.lessrpc.common.serializer.Serializer;

/**
 * Basic stub class that provides common functionalities between server stub and
 * client stub
 * 
 * @author Salim
 *
 */
public class Stub {

	/**
	 * list of supported serializers. Default is included in constructor
	 */
	private final List<Serializer> serializers;

	/**
	 * map of format to serializer instance
	 */
	protected HashMap<SerializationFormat, Serializer> serializerMap;



	public Stub(List<Serializer> serializers) {
		boolean hasJson = false;

		this.serializers = serializers;

		// Initializing serializer map
		this.serializerMap = new HashMap<SerializationFormat, Serializer>();
		for (Serializer serializer : serializers) {
			serializerMap.put(serializer.getType(), serializer);
			if (serializer.getType().getName().equals("json")) {
				hasJson = true;
			}
		}

		if (!hasJson) {
			JsonSerializer js = new JsonSerializer();
			this.getSerializers().add(js);
			serializerMap.put(js.getType(), js);
		}
	}

	/**
	 * return the serializer instance given the SerializationFormat appropriate.
	 * It will return null in absence of a serializer with the given format
	 * 
	 * @param format
	 * @return
	 */
	protected Serializer getSerializer(SerializationFormat format) {
		return serializerMap.get(format);
	}

	/**
	 * return http format string of accepted types
	 * 
	 * @return
	 */
	protected String getAcceptedTypes() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < getSerializers().size(); i++) {
			sb.append(getSerializers().get(i).getType().httpFormat());
			if (i != getSerializers().size() - 1) {
				sb.append(" , ");
			}
		}
		return sb.toString();
	}

	/**
	 * generates a new random id
	 * 
	 * @return
	 */
	public long genRandomId() {
		Random rand = new Random();
		rand.setSeed(System.currentTimeMillis());
		return rand.nextLong();
	}

	public List<Serializer> getSerializers() {
		return serializers;
	}

}
