package test.java.stub;

import org.junit.AfterClass;
import org.junit.BeforeClass;
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

import java.util.ArrayList;
import java.util.List;

public class BigDataServiceTest {
	/**
	 * pointer to a server stub
	 */
	private static ServerStub serverStub;

	/**
	 * 
	 */
	private static ServiceProviderInfo spInfo;

	private static final int serverPort = 4040;

	protected static ClientStub clientStub;

	private static ServiceInfo<Integer> service;

	@BeforeClass
	public static void initStubs() throws Exception {
		// setting service provider info
		spInfo = new ServiceProviderInfo("127.0.0.1", serverPort, EnvironmentInfo.currentEnvInfo());

		// setting service
		service = new ServiceInfo<Integer>("data", 1);

		List<Serializer> list = new ArrayList<Serializer>();
		list.add(new JsonSerializer());

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
					double[][] data = (double[][]) request.getArgs()[0];
					return new ServiceResponse<>(request.getService(), new Long(data.length * data[0].length),
							request.getRequestId());
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

	public void testExecute() throws Exception {

		double[][] data = generateNumericData(1000, 100000, 100000000, 1000000000, false);
		JsonSerializer ser = new JsonSerializer();
		byte[] bytes = ser.serialize(data, double[][].class);

		// ServiceResponse<Integer> response = clientStub.call(service, spInfo,
		// new Integer[] { 4, 5 },
		// new JsonSerializer());
		//
		// // checking service information
		// assertEquals(service, response.getService());
		// //
		// // checking class of result
		// assertEquals(Integer.class, response.getContent().getClass());
		//
		// // value of results to be equal
		// assertEquals(new Boolean(true), response.getContent());
	}

	@AfterClass
	public static void closeAfterTests() {
		try {
			serverStub.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static double[][] generateNumericData(int width, int length, double minRange, double maxRange,
			boolean round) {
		double span = maxRange - minRange;
		double[][] data = new double[width][length];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < length; j++) {
				data[i][j] = Math.random() * span + minRange;
				if (round) {
					data[i][j] = data[i][j] * 100 / 100;
				}
			}
		}
		return data;
	}

}
