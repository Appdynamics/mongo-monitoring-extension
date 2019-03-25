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
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.monitors.mongo.stats.CollectionStats;
import com.appdynamics.monitors.mongo.stats.DBStats;
import com.appdynamics.monitors.mongo.stats.ReplicaStats;
import com.appdynamics.monitors.mongo.stats.ServerStats;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.appdynamics.monitors.mongo.utils.Constants.*;
import static com.appdynamics.monitors.mongo.utils.MetricPrintUtils.getMetricPathPrefix;

/**
 * Created by bhuvnesh.kumar on 3/12/19.
 */
public class MongoDBMonitorTask implements AMonitorTaskRunnable {
    private static final Logger logger = LoggerFactory.getLogger(MongoDBMonitorTask.class);
    private Boolean status = true;
    private Map config;
    private String metricPrefix;
    private MongoClient mongoClient;
    private MetricWriteHelper metricWriter;

    private MonitorContextConfiguration monitorContextConfiguration;

    public void run() {

        metricPrefix = getMetricPathPrefix(monitorContextConfiguration.getMetricPrefix());
        MongoDatabase adminDB = mongoClient.getDatabase(ADMIN_DB);
        List<Metric> serverStats = ServerStats.fetchAndPrintServerStats(adminDB, getServerStatusExcludeMetricFields(), metricPrefix);

        List<Metric> replicaStats = ReplicaStats.fetchAndPrintReplicaSetStats(adminDB, mongoClient, metricPrefix);
        List<Metric> dbStats = DBStats.fetchDBStats(mongoClient, metricPrefix);
        List<Metric> collectionStats = CollectionStats.fetchCollectionStats(mongoClient, metricPrefix);

        List<Metric> allMetrics = new ArrayList<Metric>();
        allMetrics.addAll(serverStats);
        allMetrics.addAll(replicaStats);
        allMetrics.addAll(dbStats);
        allMetrics.addAll(collectionStats);
        if (allMetrics.size() > 0) {
            metricWriter.transformAndPrintMetrics(allMetrics);
            status = true;
        } else {
            status = false;
        }
    }

    private List<String> getServerStatusExcludeMetricFields() {
        if (config.get("serverStatusExcludeMetricFields") != null) {
            return (List<String>) config.get("serverStatusExcludeMetricFields");
        } else {
            return null;
        }
    }

    public void onTaskComplete() {
        logger.debug("Task Complete");
        if (status == true) {
            metricWriter.printMetric(metricPrefix + METRICS_SEPARATOR + "MongoDB" + METRICS_SEPARATOR + AVAILABILITY, "1", "AVERAGE", "AVERAGE", "INDIVIDUAL");
        } else {
            metricWriter.printMetric(metricPrefix + METRICS_SEPARATOR + "MongoDB" + METRICS_SEPARATOR + AVAILABILITY, "0", "AVERAGE", "AVERAGE", "INDIVIDUAL");
        }
    }

    public static class Builder {
        private MongoDBMonitorTask task = new MongoDBMonitorTask();

        Builder metricWriter(MetricWriteHelper metricWriter) {
            task.metricWriter = metricWriter;
            return this;
        }

        Builder mongoClient(MongoClient mongoClient) {
            task.mongoClient = mongoClient;
            return this;
        }

        Builder config(Map config) {
            task.config = config;
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
