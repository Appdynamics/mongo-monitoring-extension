/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.mongo;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.util.AssertUtils;
import com.appdynamics.monitors.mongo.utils.MongoClientGenerator;
import com.mongodb.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.appdynamics.monitors.mongo.utils.Constants.*;

public class MongoDBMonitor extends ABaseMonitor {

    private static final Logger logger = LoggerFactory.getLogger(MongoDBMonitor.class);

    @Override
    protected String getDefaultMetricPrefix() {
        return CUSTOMMETRICS + METRICS_SEPARATOR + MONITORNAME;
    }

    @Override
    public String getMonitorName() {
        return MONITORNAME;
    }

    @Override
    protected int getTaskCount() {
        List<Map<String, String>> servers = (List<Map<String, String>>) getContextConfiguration().getConfigYml().get(SERVERS);
        AssertUtils.assertNotNull(servers, "The 'servers' section in config.yml is not initialised");
        return servers.size();
    }

    @Override
    protected void doRun(TasksExecutionServiceProvider taskExecutor) {
        Map<String, ?> config = getContextConfiguration().getConfigYml();
        if (config != null) {
            List<Map> servers = (List) config.get(SERVERS);
            AssertUtils.assertNotNull(servers, "The 'servers' section in config.yml is not initialised");
            if (servers != null && !servers.isEmpty()) {
                MongoClient mongoClient = MongoClientGenerator.getMongoClient(servers, getContextConfiguration().getConfigYml());
                try {
                    MongoDBMonitorTask task = createTask(mongoClient, taskExecutor, getContextConfiguration().getConfigYml());
                    taskExecutor.submit("MongoDB", task);
                } catch (IOException e) {
                    logger.error("Cannot construct MongoClient uri for MongoDB");
                }
            } else {
                logger.error("There are no servers configured");
            }
        } else {
            logger.error("The config.yml is not loaded due to previous errors.The task will not run");
        }
    }

    private MongoDBMonitorTask createTask(MongoClient mongoClient, TasksExecutionServiceProvider taskExecutor, Map config) throws IOException {
        return new MongoDBMonitorTask.Builder()
                .mongoClient(mongoClient)
                .metricWriter(taskExecutor.getMetricWriteHelper())
                .monitorConfiguration(getContextConfiguration())
                .config(config)
                .build();
    }

}
