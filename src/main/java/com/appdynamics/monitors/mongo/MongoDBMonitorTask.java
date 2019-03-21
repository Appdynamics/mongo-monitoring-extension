/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.mongo;

import com.appdynamics.extensions.AMonitorTaskRunnable;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.mongodb.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.appdynamics.monitors.mongo.utils.Constants.*;
import static com.appdynamics.monitors.mongo.utils.MongoUtils.convertToString;

/**
 * Created by bhuvnesh.kumar on 3/12/19.
 */
public class MongoDBMonitorTask implements AMonitorTaskRunnable {
    private static final Logger logger = LoggerFactory.getLogger(MongoDBMonitorTask.class);
    private Boolean status = true;
    private String metricPrefix;
    private MetricWriteHelper metricWriter;
    private Map server;
    private MongoClient mongoClient;

    private MonitorContextConfiguration monitorContextConfiguration;

    public void run() {

        metricPrefix = monitorContextConfiguration.getMetricPrefix();
        String host = convertToString(server.get(HOST), EMPTY_STRING);
        String portStr = convertToString(server.get(PORT), EMPTY_STRING);
        int port = (portStr == null || portStr == EMPTY_STRING) ? -1 : Integer.parseInt(portStr);
    }


    /////////////////
    /////////////////

    /////////////////
    /////////////////


    public void onTaskComplete() {
        logger.debug("Task Complete");
        if (status == true) {
            metricWriter.printMetric(metricPrefix + METRICS_SEPARATOR + server.get(DISPLAY_NAME).toString() + METRICS_SEPARATOR + AVAILABILITY, "1", "AVERAGE", "AVERAGE", "INDIVIDUAL");
        } else {
            metricWriter.printMetric(metricPrefix + METRICS_SEPARATOR + server.get(DISPLAY_NAME).toString() + METRICS_SEPARATOR + AVAILABILITY, "0", "AVERAGE", "AVERAGE", "INDIVIDUAL");
        }
    }

    public static class Builder {
        private MongoDBMonitorTask task = new MongoDBMonitorTask();

        Builder metricWriter(MetricWriteHelper metricWriter) {
            task.metricWriter = metricWriter;
            return this;
        }

        Builder server(Map server) {
            task.server = server;
            return this;
        }

        Builder monitorConfiguration(MonitorContextConfiguration monitorContextConfiguration) {
            task.monitorContextConfiguration = monitorContextConfiguration;
            return this;
        }

        MongoDBMonitorTask build() {
            return task;
        }
    }
}
