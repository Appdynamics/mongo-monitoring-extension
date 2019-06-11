/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.mongo.stats;

import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.monitors.mongo.input.Stat;
import com.appdynamics.monitors.mongo.utils.MetricUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Phaser;

import static com.appdynamics.monitors.mongo.utils.Constants.METRICS_SEPARATOR;

/**
 * Created by bhuvnesh.kumar on 3/22/19.
 */
public class CollectionStats implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(CollectionStats.class);

    private Stat stat;

    private MetricWriteHelper metricWriteHelper;

    private List<Metric> metrics = new ArrayList<Metric>();

    private String metricPrefix;

    private MongoClient mongoClient;

    private Phaser phaser;

    private MetricUtils metricUtils;

    public CollectionStats(Stat stat, MongoClient mongoClient, MetricWriteHelper metricWriteHelper, String metricPrefix, Phaser phaser) {
        this.stat = stat;
        this.metricWriteHelper = metricWriteHelper;
        this.metricPrefix = metricPrefix;
        this.mongoClient = mongoClient;
        this.metricUtils = new MetricUtils();
        this.phaser = phaser;
        this.phaser.register();
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    @Override
    public void run() {
        logger.debug("Begin fetching collection stats");
        fetchCollectionStats(mongoClient, metricPrefix);
    }

    public void fetchCollectionStats(MongoClient mongoClient, String metricPrefix) {

        try {
            for (String databaseName : mongoClient.listDatabaseNames()) {
                DB db = mongoClient.getDB(databaseName);
                Set<String> collectionNames = db.getCollectionNames();
                if (collectionNames != null && collectionNames.size() > 0) {
                    for (String collectionName : collectionNames) {
                        DBCollection collection = db.getCollection(collectionName);
                        CommandResult collectionStatsResult = collection.getStats();
                        if (collectionStatsResult != null ) {
                            BasicDBObject collectionStats = BasicDBObject.parse(collectionStatsResult.toString());
                            if (collectionStats != null) {
                                metrics.addAll(metricUtils.generateMetrics(metricUtils.getNumericMetricsFromMap(collectionStats.toMap(), null), getCollectionStatsMetricPrefix(databaseName, collectionName, metricPrefix), stat));
                            }
                            if (metrics != null && metrics.size() > 0) {
                                metricWriteHelper.transformAndPrintMetrics(metrics);
                            }
                        } else {
                            String errorMessage = "Retrieving stats for collection " + collectionName + " of " + db.getName() + " failed";
                            if (collectionStatsResult != null) {
                                errorMessage = errorMessage.concat(" with error message " + collectionStatsResult.getErrorMessage());
                            }
                            logger.error(errorMessage);
                        }
                    }
                }
            }
        }catch(Exception e){
            logger.error("Error fetching collectionStats" , e);
        }finally {
            logger.debug("CollectionStats Phaser arrived");
            phaser.arriveAndDeregister();
        }
    }

    private static String getCollectionStatsMetricPrefix(String dbName, String collectionName, String metricPrefix) {
        return getDBStatsMetricPrefix(dbName, metricPrefix) + "Collection Stats|" + collectionName;
    }

    private static String getDBStatsMetricPrefix(String dbName, String metricPrefix) {
        return metricPrefix + "|DB Stats|" + dbName + METRICS_SEPARATOR;
    }


}
