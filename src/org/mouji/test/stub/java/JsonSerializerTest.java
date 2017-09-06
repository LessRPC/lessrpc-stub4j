package org.mouji.test.stub.java;

import org.mouji.common.info.SerializationFormat;
import org.mouji.common.info.SerializedObject;
import org.mouji.common.info.ServiceInfo;
import org.mouji.common.info.ServiceProviderInfo;
import org.mouji.common.info.ServiceSupportInfo;
import org.mouji.common.info.StubEnvInfo;
import org.mouji.common.info.responses.ExecuteRequestResponse;
import org.mouji.common.info.responses.IntegerResponse;
import org.mouji.common.info.responses.ProviderInfoResponse;
import org.mouji.common.info.responses.ServiceResponse;
import org.mouji.common.info.responses.ServiceSupportResponse;
import org.mouji.common.info.responses.TextResponse;
import org.mouji.common.serializer.Serializer;
import org.mouji.stub.java.JsonSerializer;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

public class JsonSerializerTest {

	private static ServiceProviderInfo spInfo;

	private static ServiceInfo<Integer> service;

	private static int serverPort = 4040;

	@BeforeClass
	public static void init() throws Exception {
		// setting service provider info
		spInfo = new ServiceProviderInfo("127.0.0.1", serverPort, StubEnvInfo.currentEnvInfo());

		// setting service
		service = new ServiceInfo<Integer>("add", 1);
	}

	@Test
	public void testSeviceProviderInfoResponse() {

		ProviderInfoResponse response = new ProviderInfoResponse(200,
				new ServiceProviderInfo("127.0.0.1", 4040, StubEnvInfo.currentEnvInfo()));

		Serializer serializer = new JsonSerializer();
		ProviderInfoResponse response2 = null;
		try {
			response2 = serializer.deserialize(serializer.serialize(response, ProviderInfoResponse.class),
					ProviderInfoResponse.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals(response.getStatus(), response2.getStatus());

		assertEquals(response.getContent(), response2.getContent());
	}

	@Test
	public void testSupportResponse() {

		ServiceSupportResponse response = new ServiceSupportResponse(200, new ServiceSupportInfo(service, spInfo,
				new SerializationFormat[] { SerializationFormat.defaultFotmat() }));

		Serializer serializer = new JsonSerializer();
		ServiceSupportResponse response2 = null;
		try {
			response2 = serializer.deserialize(serializer.serialize(response, ServiceSupportResponse.class),
					ServiceSupportResponse.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals(response.getStatus(), response2.getStatus());

		assertEquals(response.getContent(), response2.getContent());
	}

	@Test
	public void testIntegerResponse() {

		IntegerResponse response = new IntegerResponse(200, 1);

		Serializer serializer = new JsonSerializer();
		IntegerResponse response2 = null;
		try {
			response2 = serializer.deserialize(serializer.serialize(response, IntegerResponse.class),
					IntegerResponse.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals(response.getStatus(), response2.getStatus());

		assertEquals(response.getContent(), response2.getContent());
	}

	@Test
	public void testTextResponse() {

		TextResponse response = new TextResponse(200, "test");

		Serializer serializer = new JsonSerializer();
		TextResponse response2 = null;
		try {
			response2 = serializer.deserialize(serializer.serialize(response, TextResponse.class), TextResponse.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals(response.getStatus(), response2.getStatus());

		assertEquals(response.getContent(), response2.getContent());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecuteResponse() {

		ExecuteRequestResponse<Integer> response = new ExecuteRequestResponse<Integer>(200,
				new ServiceResponse<>(service, new SerializedObject<Integer>(new Integer(4)), 10));

		Serializer serializer = new JsonSerializer();
		ExecuteRequestResponse<Integer> response2 = null;
		try {
			response2 = serializer.deserialize(serializer.serialize(response, ExecuteRequestResponse.class),
					ExecuteRequestResponse.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals(response.getStatus(), response2.getStatus());

		assertEquals(response.getContent(), response2.getContent());
	}

}
