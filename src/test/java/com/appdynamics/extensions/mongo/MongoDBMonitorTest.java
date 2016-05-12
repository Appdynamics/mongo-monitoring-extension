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
        taskArgs.put("config-file", "src/test/resources/conf/config.yml");

        MongoDBMonitor monitor = new MongoDBMonitor();
        monitor.execute(taskArgs, null);
    }
}
