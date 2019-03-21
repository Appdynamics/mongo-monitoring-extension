/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.mongo.exception;

/**
 * Created by balakrishnav on 5/8/15.
 */
public class MongoMonitorException extends Exception {

    public MongoMonitorException() {
    }

    public MongoMonitorException(String message) {
        super(message);
    }

    public MongoMonitorException(String message, Throwable cause) {
        super(message, cause);
    }

    public MongoMonitorException(Throwable cause) {
        super(cause);
    }
}
