package com.appdynamics.monitors.mongo;

import com.appdynamics.extensions.AMonitorTaskRunnable;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.monitors.mongo.config.Configuration;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mongodb.MongoCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.TaskInputArgs.ENCRYPTED_PASSWORD;
import static com.appdynamics.extensions.TaskInputArgs.ENCRYPTION_KEY;
import static com.appdynamics.extensions.crypto.CryptoUtil.getPassword;
import static com.appdynamics.monitors.mongo.utils.Constants.*;
import static com.appdynamics.monitors.mongo.utils.MongoUtils.convertToString;

/**
 * Created by bhuvnesh.kumar on 3/12/19.
 */
public class MongoDBMonitorTask implements AMonitorTaskRunnable {
    private static final Logger logger = LoggerFactory.getLogger(MongoDBMonitorTask.class);
    private Boolean status = true;
    private String metricPrefix;
    private MetricWriteHelper metricWriter;
    private Map server;
    private MonitorContextConfiguration monitorContextConfiguration;

    public void run() {
        String host = convertToString(server.get(HOST), EMPTY_STRING);
        String portStr = convertToString(server.get(PORT), EMPTY_STRING);
        int port = (portStr == null || portStr == EMPTY_STRING) ? -1 : Integer.parseInt(portStr);
        String username = convertToString(server.get(USERNAME), EMPTY_STRING);
        String password = getPassword(server);

    }


    private List<MongoCredential> getMongoCredentials(Configuration config) {
        List<MongoCredential> credentials = Lists.newArrayList();
        if(!Strings.isNullOrEmpty(config.getAdminDBUsername()) && !Strings.isNullOrEmpty(config.getAdminDBPassword())) {
            MongoCredential adminDBCredential = MongoCredential.createCredential(config.getAdminDBUsername(), ADMIN_DB, getAdminDBPassword(config).toCharArray());
            credentials.add(adminDBCredential);
        } else {
            logger.info("adminDBUsername and adminDBPassword in config.yml is null or empty");
        }
        return credentials;
    }

    private String getAdminDBPassword(Configuration config) {
        String encryptionKey = config.getPasswordEncryptionKey();
        if (Strings.isNullOrEmpty(encryptionKey)) {
            return config.getAdminDBPassword();
        } else {
            String encryptedPassword = config.getAdminDBPassword();
            return getDecryptedPassword(encryptionKey, encryptedPassword);
        }
    }

    private String getDecryptedPassword(String encryptionKey, String encryptedPassword) {
        Map<String, String> argsForDecryption = new HashMap<String, String>();
        argsForDecryption.put(ENCRYPTED_PASSWORD, encryptedPassword);
        argsForDecryption.put(ENCRYPTION_KEY, encryptionKey);
        return getPassword(argsForDecryption);
    }



    public void onTaskComplete() {
        logger.debug("Task Complete");
        if (status == true) {
            metricWriter.printMetric(metricPrefix + METRICS_SEPARATOR + server.get(DISPLAY_NAME).toString() + METRICS_SEPARATOR + AVAILABILITY, "1", "AVERAGE", "AVERAGE", "INDIVIDUAL");
        } else {
            metricWriter.printMetric(metricPrefix + METRICS_SEPARATOR + server.get(DISPLAY_NAME).toString() + METRICS_SEPARATOR + AVAILABILITY, "0", "AVERAGE", "AVERAGE", "INDIVIDUAL");
        }
    }

    public static class Builder {
        private MongoDBMonitorTask task = new MongoDBMonitorTask();

        Builder metricPrefix(String metricPrefix) {
            task.metricPrefix = metricPrefix;
            return this;
        }

        Builder metricWriter(MetricWriteHelper metricWriter) {
            task.metricWriter = metricWriter;
            return this;
        }

        Builder server(Map server) {
            task.server = server;
            return this;
        }

        Builder monitorConfiguration(MonitorContextConfiguration monitorContextConfiguration) {
            task.monitorContextConfiguration = monitorContextConfiguration;
            return this;
        }

        MongoDBMonitorTask build() {
            return task;
        }
    }
}
