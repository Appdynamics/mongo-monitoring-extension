/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.mongo.utils;

import com.appdynamics.monitors.mongo.connection.MongoClientSSLOptions;
import com.appdynamics.monitors.mongo.exception.MongoMonitorException;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.util.CryptoUtils.getPassword;
import static com.appdynamics.monitors.mongo.utils.Constants.ADMIN_DB;
import static com.appdynamics.monitors.mongo.utils.Constants.ENCRYPTED_PASSWORD;
import static com.appdynamics.monitors.mongo.utils.Constants.ENCRYPTION_KEY;
import static com.appdynamics.monitors.mongo.utils.Constants.HOST;
import static com.appdynamics.monitors.mongo.utils.Constants.PASSWORD;
import static com.appdynamics.monitors.mongo.utils.Constants.PORT;
import static com.appdynamics.monitors.mongo.utils.Constants.USERNAME;

/**
 * Created by bhuvnesh.kumar on 3/21/19.
 */
public class MongoClientGenerator {
    private static final Logger logger = LoggerFactory.getLogger(MongoClientGenerator.class);

    public static MongoClient getMongoClient(List servers, Map config) {
        MongoClient mongoClient = null;
        try {
            MongoClientOptions clientSSLOptions = MongoClientSSLOptions.getMongoClientSSLOptions(config);
            MongoCredential credential = getMongoCredentials((Map<String, String>) getCredentials(config));
            mongoClient = buildMongoClient(credential, clientSSLOptions, servers);
        } catch (MongoMonitorException e) {
            logger.error("Error in building the MongoClientGenerator", e);
        }
        return mongoClient;
    }

    private static MongoCredential getMongoCredentials(Map<String, String> credentials) {
        MongoCredential adminDBCredential = null;
        if (credentials.get(USERNAME) != null && credentials.get(PASSWORD) != null) {
            adminDBCredential = MongoCredential.createCredential(credentials.get(USERNAME), ADMIN_DB, credentials.get(PASSWORD).toCharArray());
        } else {
            logger.info("username and password in config are null or empty");
        }
        return adminDBCredential;
    }

    private static MongoClient buildMongoClient(MongoCredential credential, MongoClientOptions options, List<Map> servers) {

        MongoClient mongoClient = null;
        List<ServerAddress> seeds = Lists.newArrayList();
        for (Map server : servers) {
            seeds.add(new ServerAddress(server.get(HOST).toString(), (Integer) server.get(PORT)));
        }
        if (options == null && credential == null) {
            mongoClient = new MongoClient(seeds);
        } else if (options != null && credential == null) {
            mongoClient = new MongoClient(seeds, options);
        } else {
            mongoClient = new MongoClient(seeds, credential, options);
        }
        return mongoClient;
    }

    private static Map getCredentials(Map config) {
        Map<String, String> credentials = new HashMap<String, String>();

        if (!Strings.isNullOrEmpty(config.get(USERNAME).toString())) {
            credentials.put(USERNAME, config.get(USERNAME).toString());
        }
        if (!Strings.isNullOrEmpty(config.get(PASSWORD).toString())) {
            credentials.put(PASSWORD, config.get(PASSWORD).toString());
        }
        if (!Strings.isNullOrEmpty(config.get(ENCRYPTED_PASSWORD).toString())) {
            credentials.put(ENCRYPTED_PASSWORD, config.get(ENCRYPTED_PASSWORD).toString());
        }
        if (!Strings.isNullOrEmpty(config.get(ENCRYPTION_KEY).toString())) {
            credentials.put(ENCRYPTION_KEY, config.get(ENCRYPTION_KEY).toString());
        }
        String password = getPassword(credentials);
        credentials.remove(ENCRYPTION_KEY);
        credentials.remove(ENCRYPTED_PASSWORD);
        credentials.put(PASSWORD, password);
        return credentials;
    }

}
