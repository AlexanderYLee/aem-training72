package com.epam.aemtraining.impl;

import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Created by Aliaksandr_Li on 5/13/2016.
 */
@Component(immediate = true,metatype=true)
@Service(EventHandler.class)
@Properties({
    @Property(name = EventConstants.EVENT_TOPIC, value = SlingConstants.TOPIC_RESOURCE_CHANGED),
    @Property(name = EventConstants.EVENT_FILTER, value =
            "(&(path=/content/myapp/*)(resourceRemovedAttributes=*))")
})
public class PropertyDeletionEventListner implements EventHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());


    @Reference
    JobManager jobManager;

    public void handleEvent(Event event) {
        log.debug("Event is caught:" + event.getTopic());
        Map<String, Object> props = new HashMap<String, Object>(2);
        props.put("path", event.getProperty(SlingConstants.PROPERTY_PATH));
        props.put("names", event.getProperty(SlingConstants.PROPERTY_REMOVED_ATTRIBUTES));
        Job job = jobManager.addJob("com/epam/aemtraining72/job", props);
        log.debug("Job is added:" + job.getTopic());
    }
}