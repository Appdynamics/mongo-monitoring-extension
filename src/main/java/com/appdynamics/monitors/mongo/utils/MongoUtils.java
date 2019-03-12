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
