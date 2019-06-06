/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.mongo.stats;

import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContext;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.monitors.mongo.input.Stat;
import com.appdynamics.monitors.mongo.utils.MetricPrintUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Phaser;

import static com.appdynamics.monitors.mongo.utils.Constants.METRICS_SEPARATOR;
import static com.appdynamics.monitors.mongo.utils.MongoUtils.executeMongoCommand;

/**
 * Created by bhuvnesh.kumar on 3/22/19.
 */
public class DBStats implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(DBStats.class);

    private Stat stat;

    private MonitorContext context;

    private MetricWriteHelper metricWriteHelper;

    private List<Metric> metrics = new ArrayList<Metric>();

    private String metricPrefix;

    private MongoClient mongoClient;

    private MongoDatabase adminDB;

    private Phaser phaser;

    private MetricPrintUtils metricPrintUtils;

    public DBStats(Stat stat, MongoDatabase adminDB, MongoClient mongoClient, MonitorContext context, MetricWriteHelper metricWriteHelper, String metricPrefix, Phaser phaser) {
        this.stat = stat;
        this.adminDB = adminDB;
        this.mongoClient = mongoClient;
        this.context = context;
        this.metricWriteHelper = metricWriteHelper;
        this.metricPrefix = metricPrefix;
        this.metricPrintUtils = new MetricPrintUtils();
        this.phaser = phaser;
        this.phaser.register();
    }

    public void run(){
        logger.debug("Begin fetching db stats");
        fetchDBStats();
    }

    public void fetchDBStats() {
        try{
            Document commandJson = new Document();
            commandJson.append("dbStats", 1);

            for (String databaseName : mongoClient.listDatabaseNames()) {
                MongoDatabase db = mongoClient.getDatabase(databaseName);
                BasicDBObject dbStats = executeMongoCommand(db, commandJson);
                if (dbStats != null) {
                    metrics.addAll(metricPrintUtils.generateMetrics(metricPrintUtils.getNumericMetricsFromMap(dbStats.toMap(), null), getDBStatsMetricPrefix(dbStats.get("db").toString(), metricPrefix), stat));

                }
                if (metrics != null && metrics.size() > 0) {
                    metricWriteHelper.transformAndPrintMetrics(metrics);
                }
            }
        }catch(Exception e){
            logger.error("Error fetching DBStats" , e);
        }finally {
            logger.debug("DBStats Phaser arrived for {}", adminDB.getName());
            phaser.arriveAndDeregister();
        }
    }

    private String getDBStatsMetricPrefix(String dbName, String metricPrefix) {
        return metricPrefix + "DB Stats|" + dbName;
    }

}
