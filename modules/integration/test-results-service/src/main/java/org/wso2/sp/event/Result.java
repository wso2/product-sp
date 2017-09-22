package org.wso2.sp.event;

import javax.xml.bind.annotation.XmlRootElement;

/**.
 * Result class
 */

@XmlRootElement
public class Result {

    private String testCaseName;
    private String event;

    public Result() {

    }

    public Result(String testCaseName, String event) {
        this.testCaseName = testCaseName;
        this.event = event;
    }

    public String getTestCaseName() {
        return testCaseName;
    }

    public String getEvent() {
        return event;
    }

    public void setTestCaseName(String testCaseName) {
        this.testCaseName = testCaseName;
    }

    public void setEvent(String event) {
        this.event = event;
    }

}
