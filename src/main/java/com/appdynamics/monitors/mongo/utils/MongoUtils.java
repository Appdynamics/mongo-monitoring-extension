/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.mongo.utils;

/**
 * Created by bhuvnesh.kumar on 3/12/19.
 */
public class MongoUtils {
    public static String convertToString(final Object field, final String defaultStr) {
        if (field == null) {
            return defaultStr;
        }
        return field.toString();
    }


}
