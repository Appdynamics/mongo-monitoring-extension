/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.mongo.stats;

import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.mongo.input.Stat;
import com.appdynamics.extensions.mongo.utils.Constants;
import com.appdynamics.extensions.mongo.utils.MetricUtils;
import com.appdynamics.extensions.mongo.utils.MongoUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Phaser;

/**
 * Created by bhuvnesh.kumar on 3/22/19.
 */
public class ServerStats implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(ServerStats.class);

    private Stat stat;

    private MetricWriteHelper metricWriteHelper;

    private List<Metric> metrics = new ArrayList<Metric>();

    private String metricPrefix;

    private MongoDatabase adminDB;

    private Phaser phaser;

    private MetricUtils metricUtils;

    public ServerStats(Stat stat, MongoDatabase adminDB, MetricWriteHelper metricWriteHelper, String metricPrefix, Phaser phaser) {
        this.stat = stat;
        this.adminDB = adminDB;
        this.metricWriteHelper = metricWriteHelper;
        this.metricPrefix = metricPrefix;
        this.metricUtils = new MetricUtils();
        this.phaser = phaser;
        this.phaser.register();
    }

    public void run(){
        logger.debug("Begin fetching sever stats");
        fetchAndPrintServerStats(adminDB, metricPrefix);
    }

    public void fetchAndPrintServerStats(MongoDatabase adminDB, String metricPrefix) {
        Boolean status = true;
        try {
            Document commandJson = new Document();
            commandJson.append("serverStatus", 1);
            BasicDBObject serverStats = MongoUtils.executeMongoCommand(adminDB, commandJson);
            if (serverStats != null) {
                metrics.addAll(metricUtils.generateMetrics(metricUtils.getNumericMetricsFromMap(serverStats.toMap(), null), getServerStatsMetricPrefix(metricPrefix), stat));
            }else
                status = false;
            if (metrics != null && metrics.size() > 0) {
                metricWriteHelper.transformAndPrintMetrics(metrics);
            }
        }catch(Exception e){
            logger.error("Error fetching serverStats" , e);
        }finally {
            if (status == true) {
                metricWriteHelper.printMetric(metricPrefix + Constants.HEART_BEAT, "1", "AVERAGE", "AVERAGE", "INDIVIDUAL");
            } else {
                metricWriteHelper.printMetric(metricPrefix + Constants.HEART_BEAT, "0", "AVERAGE", "AVERAGE", "INDIVIDUAL");
            }
            logger.debug("ServerStats Phaser arrived for {}", adminDB.getName());
            phaser.arriveAndDeregister();
        }
    }

    private static String getServerStatsMetricPrefix(String metricPrefix) {
        return metricPrefix + "Server Stats";
    }

}
