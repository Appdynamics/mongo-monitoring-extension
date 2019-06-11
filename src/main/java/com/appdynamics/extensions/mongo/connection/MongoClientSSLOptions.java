/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.mongo.connection;

import com.appdynamics.extensions.mongo.exception.MongoMonitorException;
import com.appdynamics.extensions.mongo.utils.Constants;
import com.mongodb.MongoClientOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by bhuvnesh.kumar on 3/19/19.
 */
public class MongoClientSSLOptions {
    private static final Logger logger = LoggerFactory.getLogger(MongoClientSSLOptions.class);

    public static MongoClientOptions getMongoClientSSLOptions(Map config) throws MongoMonitorException {
        Boolean ssl = false;
        if (config.get(Constants.USE_SSL) != null) {
            ssl = (Boolean) config.get(Constants.USE_SSL);
        }
        Map<String, ?> connectionMap = (Map<String, ?>) config.get(Constants.CONNECTION);
        MongoClientOptions.Builder optionsBuilder = MongoClientOptions.builder();
        if (ssl) {
            if (connectionMap.get(Constants.CONNECTION_TIMEOUT) != null) {
                optionsBuilder.connectTimeout((Integer) connectionMap.get(Constants.CONNECTION_TIMEOUT));
            }
            if (connectionMap.get(Constants.SOCKET_TIMEOUT) != null) {
                optionsBuilder.socketTimeout((Integer) connectionMap.get(Constants.SOCKET_TIMEOUT));
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
