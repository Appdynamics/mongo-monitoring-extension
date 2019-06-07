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
import com.appdynamics.extensions.util.StringUtils;
import com.appdynamics.monitors.mongo.input.Stat;
import com.appdynamics.monitors.mongo.stats.CollectionStats;
import com.appdynamics.monitors.mongo.stats.DBStats;
import com.appdynamics.monitors.mongo.stats.ReplicaStats;
import com.appdynamics.monitors.mongo.stats.ServerStats;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Phaser;

import static com.appdynamics.monitors.mongo.utils.Constants.ADMIN_DB;
import static com.appdynamics.monitors.mongo.utils.Constants.AVAILABILITY;
import static com.appdynamics.monitors.mongo.utils.Constants.METRICS_SEPARATOR;

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

        if (!metricPrefix.endsWith(METRICS_SEPARATOR)) {
            metricPrefix += METRICS_SEPARATOR;
        }
        try {
            MongoDatabase adminDB = mongoClient.getDatabase(ADMIN_DB);

            Phaser phaser = new Phaser();
            phaser.register();
            Stat.Stats metricConfig = (Stat.Stats) monitorContextConfiguration.getMetricsXml();
            for (Stat stat : metricConfig.getStats()) {
                if (StringUtils.hasText(stat.getName()) && stat.getName().equalsIgnoreCase("serverStats")) {
                    ServerStats serverMetricTask = new ServerStats(stat, adminDB, metricWriter, metricPrefix, phaser);
                    monitorContextConfiguration.getContext().getExecutorService().execute("MetricCollectorTask", serverMetricTask);
                    logger.debug("Registering MetricCollectorTask phaser for server stats");
                } else if (StringUtils.hasText(stat.getName()) && stat.getName().equalsIgnoreCase("replicaStats")) {
                    ReplicaStats replicaMetricsTask = new ReplicaStats(stat, adminDB, mongoClient, metricWriter, metricPrefix, phaser);
                    monitorContextConfiguration.getContext().getExecutorService().execute("MetricCollectorTask", replicaMetricsTask);
                    logger.debug("Registering MetricCollectorTask phaser for replica stats");
                } else if (StringUtils.hasText(stat.getName()) && stat.getName().equalsIgnoreCase("dbStats")) {
                    DBStats dbMetricsTask = new DBStats(stat, adminDB, mongoClient, metricWriter, metricPrefix, phaser);
                    monitorContextConfiguration.getContext().getExecutorService().execute("MetricCollectorTask", dbMetricsTask);
                    logger.debug("Registering MetricCollectorTask phaser for database stats");
                } else if (StringUtils.hasText(stat.getName()) && stat.getName().equalsIgnoreCase("collectionStats")) {
                    CollectionStats collectionMetricsTask = new CollectionStats(stat, mongoClient, metricWriter, metricPrefix, phaser);
                    monitorContextConfiguration.getContext().getExecutorService().execute("MetricCollectorTask", collectionMetricsTask);
                    logger.debug("Registering MetricCollectorTask phaser for collection stats");
                }
            }
        //Wait for all tasks to finish
        phaser.arriveAndAwaitAdvance();
        logger.info("Completed the MongoDB Monitoring task");

        }catch(Exception e) {
        logger.error("Unexpected error while running the NongoDB Monitor", e);
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

        Builder metricPrefix(String metricPrefix) {
            task.metricPrefix = metricPrefix;
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
