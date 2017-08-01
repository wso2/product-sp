package org.wso2.sp.http.server;

/**
 * This is a sample HTTP server to receive events through HTTP/HTTPS protocol.
 */
public class HttpServerMain {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(HttpServerMain.class);

    public static void main(String[] args) throws InterruptedException {
        while (true) {
            HttpServerListenerHandler lst = new HttpServerListenerHandler(8080);
            while (!lst.getServerListner().isMessageArrive()) {
                Thread.sleep(100);
            }
            logger.info("Received Event Names:" + lst.getServerListner().getData());
            logger.info("Received Event Headers key set:" + lst.getServerListner().getHeaders().keySet().toString());
            logger.info("Received Event Headers value set:" + lst.getServerListner().getHeaders().values().toString());
            lst.shutdown();
        }
    }
}
