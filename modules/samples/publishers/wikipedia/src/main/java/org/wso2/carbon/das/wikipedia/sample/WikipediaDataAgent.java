/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.das.wikipedia.sample;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

/**
 * This class represents a data agent that will publish wikipedia articles from a full wikipedia dump, as events.
 */
public class WikipediaDataAgent {
    
    private static final String WIKIPEDIA_DATA_STREAM = "org.wso2.das.sample.wikipedia.data";
    
    private static final String NS = "http://www.mediawiki.org/xml/export-0.10/";
    
    private static final String VERSION = "1.0.0";
    
    private static final int defaultThriftPort = 7611;
    
    private static final int defaultBinaryPort = 9611;
    
    private static final SimpleDateFormat tsFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static void main(String[] args) throws Exception {
        System.out.println("Starting DAS Wikipedia Data Agent");
        String currentDir = System.getProperty("user.dir");
        System.setProperty("javax.net.ssl.trustStore", currentDir + "/src/main/resources/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

        AgentHolder.setConfigPath(getDataAgentConfigPath());
        String host = getLocalAddress().getHostAddress();

        String type = getProperty("type", "Thrift");
        int receiverPort = defaultThriftPort;
        if (type.equals("Binary")) {
            receiverPort = defaultBinaryPort;
        }
        int securePort = receiverPort + 100;

        String url = getProperty("url", "tcp://" + host + ":" + receiverPort);
        String authURL = getProperty("authURL", "ssl://" + host + ":" + securePort);
        String username = getProperty("username", "admin");
        String password = getProperty("password", "admin");

        String path;
        if (args.length == 0 || args[0] == null || args[0].isEmpty()) {
            System.out.println("Usage: WikipediaDataAgent <path> [count]");
            return;
        } else {
            path = args[0];
        }
        
        long count;
        if (args.length < 2 || args[1] == null || args[1].isEmpty()) {
            count = Long.MAX_VALUE;
        } else {
            count = Integer.parseInt(args[1]);
            if (count == -1) {
                count = Long.MAX_VALUE;
            }
        }

        DataPublisher dataPublisher = new DataPublisher(type, url, authURL, username, password);
        String streamId = DataBridgeCommonsUtils.generateStreamId(WIKIPEDIA_DATA_STREAM, VERSION);
        publishEvents(dataPublisher, streamId, path, count);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignore) { /* ignore */ }
        dataPublisher.shutdown();
    }

    public static String getDataAgentConfigPath() {
        File filePath = new File("src" + File.separator + "main" + File.separator + "resources");
        if (!filePath.exists()) {
            filePath = new File("test" + File.separator + "resources");
        }
        if (!filePath.exists()) {
            filePath = new File("resources");
        }
        if (!filePath.exists()) {
            filePath = new File("test" + File.separator + "resources");
        }
        return filePath.getAbsolutePath() + File.separator + "data-agent-conf.xml";
    }

    public static InetAddress getLocalAddress() throws SocketException, UnknownHostException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                    return addr;
                }
            }
        }
        return InetAddress.getLocalHost();
    }

    private static String getProperty(String name, String def) {
        String result = System.getProperty(name);
        if (result == null || result.length() == 0 || result.equals("")) {
            result = def;
        }
        return result;
    }
    
    private static void publishEvents(DataPublisher dataPublisher, 
            String streamId, String path, 
            long count) throws Exception {
        long prevDataCount = 0;
        final AtomicLong dataCount = new AtomicLong();
        File file = new File(path);
        long fileSize = file.length();
        int progress = 0, tmpProgess;
        InputStream in = new FileInputStream(file) {
            @Override
            public int read() throws IOException {
                int b = super.read();
                dataCount.incrementAndGet();
                return b;
            }
            @Override
            public int read(byte[] b) throws IOException {
                int i = super.read(b);
                dataCount.addAndGet(i);
                return i;
            }
            @Override
            public int read(byte[] b, int offset, int length) throws IOException {
                int i = super.read(b, offset, length);
                dataCount.addAndGet(i);
                return i;
            }
        };
        XMLStreamReader reader = OMXMLBuilderFactory.createOMBuilder(in).getDocument().getXMLStreamReader(false);
        long i = 0;
        long start = System.currentTimeMillis();
        OMElement page;
        while (reader.hasNext()) {
            /* get to the start of pages */
            if (reader.getEventType() == XMLStreamReader.START_ELEMENT &&
                    reader.getName().equals(new QName(NS, "mediawiki"))) {
                break;
            }
            reader.next();
        }
        long tpsStartTS = System.currentTimeMillis();
        long j = 0;
        while (reader.hasNext() && i < count) {
            try {
                page = OMXMLBuilderFactory.createStAXOMBuilder(reader).getDocumentElement();
            } catch (Exception e) {
                /* a hack to detect the end of the document */
                break;
            }
            page.build();
            if (!page.getQName().getLocalPart().equals("page")) {
                continue;
            }
            Object[] payload = createPayload(page);
            Event event = new Event(streamId, System.currentTimeMillis(), null, null, payload);
            dataPublisher.publish(event);
            i++;
            tmpProgess = (int) (dataCount.get() / (double) fileSize * 100.0);
            if (tmpProgess > progress) {
                progress = tmpProgess;
                long tpsEndTS = System.currentTimeMillis();
                double tps = (i - j) / (double) (tpsEndTS - tpsStartTS) * 1000.0;
                j = i;
                double dataRate = (dataCount.get() - prevDataCount) / ((1024.0 * 1024.0) * (tpsEndTS - tpsStartTS)) * 1000.0;
                prevDataCount = dataCount.get();
                tpsStartTS = tpsEndTS;
                System.out.println("[" + (new Date()).toString() + "] " + progress + "% -> " + 
                    (int) (dataCount.get() / (double) (1024 * 1024)) + " MB, Count: " + i + ", TPS: " + tps + 
                    ", Data Rate: " + dataRate + " MB/s");
            }
        }
        long end = System.currentTimeMillis();
        in.close();
        System.out.println("Published " + i + " Wikipedia page(s) as events in " + (end - start) / 1000.0 + 
                " seconds, TPS: " + (i / (double) (end - start) * 1000.0) + ".");
    }
    
    private static long timestamp(String tsStr) throws ParseException {
        return tsFormat.parse(tsStr).getTime();
    }
    
    private static String text(OMElement element) {
        if (element == null) {
            return "";
        } else {
            return element.getText();
        }
    }
    
    private static long getLong(String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        } else {
            return Long.parseLong(value);
        }
    }
    
    private static Object[] createPayload(OMElement page) throws ParseException {
        String title = text(page.getFirstChildWithName(new QName(NS, "title")));
        OMElement revision = page.getFirstChildWithName(new QName(NS, "revision"));
        String revisionTSStr = text(revision.getFirstChildWithName(new QName(NS, "timestamp")));
        long revisionTS = timestamp(revisionTSStr);
        OMElement contributor = revision.getFirstChildWithName(new QName(NS, "contributor"));
        String contributorUsername = text(contributor.getFirstChildWithName(new QName(NS, "username")));
        long contributorId = getLong(text(contributor.getFirstChildWithName(new QName(NS, "id"))));
        String comment = text(revision.getFirstChildWithName(new QName(NS, "comment")));
        String model = text(revision.getFirstChildWithName(new QName(NS, "model")));
        String format = text(revision.getFirstChildWithName(new QName(NS, "format")));
        String text = text(revision.getFirstChildWithName(new QName(NS, "text")));
        String sha1 = text(revision.getFirstChildWithName(new QName(NS, "sha1")));
        return new Object[] { sha1, title, revisionTS, contributorUsername, 
                contributorId, comment, model, format, text, text.length() };
    }

}
