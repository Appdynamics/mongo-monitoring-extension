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
import com.appdynamics.monitors.mongo.connection.SslUtils;
import com.appdynamics.monitors.mongo.utils.MongoClientGenerator;
import com.mongodb.MongoClient;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
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
        checkForSSL();
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

    private void checkForSSL() {
        if ((Boolean) getContextConfiguration().getConfigYml().get(USE_SSL)) {
            SslUtils sslUtils = new SslUtils();
            sslUtils.setSslProperties(getContextConfiguration().getConfigYml());
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


    public static void main(String[] args) throws TaskExecutionException, IOException {

//        ConsoleAppender ca = new ConsoleAppender();
//        ca.setWriter(new OutputStreamWriter(System.out));
//        ca.setLayout(new PatternLayout("%-5p [%t]: %m%n"));
//        ca.setThreshold(Level
//                .DEBUG);
//        org.apache.log4j.Logger.getRootLogger().addAppender(ca);

        MongoDBMonitor mongoMonitor = new MongoDBMonitor();
        Map<String, String> argsMap = new HashMap<String, String>();
        argsMap.put("config-file", "/Users/bhuvnesh.kumar/repos/appdynamics/extensions/mongo-monitoring-extension/src/main/resources/conf/config.yml");
        mongoMonitor.execute(argsMap, null);
    }

}
