package org.wso2.das.http.server;

/**
 * This is a sample HTTP server to receive events through HTTP/HTTPS protocol
 */
public class HttpServerMain {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(HttpServerMain.class);

    public static void main(String[] args) throws InterruptedException {
        while (true) {
            HttpServerListenerHandler lst = new HttpServerListenerHandler(8080);
            while (!lst.getServerListner().iaMessageArrive()) {
                Thread.sleep(100);
            }
            logger.info("Received Event :" + lst.getServerListner().getData());
            logger.info("Received Event Headers :" + lst.getServerListner().getHeaders().toString());
            lst.shutdown();
        }
    }
}
