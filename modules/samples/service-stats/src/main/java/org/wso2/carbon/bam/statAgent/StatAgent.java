package org.wso2.carbon.bam.statAgent;

import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.*;

import javax.security.sasl.AuthenticationException;
import java.net.*;
import java.util.*;
import java.lang.String;

public class StatAgent {
    public static final String STREAM_NAME1 = "bam_service_data_publisher";
    public static final String VERSION1 = "1.0.0";

    public static String[] hosts = {"esb.foo.org", "dss.foo.org", "as.it.foo.org", "as.mkt.foo.com",
                                    "as.foo.org"};

    public static String[] remoteAddresses = {"94.167.250.236", "34.33.134.212", "237.120.89.29",
                                              "103.216.158.196", "81.225.246.119", "205.42.95.109",
                                              "100.103.41.21", "93.41.15.186", "207.10.167.241",
                                              "43.46.199.25"};

    public static long[] responseTimes = {19, 1, 2, 31, 4, 10, 3, 1, 144, 600};


    public static Long[] timestamps;
//    = { 1317546120000L, 1317546120000L, 1317546360000L, 1317546240000L, 1317546600000L,
//                                        1317547380000L, 1317949320000L, 1317954180000L, 1317956040000L, 1318039980000L,
//                                        1318634640000L, 1318625520000L, 1318652580000L, 1319858460000L, 1319892780000L,
//                                        1320228120000L, 1320753720000L, 1321405320000L, 1322618520000L, 1322816520000L,
//                                        1322816520000L, 1322816760000L, 1322816640000L, 1322817000000L, 1322817780000L,
//                                        1325527320000L, 1325797320000L, 1326359160000L, 1326596640000L, 1326633000000L,
//                                        1326590580000L, 1329051840000L, 1330002600000L, 1329960180000L };

    public static Map<String, List<String>> services = new HashMap<String, List<String>>();

    private static int genRandNumber(int max, int min) {
        Random rand = new Random();
        return (rand.nextInt(max - min + 1) + min);
    }

       static {
//    private static long[] generateTimeStamps() {

        long currentTimeStamp = System.currentTimeMillis();

        List<Long> timeStampList = new ArrayList<Long>();

        for (int i = 0; i < 10; i++) {
            timeStampList.add(currentTimeStamp - 1000 * (genRandNumber(3600, 0)));
            timeStampList.add(currentTimeStamp - 1000 * genRandNumber(86400 , 3600));
            timeStampList.add(currentTimeStamp - 1000 * 86400 * genRandNumber(30 , 1));
            timeStampList.add(currentTimeStamp - 1000 * genRandNumber(12 * 30 * 86400  , 30 * 86400 ));
            timeStampList.add(currentTimeStamp - 1000 * genRandNumber(12 * 30 * 86400 , 12 * 30 * 86400  ));
        }

           timestamps = timeStampList.toArray(new Long[timeStampList.size()]);

//        //
//        DateFormat dateFormat = new SimpleDateFormat();
//        try {
//            Date date = dateFormat.parse("" + System.currentTimeMillis());
//            Calendar calendar = new GregorianCalendar();
//            calendar.setTime(date);
//
//
//            for (int i = 0; i < 7; i++) {
//                Calendar tmpCalendar = (Calendar) calendar.clone();
//                // add hours
//                timeStampList.add(new Timestamp(calendar.get(Calendar.MONTH)));
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
    }

    static {



        List<String> operations = new ArrayList<String>();
        operations.add("creditAmount");
        operations.add("checkCredit");

        services.put("creditService", operations);

        operations.clear();
        operations.add("shipOrder");

        services.put("shippingService", operations);

        operations.clear();
        operations.add("validateOrder");
        operations.add("validateShipping");

        services.put("validationService", operations);

        operations.clear();
        operations.add("checkoutOrder");

        services.put("checkoutService", operations);

        operations.clear();
        operations.add("getCustomerInfo");
        operations.add("getCustomers");
        operations.add("addCustomer");
        operations.add("editCustomerInfo");
        operations.add("removeCustomer");

        services.put("customerInfoService", operations);

    }

    public static void main(String[] args)
            throws AgentException, MalformedStreamDefinitionException,
                   StreamDefinitionException, DifferentStreamDefinitionAlreadyDefinedException,
                   MalformedURLException,
                   AuthenticationException, NoStreamDefinitionExistException,
                   org.wso2.carbon.databridge.commons.exception.AuthenticationException,
                   TransportException, SocketException {
        System.out.println("Starting BAM Statistics Agent");
        AgentConfiguration agentConfiguration = new AgentConfiguration();
        String currentDir = System.getProperty("user.dir");
        System.setProperty("javax.net.ssl.trustStore", currentDir + "/src/main/resources/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        Agent agent = new Agent(agentConfiguration);
        String host;
        //host = InetAddress.getLocalHost().getHostAddress();
        if (getLocalAddress() != null) {
           host = getLocalAddress().getHostAddress();
        } else {
           host = "localhost"; // Defaults to localhost
        }

        String url = getProperty("url", "tcp://" + host + ":" + "7611");
        String username = getProperty("username", "admin");
        String password = getProperty("password", "admin");

        //create data publisher
        DataPublisher dataPublisher = new DataPublisher(url, username, password, agent);

        String streamId1 = null;


        try {
            streamId1 = dataPublisher.findStream(STREAM_NAME1, VERSION1);
            System.out.println("Stream already defined");

        } catch (NoStreamDefinitionExistException e) {
            streamId1 = dataPublisher.defineStream("{" +
                                                        "  'name':'" + STREAM_NAME1 + "'," +
                                                        "  'version':'" + VERSION1 + "'," +
                                                        "  'nickName': 'Statistics'," +
                                                        "  'description': 'Service statistics'," +
                                                        "  'metaData':[" +
                                                        "          {'name':'request_url','type':'STRING'}," +
                                                        "          {'name':'remote_address','type':'STRING'}," +
                                                        "          {'name':'content_type','type':'STRING'}," +
                                                        "          {'name':'user_agent','type':'STRING'}," +
                                                        "          {'name':'host','type':'STRING'}," +
                                                        "          {'name':'referer','type':'STRING'}" +
                                                        "  ]," +
                                                        "  'payloadData':[" +
                                                        "          {'name':'service_name','type':'STRING'}," +
                                                        "          {'name':'operation_name','type':'STRING'}," +
                                                        "          {'name':'timestamp','type':'LONG'}," +
                                                        "          {'name':'response_time','type':'LONG'}," +
                                                        "          {'name':'request_count','type':'INT'}," +
                                                        "          {'name':'response_count','type':'INT'}," +
                                                        "          {'name':'fault_count','type':'INT'}" +
//                                                        "  ]," +
//                                                        "  'correlationData':[" +
//                                                        "          {'name':'bam_activity_id','type':'STRING'}" +
                                                        "  ]" +
                                                        "}");
        }

        //Publish event for a valid stream
        if (!streamId1.isEmpty()) {
            System.out.println("Stream ID: " + streamId1);

            for (int i = 0; i < 100; i++) {
                publishEvents(dataPublisher, streamId1);
                System.out.println("Events published : " + (i + 1) * 9);
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }

            dataPublisher.stop();
        }
    }

    private static void publishEvents(DataPublisher dataPublisher, String streamId)
            throws AgentException {

        Random rand = new Random();

        for (int i = 0; i < 3; i++) {
            int hostIndex = rand.nextInt(5);

            String host = hosts[hostIndex];

            for (int j = 0; j < 3; j++) {
                int serviceIndex = rand.nextInt(4);

                Iterator<String> serviceIterator = services.keySet().iterator();

                int k = 0;
                String service = null;
                while (serviceIterator.hasNext() && k < serviceIndex) {
                    service = serviceIterator.next();
                }

                if (service == null) {
                    service = serviceIterator.next();
                }

                List<String> operations = services.get(service);

                int operationIndex = rand.nextInt(operations.size());

                String operation = operations.get(operationIndex);

                Object[] meta = new Object[] {
                        "http://" + host + "/services/" + service,
                        remoteAddresses[rand.nextInt(10)],
                        "application/xml",
                        "http-components/client",
                        host,
                        "http://example.org"
                };

                int response = rand.nextInt(2);
                int fault = 0;

                if (response == 0) {
                    fault = 1;
                }

                Object[] payload = new Object[] {
                        service,
                        operation,
                        timestamps[rand.nextInt(34)], // Unix timeStamp
                        responseTimes[rand.nextInt(10)],
                        1,
                        response,
                        fault
                };

                Object[] correlation = null;
//                        new Object[] {
//                        UUID.randomUUID().toString()
//                };

                Event statisticsEvent = new Event(streamId, System.currentTimeMillis(),
                                                  meta, correlation,payload);
                dataPublisher.publish(statisticsEvent);
            }
        }
    }

    public static InetAddress getLocalAddress() throws SocketException {
        Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
        while (ifaces.hasMoreElements()) {
            NetworkInterface iface = ifaces.nextElement();
            Enumeration<InetAddress> addresses = iface.getInetAddresses();

            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                    return addr;
                }
            }
        }

        return null;
    }

    private static String getProperty(String name, String def) {
        String result = System.getProperty(name);
        if (result == null || result.length() == 0 || result == "") {
            result = def;
        }
        return result;
    }
}

