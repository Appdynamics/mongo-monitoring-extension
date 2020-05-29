/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.mongo.utils;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.mongo.connection.MongoClientSSLOptions;
import com.appdynamics.extensions.mongo.exception.MongoMonitorException;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.util.CryptoUtils.getPassword;

/**
 * Created by bhuvnesh.kumar on 3/21/19.
 */
public class MongoClientGenerator {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(MongoClientGenerator.class);

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
        if (credentials.get(Constants.USERNAME) != null && credentials.get(Constants.PASSWORD) != null) {
            adminDBCredential = MongoCredential.createCredential(credentials.get(Constants.USERNAME), Constants.ADMIN_DB, credentials.get(Constants.PASSWORD).toCharArray());
        } else {
            logger.info("username and password in config are null or empty");
        }
        return adminDBCredential;
    }

    private static MongoClient buildMongoClient(MongoCredential credential, MongoClientOptions options, List<Map> servers) {

        MongoClient mongoClient = null;
        List<ServerAddress> seeds = Lists.newArrayList();
        for (Map server : servers) {
            seeds.add(new ServerAddress(server.get(Constants.HOST).toString(), (Integer) server.get(Constants.PORT)));
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

        if (!Strings.isNullOrEmpty(config.get(Constants.USERNAME).toString())) {
            credentials.put(Constants.USERNAME, config.get(Constants.USERNAME).toString());
        }
        if (!Strings.isNullOrEmpty(config.get(Constants.PASSWORD).toString())) {
            credentials.put(Constants.PASSWORD, config.get(Constants.PASSWORD).toString());
        }
        if (!Strings.isNullOrEmpty(config.get(Constants.ENCRYPTED_PASSWORD).toString())) {
            credentials.put(Constants.ENCRYPTED_PASSWORD, config.get(Constants.ENCRYPTED_PASSWORD).toString());
        }
        if (!Strings.isNullOrEmpty(config.get(Constants.ENCRYPTION_KEY).toString())) {
            credentials.put(Constants.ENCRYPTION_KEY, config.get(Constants.ENCRYPTION_KEY).toString());
        }
        String password = getPassword(credentials);
        credentials.remove(Constants.ENCRYPTION_KEY);
        credentials.remove(Constants.ENCRYPTED_PASSWORD);
        credentials.put(Constants.PASSWORD, password);
        return credentials;
    }

}
