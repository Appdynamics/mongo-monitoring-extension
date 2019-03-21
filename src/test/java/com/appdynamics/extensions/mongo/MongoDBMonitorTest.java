/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.mongo;

import com.appdynamics.monitors.mongo.MongoDBMonitor;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by balakrishnavadavalasa on 03/05/16.
 */
public class MongoDBMonitorTest {

    @Test
    public void testMongoDBMonitor() throws TaskExecutionException {
        Map<String, String> taskArgs = new HashMap<String, String>();
        taskArgs.put("config-file", "src/test/resources/conf/config_old.yml");

        MongoDBMonitor monitor = new MongoDBMonitor();
        monitor.execute(taskArgs, null);
    }
}
