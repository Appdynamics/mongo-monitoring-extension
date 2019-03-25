/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.mongo.stats;

import com.appdynamics.extensions.metrics.Metric;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.appdynamics.monitors.mongo.utils.Constants.METRICS_SEPARATOR;
import static com.appdynamics.monitors.mongo.utils.MetricPrintUtils.getNumericMetricsFromMap;
import static com.appdynamics.monitors.mongo.utils.MongoUtils.executeMongoCommand;

/**
 * Created by bhuvnesh.kumar on 3/22/19.
 */
public class DBStats {

    public static List<Metric> fetchDBStats(MongoClient mongoClient, String metricPrefix) {
        Document commandJson = new Document();
        commandJson.append("dbStats", 1);
        List<Metric> metrics = new ArrayList<Metric>();
        for (String databaseName : mongoClient.listDatabaseNames()) {
            MongoDatabase db = mongoClient.getDatabase(databaseName);
            BasicDBObject dbStats = executeMongoCommand(db, commandJson);
            metrics.addAll(getDBStats(dbStats, metricPrefix));
        }
        return metrics;
    }

    private static List<Metric> getDBStats(BasicDBObject dbStats, String metricPrefix) {
        String dbStatsPath = getDBStatsMetricPrefix(dbStats.get("db").toString(), metricPrefix);
        return getNumericMetricsFromMap(dbStats.toMap(), dbStatsPath);

    }

    private static String getDBStatsMetricPrefix(String dbName, String metricPrefix) {
        return metricPrefix + "DB Stats|" + dbName + METRICS_SEPARATOR;
    }

}
