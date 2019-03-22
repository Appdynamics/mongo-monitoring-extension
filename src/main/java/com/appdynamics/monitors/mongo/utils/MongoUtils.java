/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.mongo.utils;

import com.mongodb.DBObject;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
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

    public static DBObject executeMongoCommand(MongoDatabase db, Document command) {
        DBObject dbObject = null;
        try {
            dbObject = (DBObject) JSON.parse(db.runCommand(command).toJson());
            /*if (dbStats != null && !dbStats.getOk().toString().equals(OK_RESPONSE)) {
                logger.error("Error retrieving db stats. Invalid permissions set for this user.DB= " + db.getName());
            }*/
        } catch (MongoCommandException e) {
            logger.error("Error while executing " + command + " for db " + db, e);
        }
        return dbObject;
    }

}
