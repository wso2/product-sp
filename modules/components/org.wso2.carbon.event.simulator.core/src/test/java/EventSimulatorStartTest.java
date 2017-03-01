import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.eventsimulator.core.EventSimulatorStart;
import scala.util.parsing.json.JSON;

/**
 * Created by ruwini on 2/18/17.
 */
public class EventSimulatorStartTest {
    private static Logger logger = LoggerFactory.getLogger(EventSimulatorStartTest.class);
    public static void main(String[] args) {
        logger.info("Test Start");
        String config = "{\"Type\":\"single\",\"Config\" : \"{\\\"streamName\\\":\\\"stream2\\\",\\\"attributeValues\\\":[\\\"WSO2\\\",\\\"345\\\",\\\"45\\\"]}\"}";
//        String config = "{\"Type\" :\"single\",\"Config\":\"{\\\"streamName\\\" : \\\"stream2\\\"}\"}";

        EventSimulatorStart eventSimulatorStart = new EventSimulatorStart();
        eventSimulatorStart.EventSimulatorStart(config);
        logger.info("Test Stop");
    }
}
