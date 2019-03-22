/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.mongo.stats;

import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.monitors.mongo.utils.MetricPrintUtils;
import com.appdynamics.monitors.mongo.utils.MongoUtils;
import com.mongodb.DBObject;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.appdynamics.monitors.mongo.utils.Constants.METRICS_SEPARATOR;
import static com.appdynamics.monitors.mongo.utils.MetricPrintUtils.getMetricPathPrefix;

/**
 * Created by bhuvnesh.kumar on 3/22/19.
 */
public class ServerStats {

    private static final Logger logger = LoggerFactory.getLogger(ServerStats.class);


    public static List<Metric> fetchAndPrintServerStats(MongoDatabase adminDB, List<String> serverStatusExcludeMetricCategories, String metricPrefix) {
        Document commandJson = new Document();
        commandJson.append("serverStatus", 1);
        for (String suppressCategory : serverStatusExcludeMetricCategories) {
            commandJson.append(suppressCategory, 0);
        }
        DBObject serverStats = MongoUtils.executeMongoCommand(adminDB, commandJson);
        if (serverStats != null) {

            return getServerStats(serverStats, metricPrefix);
        } else {
            logger.error("ServerStatus returned null");
            return null;
        }
    }


    private static List<Metric> getServerStats(DBObject serverStats, String metricPrefix) {
        String metricPath = getServerStatsMetricPrefix(getMetricPathPrefix(metricPrefix));
        return MetricPrintUtils.getNumericMetricsFromMap(serverStats.toMap(), metricPath);
    }

    private static String getServerStatsMetricPrefix(String metricPrefix) {
        return metricPrefix + "Server Stats" + METRICS_SEPARATOR;
    }

}
