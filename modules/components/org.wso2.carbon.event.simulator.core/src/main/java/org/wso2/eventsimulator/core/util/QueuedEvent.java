package org.wso2.eventsimulator.core.util;

/**
 * Created by ruwini on 2/2/17.
 */

import org.wso2.siddhi.core.event.Event;

public class QueuedEvent implements Comparable<QueuedEvent> {
    private Long id;
    private Event event;

    public QueuedEvent(Long id, Event event) {
        this.id = id;
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

    public int compareTo(QueuedEvent event) {
        if (id < event.id) {
            return 1;
        } else if (id > event.id) {
            return -1;
        } else {
            return 0;
        }
    }
}
