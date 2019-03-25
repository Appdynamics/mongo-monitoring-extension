/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.mongo.utils;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bhuvnesh.kumar on 3/12/19.
 */
public class MongoUtils {
    private static final Logger logger = LoggerFactory.getLogger(MongoUtils.class);

    public static String convertToString(final Object field, final String defaultStr) {
        if (field == null) {
            return defaultStr;
        }
        return field.toString();
    }

    public static BasicDBObject executeMongoCommand(MongoDatabase db, Document command) {

        BasicDBObject basicDBObject = null;
        try {
            basicDBObject = BasicDBObject.parse(db.runCommand(command).toJson());

        } catch (MongoCommandException e) {
            logger.error("Error while executing " + command + " for db " + db, e);
        }
        return basicDBObject;
    }

}
