package test.java.stub;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.lessrpc.stub.java.serializer.JsonSerializer;
import org.lessrpc.stub.java.stubs.ClientStub;
import org.lessrpc.stub.java.stubs.ServerStub;
import org.lessrpc.common.errors.ApplicationSpecificErrorException;
import org.lessrpc.common.errors.ExecuteInternalError;
import org.lessrpc.common.errors.InvalidArgsException;
import org.lessrpc.common.errors.ServiceNotSupportedException;
import org.lessrpc.common.info.EnvironmentInfo;
import org.lessrpc.common.info.SerializationFormat;
import org.lessrpc.common.info.ServiceDescription;
import org.lessrpc.common.info.ServiceInfo;
import org.lessrpc.common.info.ServiceProviderInfo;
import org.lessrpc.common.info.ServiceRequest;
import org.lessrpc.common.info.ServiceSupportInfo;
import org.lessrpc.common.info.responses.ServiceResponse;
import org.lessrpc.common.serializer.Serializer;
import org.lessrpc.common.services.ServiceProvider;
import org.lessrpc.serialize.msgpack.MsgPackSerializer;

public class SimpleStubsRunningTest {
	/**
	 * pointer to a server stub
	 */
	private static ServerStub serverStub;

	/**
	 * 
	 */
	private static ServiceProviderInfo spInfo;

	private static final int serverPort = 4040;

	private static ClientStub clientStub;

	private static ServiceInfo<Integer> service;
	private static ServiceDescription<Integer> desc;

	@BeforeClass
	public static void initStubs() throws Exception {
		// setting service provider info
		spInfo = new ServiceProviderInfo("127.0.0.1", serverPort, EnvironmentInfo.currentEnvInfo());

		// setting service
		service = new ServiceInfo<Integer>("add", 1);

		desc = new ServiceDescription<>(service, new Class[] { Integer.class, Integer.class }, Integer.class);

		List<Serializer> list = new ArrayList<Serializer>();
		list.add(new JsonSerializer());
		list.add(new MsgPackSerializer());

		serverStub = new ServerStub(serverPort, list);

		ServiceProvider serviceProvider = new ServiceProvider() {

			@Override
			public ServiceSupportInfo service(ServiceInfo<?> info) throws ServiceNotSupportedException {
				return new ServiceSupportInfo(info, spInfo,
						new SerializationFormat[] { SerializationFormat.defaultFotmat() });
			}

			@Override
			public boolean ping() {
				return true;
			}

			@Override
			public ServiceProviderInfo info() {
				return spInfo;
			}

			@Override
			public ServiceResponse<?> execute(ServiceRequest request) throws ApplicationSpecificErrorException,
					ExecuteInternalError, InvalidArgsException, ServiceNotSupportedException {
				if (request.getService().getId() == 1) {
					if (!(request.getArgs()[0] instanceof Integer) || !(request.getArgs()[1] instanceof Integer)) {
						throw new InvalidArgsException("Both must be integers!!");
					}
					Integer num1 = (Integer) request.getArgs()[0];
					Integer num2 = (Integer) request.getArgs()[1];
					return new ServiceResponse<>(request.getService(), num1 + num2, request.getRequestId());
				} else {
					throw new ServiceNotSupportedException(request.getService());
				}
			}

			@Override
			public List<ServiceSupportInfo> listSupport() {
				List<ServiceSupportInfo> list = new ArrayList<ServiceSupportInfo>();
				list.add(new ServiceSupportInfo(service, spInfo,
						new SerializationFormat[] { SerializationFormat.defaultFotmat() }));
				return list;
			}

			@SuppressWarnings("rawtypes")
			@Override
			public List<ServiceDescription> listServices() {
				List<ServiceDescription> list = new ArrayList<ServiceDescription>();
				list.add(
						new ServiceDescription<>(service, new Class[] { Integer.class, Integer.class }, Integer.class));
				return list;
			}
		};
		ServiceProvider provider = serviceProvider;
		serverStub.init(provider);

		serverStub.start();

		clientStub = new ClientStub(list);

	}

	@Test
	public void testPing() throws Exception {
		boolean ping = clientStub.ping(spInfo);
		assertTrue(ping);
	}

	@Test
	public void testInfo() throws Exception {
		ServiceProviderInfo provider = clientStub.getInfo(spInfo.getURL(), spInfo.getPort());
		assertEquals(spInfo, provider);
	}

	@Test
	public void testService() throws Exception {
		ServiceSupportInfo support = clientStub.getServiceSupport(spInfo, service);
		// checking service information
		assertEquals(service, support.getService());
		// // checking number of formats
		assertEquals(support.getSerializers().length, 1);
		// // checking format information
		assertEquals(support.getSerializers()[0], SerializationFormat.defaultFotmat());
	}

	@Test
	public void testExecute() throws Exception {
		ServiceResponse<Integer> response = clientStub.call(desc, spInfo, new Integer[] { 4, 5 }, new JsonSerializer());

		// checking service information
		assertEquals(service, response.getService());
		//
		// checking class of result
		assertEquals(Integer.class, response.getContent().getClass());

		// value of results to be equal
		assertEquals(new Integer(9), response.getContent());
	}

	@Test
	public void testExecuteSpecifyFormat() throws Exception {
		ServiceResponse<Integer> response = clientStub.call(desc, spInfo, new Integer[] { 4, 5 }, new MsgPackSerializer(),
				new SerializationFormat[] { new MsgPackSerializer().getType() });

		// checking service information
		assertEquals(service, response.getService());
		//
		// checking class of result
		assertEquals(Integer.class, response.getContent().getClass());

		// value of results to be equal
		assertEquals(new Integer(9), response.getContent());
	}

	@AfterClass
	public static void closeAfterTests() {
		try {
			serverStub.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
