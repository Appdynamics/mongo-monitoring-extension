/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.mongo;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.mongo.connection.SslUtils;
import com.appdynamics.extensions.mongo.input.Stat;
import com.appdynamics.extensions.mongo.utils.Constants;
import com.appdynamics.extensions.mongo.utils.MongoClientGenerator;
import com.appdynamics.extensions.util.AssertUtils;
import com.mongodb.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;

public class MongoDBMonitor extends ABaseMonitor {

    private static final Logger logger = LoggerFactory.getLogger(MongoDBMonitor.class);

    @Override
    protected String getDefaultMetricPrefix() {
        return Constants.CUSTOMMETRICS + Constants.METRICS_SEPARATOR + Constants.MONITORNAME;
    }

    @Override
    public String getMonitorName() {
        return Constants.MONITORNAME;
    }


    @Override
    protected void doRun(TasksExecutionServiceProvider taskExecutor) {
        checkForSSL();
        List<Map<String, ?>> servers = getServers();
        AssertUtils.assertNotNull(servers, "The 'servers' section in config.yml is not initialised");
        if (servers != null && !servers.isEmpty()) {
                MongoClient mongoClient = MongoClientGenerator.getMongoClient(servers, getContextConfiguration().getConfigYml());
                try {
                    MongoDBMonitorTask task = createTask(mongoClient, taskExecutor, getContextConfiguration().getConfigYml());
                    taskExecutor.submit("MongoDB", task);
                } catch (Exception e) {
                    logger.error("Cannot construct MongoClient uri for MongoDB", e);
                }
        } else {
            logger.error("There are no servers configured");
        }
    }

    @Override
    protected List<Map<String, ?>> getServers() {
        return (List) getContextConfiguration().getConfigYml().get(Constants.SERVERS);
    }

    private void checkForSSL() {
        if ((Boolean) getContextConfiguration().getConfigYml().get(Constants.USE_SSL)) {
            logger.debug("SSL set to true, setting SSL properties");
            SslUtils sslUtils = new SslUtils();
            sslUtils.setSslProperties(getContextConfiguration().getConfigYml());
        }
    }

    private MongoDBMonitorTask createTask(MongoClient mongoClient, TasksExecutionServiceProvider taskExecutor, Map config){
        return new MongoDBMonitorTask.Builder()
                .mongoClient(mongoClient)
                .metricWriter(taskExecutor.getMetricWriteHelper())
                .monitorConfiguration(getContextConfiguration())
                .metricPrefix(getContextConfiguration().getMetricPrefix())
                .config(config)
                .build();
    }

    @Override
    protected void initializeMoreStuff(Map<String, String> args) {
        this.getContextConfiguration().setMetricXml(args.get("metric-file"), Stat.Stats.class);

    }

}
