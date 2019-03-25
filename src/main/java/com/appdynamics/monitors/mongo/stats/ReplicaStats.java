/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.mongo.stats;

import com.appdynamics.extensions.metrics.Metric;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.appdynamics.monitors.mongo.utils.Constants.METRICS_SEPARATOR;
import static com.appdynamics.monitors.mongo.utils.MongoUtils.executeMongoCommand;

/**
 * Created by bhuvnesh.kumar on 3/22/19.
 */
public class ReplicaStats {
    private static final Logger logger = LoggerFactory.getLogger(ReplicaStats.class);

    public static List<Metric> fetchAndPrintReplicaSetStats(MongoDatabase adminDB, MongoClient mongoClient, String metricPrefix) {
        if (mongoClient.getReplicaSetStatus() != null) {
            Document commandJson = new Document();
            commandJson.append("replSetGetStatus", 1);
            BasicDBObject replicaStats = executeMongoCommand(adminDB, commandJson);
            return getReplicaStats(replicaStats, metricPrefix);
        } else {
            logger.info("not running with --replSet, skipping replicaset stats");
            return null;
        }
    }

    private static List<Metric> getReplicaStats(BasicDBObject replicaStats, String metricPrefix) {
        List<Metric> metrics = new ArrayList<Metric>();
        if (replicaStats != null) {
            String replicaStatsPath = getReplicaStatsMetricPrefix(metricPrefix);
            BasicDBList members = (BasicDBList) replicaStats.get("members");
            for (int i = 0; i < members.size(); i++) {
                BasicDBObject member = (BasicDBObject) members.get(i);
                Metric metricHealth = new Metric("Health", member.get("health").toString(), replicaStatsPath + member.get("name") + METRICS_SEPARATOR + "Health");
                metrics.add(metricHealth);
                Metric metricState = new Metric("State", member.get("state").toString(), replicaStatsPath + member.get("state") + METRICS_SEPARATOR + "State");
                metrics.add(metricState);
                Metric metricUptime = new Metric("Uptime", member.get("uptime").toString(), replicaStatsPath + member.get("uptime") + METRICS_SEPARATOR + "Uptime");
                metrics.add(metricUptime);
            }
        }
        return metrics;
    }

    private static String getReplicaStatsMetricPrefix(String metricPrefix) {
        return metricPrefix + "Replica Stats" + METRICS_SEPARATOR;
    }
}
