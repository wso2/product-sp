package org.wso2.carbon.analytics.jobmanager.restapi;

import org.wso2.msf4j.MicroservicesRunner;

/**
 * Application entry point.
 *
 * @since 1.0.0-SNAPSHOT
 */
public class Application {
    public static void main(String[] args) {

System.out.println("starting=========================================================");
        new MicroservicesRunner()
                .deploy(new WorkersApi())
                .start();
    }
}
