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
import com.appdynamics.monitors.mongo.utils.MetricPrintUtils;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.BSONTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Phaser;

import static com.appdynamics.monitors.mongo.utils.MongoUtils.executeMongoCommand;

/**
 * Created by bhuvnesh.kumar on 3/22/19.
 */
public class ReplicaStats implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(ReplicaStats.class);

    private Stat stat;

    private MetricWriteHelper metricWriteHelper;

    private List<Metric> metrics = new ArrayList<Metric>();

    private String metricPrefix;

    private MongoDatabase adminDB;

    private MongoClient mongoClient;

    private Phaser phaser;

    private MetricPrintUtils metricPrintUtils;

    public ReplicaStats(Stat stat, MongoDatabase adminDB,  MongoClient mongoClient, MetricWriteHelper metricWriteHelper, String metricPrefix, Phaser phaser) {
        this.stat = stat;
        this.adminDB = adminDB;
        this.metricWriteHelper = metricWriteHelper;
        this.mongoClient = mongoClient;
        this.metricPrefix = metricPrefix;
        this.metricPrintUtils = new MetricPrintUtils();
        this.phaser = phaser;
        this.phaser.register();
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    public void run(){
        logger.debug("Begin fetching replica stats");
        fetchAndPrintReplicaSetStats(adminDB, metricPrefix);
    }

    public void fetchAndPrintReplicaSetStats(MongoDatabase adminDB, String metricPrefix) {
        int primaryElected = 0;
        try {
            Document commandJson = new Document();
            commandJson.append("replSetGetStatus", 1);
            BasicDBObject replicaStats = executeMongoCommand(adminDB, commandJson);
            if (replicaStats != null) {
                BasicDBList members = (BasicDBList) replicaStats.get("members");
                Map<String, Object> memebersData = new HashMap<>();
                for (int i = 0; i < members.size(); i++) {
                    DBObject member = (DBObject) members.get(i);
                    memebersData.put(member.get("name").toString(),member.toMap());
                    if (member.get("stateStr").toString().equalsIgnoreCase("PRIMARY"))
                        primaryElected = 1;
                }

                metrics.addAll(metricPrintUtils.generateReplicaMetrics(metricPrintUtils.getNumericMetricsFromMap(memebersData, null), getReplicaStatsMetricPrefix(metricPrefix), stat, memebersData.keySet(), primaryElected));
            }

            logger.debug("Calculating replication oplog window");
            DB db = mongoClient.getDB("local");
            DBCollection collection = db.getCollection("oplog.rs");
            List<DBObject> dbObjects = collection.find().sort(new BasicDBObject("ts",-1)).toArray();

            DBObject startEntry = dbObjects.get(0);
            DBObject lastEntry = dbObjects.get(dbObjects.size()-1);
            BSONTimestamp startTime = (BSONTimestamp)startEntry.get("ts");
            BSONTimestamp endTime = (BSONTimestamp)lastEntry.get("ts");
            int diff = startTime.getTime()-endTime.getTime();

            metrics.add(new Metric("Replica Oplog Window", String.valueOf(diff), getReplicaStatsMetricPrefix(metricPrefix) + "| Replica Oplog Window", "OBS", "CUR", "COL"));

            logger.debug("Fetched all replica metrics");
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
