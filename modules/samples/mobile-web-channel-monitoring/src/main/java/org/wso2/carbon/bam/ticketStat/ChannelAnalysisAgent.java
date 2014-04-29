package org.wso2.carbon.bam.ticketStat;

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

public class ChannelAnalysisAgent {
    public static final String STREAM_NAME1 = "org.wso2.ticket.service";
    public static final String VERSION1 = "1.0.0";

    public static long[] responseTimes = {19, 1, 2, 31, 4, 10, 3, 1, 144, 600};
    public static List<String> operations = new ArrayList<String>();

    public static Long[] timestamps;

    public static String[] clientTypes = new String[]{"web_channel", "mobile_channel"};


    private static int genRandNumber(int max, int min) {
        Random rand = new Random();
        return (rand.nextInt(max - min + 1) + min);
    }

    static {

        long currentTimeStamp = System.currentTimeMillis();

        List<Long> timeStampList = new ArrayList<Long>();

        for (int i = 0; i < 10; i++) {
            timeStampList.add(currentTimeStamp - 1000 * (genRandNumber(3600, 0)));
            timeStampList.add(currentTimeStamp - 1000 * genRandNumber(86400, 3600));
            timeStampList.add(currentTimeStamp - 1000 * 86400 * genRandNumber(30, 1));
            timeStampList.add(currentTimeStamp - 1000 * genRandNumber(12 * 30 * 86400, 30 * 86400));
            timeStampList.add(currentTimeStamp - 1000 * genRandNumber(12 * 30 * 86400, 12 * 30 * 86400));
        }

        timestamps = timeStampList.toArray(new Long[timeStampList.size()]);
    }

    static {
        operations.add("order");
        operations.add("view");
        operations.add("cancel");

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
        if (getLocalAddress() != null) {
            host = getLocalAddress().getHostAddress();
        } else {
            host = "localhost"; // Defaults to localhost
        }

        String url = getProperty("url", "tcp://" + host + ":" + "7611");
        String username = getProperty("username", "admin");
        String password = getProperty("password", "admin");

        DataPublisher dataPublisher = new DataPublisher(url, username, password, agent);

        String streamId1 = null;


        try {
            streamId1 = dataPublisher.findStream(STREAM_NAME1, VERSION1);
            System.out.println("Stream already defined");

        } catch (NoStreamDefinitionExistException e) {
            streamId1 = dataPublisher.defineStream("{" +
                    "  'name':'"+STREAM_NAME1+"',  " +
                    "  'version':'"+VERSION1+"',                            " +
                    "  'nickName': 'Ticket_Service',               " +
                    "  'description': 'Ticket Booking Service',  " +
                    "  'metaData':[                            " +
                    "         {'name':'clientType','type':'STRING'}  " +
                    "  ],                      " +
                    "  'payloadData':[         " +
                    "         {'name':'operation','type':'STRING'}," +
                    "          {'name':'timestamp','type':'LONG'}" +
                    "  ]" +
                    "}"
            );
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


            for (int j = 0; j < 5; j++) {

                int operationIndex = rand.nextInt(3);

                String operation = operations.get(operationIndex);
                 int clientType = rand.nextInt(2);
                Object[] meta = new Object[]{
                    clientTypes[clientType]
                };

                int response = rand.nextInt(2);
                int fault = 0;

                if (response == 0) {
                    fault = 1;
                }

                Object[] payload = new Object[]{
                        operation,
                        timestamps[rand.nextInt(34)], // Unix timeStamp
                };

                Object[] correlation = null;

                Event ticketEvent = new Event(streamId, System.currentTimeMillis(),
                        meta, correlation, payload);
                dataPublisher.publish(ticketEvent);
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

