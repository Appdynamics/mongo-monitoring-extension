package com.appdynamics.monitors.mongo;

import com.appdynamics.extensions.AMonitorTaskRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bhuvnesh.kumar on 3/12/19.
 */
public class MongoDBMonitorTask implements AMonitorTaskRunnable {
    private static final Logger logger = LoggerFactory.getLogger(MongoDBMonitorTask.class);

    public void run() {
        ;
    }
    public void onTaskComplete() {
        logger.debug("Task Complete");
//        if (status == true) {
//            metricWriter.printMetric(metricPrefix + METRICS_SEPARATOR + server.get(DISPLAY_NAME).toString() + METRICS_SEPARATOR + AVAILABILITY, "1", "AVERAGE", "AVERAGE", "INDIVIDUAL");
//        } else {
//            metricWriter.printMetric(metricPrefix + METRICS_SEPARATOR + server.get(DISPLAY_NAME).toString() + METRICS_SEPARATOR + AVAILABILITY, "0", "AVERAGE", "AVERAGE", "INDIVIDUAL");
//        }
    }

}
