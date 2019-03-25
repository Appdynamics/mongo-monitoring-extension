/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.mongo.connection;

import com.appdynamics.monitors.mongo.exception.MongoMonitorException;
import com.mongodb.MongoClientOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.appdynamics.monitors.mongo.utils.Constants.CONNECTION;
import static com.appdynamics.monitors.mongo.utils.Constants.SSL_ENABLED;

/**
 * Created by bhuvnesh.kumar on 3/19/19.
 */
public class MongoClientSSLOptions {
    private static final Logger logger = LoggerFactory.getLogger(MongoClientSSLOptions.class);

    public static MongoClientOptions getMongoClientSSLOptions(Map config) throws MongoMonitorException {
        Boolean ssl = false;
        if (config.get(SSL_ENABLED) != null) {
            ssl = (Boolean) config.get(SSL_ENABLED);
        }
        Map<String, ?> connectionMap = (Map<String, ?>) config.get(CONNECTION);
        MongoClientOptions.Builder optionsBuilder = MongoClientOptions.builder();
        if (ssl) {
            if (connectionMap.get("connectTimeout") != null) {
                optionsBuilder.connectTimeout((Integer) connectionMap.get("connectTimeout"));
            }
            if (connectionMap.get("socketTimeout") != null) {
                optionsBuilder.socketTimeout((Integer) connectionMap.get("socketTimeout"));
            }
            try {
                optionsBuilder.sslEnabled(true);
            } catch (Exception e) {
                logger.error("Error enabling ssl on the mongo client builder", e);
                throw new MongoMonitorException("Error establishing ssl socket factory");
            }
        } else {
            try {
                optionsBuilder.sslEnabled(false);
            } catch (Exception e) {
                logger.error("Error enabling ssl on the mongo client builder", e);
                throw new MongoMonitorException("Error establishing ssl socket factory");
            }
        }
        return optionsBuilder.build();
    }
}