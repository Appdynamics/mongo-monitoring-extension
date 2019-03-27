/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.mongo.stats;

import com.appdynamics.extensions.metrics.Metric;
import com.mongodb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.appdynamics.monitors.mongo.utils.Constants.METRICS_SEPARATOR;
import static com.appdynamics.monitors.mongo.utils.MetricPrintUtils.getNumericMetricsFromMap;

/**
 * Created by bhuvnesh.kumar on 3/22/19.
 */
public class CollectionStats {
    private static final Logger logger = LoggerFactory.getLogger(CollectionStats.class);

    public static List<Metric> fetchCollectionStats(MongoClient mongoClient, String metricPrefix) {
        List<Metric> metricList = new ArrayList<Metric>();

        for (String databaseName : mongoClient.listDatabaseNames()) {
            DB db = (DB) mongoClient.getDatabase(databaseName);
            Set<String> collectionNames = db.getCollectionNames();
            if (collectionNames != null && collectionNames.size() > 0) {
                for (String collectionName : collectionNames) {
                    DBCollection collection = db.getCollection(collectionName);
                    CommandResult collectionStatsResult = collection.getStats();
                    if (collectionStatsResult != null && collectionStatsResult.ok()) {
                        BasicDBObject collectionStats = BasicDBObject.parse(collectionStatsResult.toString());
                        metricList.addAll(getCollectionStats(db.getName(), collectionName, collectionStats, metricPrefix));
                    } else {
                        String errorMessage = "Retrieving stats for collection " + collectionName + " of " + db.getName() + " failed";
                        if (collectionStatsResult != null) {
                            errorMessage = errorMessage.concat(" with error message " + collectionStatsResult.getErrorMessage());
                        }
                        logger.error(errorMessage);
                        return null;
                    }
                }
            }
        }

        return metricList;
    }


    private static List<Metric> getCollectionStats(String dbName, String collectionName, BasicDBObject collectionStats, String metricPrefix) {
        if (collectionStats != null) {
            String collectionStatsPath = getCollectionStatsMetricPrefix(dbName, collectionName, metricPrefix);
            return getNumericMetricsFromMap(collectionStats.toMap(), collectionStatsPath);
        } else {
            logger.info("CollectionStats for db " + dbName + "found to be NULL");
            return null;
        }
    }

    private static String getCollectionStatsMetricPrefix(String dbName, String collectionName, String metricPrefix) {
        return getDBStatsMetricPrefix(dbName, metricPrefix) + "Collection Stats|" + collectionName + METRICS_SEPARATOR;
    }

    private static String getDBStatsMetricPrefix(String dbName, String metricPrefix) {
        return metricPrefix + "DB Stats|" + dbName + METRICS_SEPARATOR;
    }


}
