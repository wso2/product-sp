package org.wso2.carbon.bam.kpiagent;

import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import javax.security.sasl.AuthenticationException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Random;
import java.lang.String;

public class KPIAgent {
    private static Logger logger = Logger.getLogger(KPIAgent.class);
    public static final String PHONE_RETAIL_STREAM = "org.wso2.bam.phone.retail.store.kpi";
    public static final String VERSION = "1.0.0";

    public static final String[] phoneModels = {"Nokia", "Apple", "Samsung", "Sony-Ericson", "LG"};
    public static final String[] users = {"James", "Mary", "John", "Peter", "Harry", "Tom", "Paul"};
    public static final int[] quantity = {2, 5, 3, 4, 1};
    public static final int[] price = {50000, 55000, 90000, 80000, 70000};


    public static void main(String[] args) throws AgentException,
                                                  MalformedStreamDefinitionException,
                                                  StreamDefinitionException,
                                                  DifferentStreamDefinitionAlreadyDefinedException,
                                                  MalformedURLException,
                                                  AuthenticationException,
                                                  NoStreamDefinitionExistException,
                                                  TransportException, SocketException,
                                                  org.wso2.carbon.databridge.commons.exception.AuthenticationException {
        System.out.println("Starting BAM Phone Reatil Shop KPI Agent");
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

        //create data publisher

        DataPublisher dataPublisher = new DataPublisher(url, username, password, agent);
        String streamId = null;

        try {
            streamId = dataPublisher.findStream(PHONE_RETAIL_STREAM, VERSION);
            System.out.println("Stream already defined");

        } catch (NoStreamDefinitionExistException e) {
            streamId = dataPublisher.defineStream("{" +
                                                  "  'name':'" + PHONE_RETAIL_STREAM + "'," +
                                                  "  'version':'" + VERSION + "'," +
                                                  "  'nickName': 'Phone_Retail_Shop'," +
                                                  "  'description': 'Phone Sales'," +
                                                  "  'metaData':[" +
                                                  "          {'name':'clientType','type':'STRING'}" +
                                                  "  ]," +
                                                  "  'payloadData':[" +
                                                  "          {'name':'brand','type':'STRING'}," +
                                                  "          {'name':'quantity','type':'INT'}," +
                                                  "          {'name':'total','type':'INT'}," +
                                                  "          {'name':'user','type':'STRING'}" +
                                                  "  ]" +
                                                  "}");
//            //Define event stream
        }


        //Publish event for a valid stream
        if (!streamId.isEmpty()) {
            System.out.println("Stream ID: " + streamId);

            for (int i = 0; i < 100; i++) {
                publishEvents(dataPublisher, streamId, i);
                System.out.println("Events published : " + (i + 1));
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }

            dataPublisher.stop();
        }
    }

    private static void publishEvents(DataPublisher dataPublisher, String streamId, int i) throws AgentException {
        int quantity = getRandomQuantity();
        Event eventOne = new Event(streamId, System.currentTimeMillis(), new Object[]{"external"}, null,
                new Object[]{getRandomProduct(), quantity, quantity*getRandomPrice(), getRandomUser()});
        dataPublisher.publish(eventOne);
    }

    private static String getRandomProduct() {
        return phoneModels[getRandomId(5)];
    }

    private static String getRandomUser() {
        return users[getRandomId(7)];
    }

    private static int getRandomQuantity() {
        return quantity[getRandomId(5)];
    }

    private static int getRandomPrice() {
        return price[getRandomId(5)];
    }


    private static int getRandomId(int i) {
        Random randomGenerator = new Random();
        return randomGenerator.nextInt(i);
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
