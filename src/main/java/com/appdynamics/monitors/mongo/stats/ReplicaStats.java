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
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Phaser;

import static com.appdynamics.monitors.mongo.utils.Constants.METRICS_SEPARATOR;
import static com.appdynamics.monitors.mongo.utils.MongoUtils.executeMongoCommand;

/**
 * Created by bhuvnesh.kumar on 3/22/19.
 */
public class ReplicaStats implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(ReplicaStats.class);

    private Stat stat;

    private MonitorContext context;

    private MetricWriteHelper metricWriteHelper;

    private List<Metric> metrics = new ArrayList<Metric>();

    private String metricPrefix;

    private MongoDatabase adminDB;

    private Phaser phaser;

    private MetricPrintUtils metricPrintUtils;

    public ReplicaStats(Stat stat, MongoDatabase adminDB, MonitorContext context, MetricWriteHelper metricWriteHelper, String metricPrefix, Phaser phaser) {
        this.stat = stat;
        this.adminDB = adminDB;
        this.context = context;
        this.metricWriteHelper = metricWriteHelper;
        this.metricPrefix = metricPrefix;
        this.metricPrintUtils = new MetricPrintUtils();
        this.phaser = phaser;
        this.phaser.register();
    }

    public void run(){
        logger.debug("Begin fetching replica stats");
        fetchAndPrintReplicaSetStats(adminDB, metricPrefix);
    }

    public void fetchAndPrintReplicaSetStats(MongoDatabase adminDB, String metricPrefix) {
        try {
            Document commandJson = new Document();
            commandJson.append("replSetGetStatus", 1);
            BasicDBObject replicaStats = executeMongoCommand(adminDB, commandJson);
            if (replicaStats != null) {
                BasicDBList members = (BasicDBList) replicaStats.get("members");
                Map<String, Object> memebersData = new HashMap<>();
                for (int i = 0; i < members.size(); i++) {
                    DBObject member = (DBObject) members.get(i);
                    memebersData.putAll(member.toMap());
                }
                metrics.addAll(metricPrintUtils.generateMetrics(metricPrintUtils.getNumericMetricsFromMap(memebersData, null), getReplicaStatsMetricPrefix(metricPrefix), stat));
            }
            if (metrics != null && metrics.size() > 0) {
                metricWriteHelper.transformAndPrintMetrics(metrics);
            }
        } catch (Exception e) {
            logger.error("Error fetching replicaStats", e);
        } finally {
            logger.debug("ReplicaStats Phaser arrived for {}", adminDB.getName());
            phaser.arriveAndDeregister();
        }
    }



    private static String getReplicaStatsMetricPrefix(String metricPrefix) {
        return metricPrefix + "Replica Stats" ;
    }
}
