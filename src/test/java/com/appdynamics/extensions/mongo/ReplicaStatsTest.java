package com.appdynamics.extensions.mongo;

import com.appdynamics.extensions.AMonitorJob;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.util.PathResolver;
import com.appdynamics.extensions.mongo.input.Stat;
import com.appdynamics.extensions.mongo.stats.ReplicaStats;
import com.appdynamics.extensions.mongo.utils.MongoUtils;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.bson.BSONObject;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Map;
import java.util.concurrent.Phaser;

import static org.mockito.Matchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MongoUtils.class)
@PowerMockIgnore("javax.net.ssl.*")
public class ReplicaStatsTest {

    @Mock
    private TasksExecutionServiceProvider serviceProvider;

    @Mock
    private MetricWriteHelper metricWriter;

    @Mock
    private Phaser phaser;

    private Stat.Stats stat;

    @Mock
    private MongoDatabase adminDB;

    @Mock
    private MongoClient mongoClient;

    private MonitorContextConfiguration contextConfiguration = new MonitorContextConfiguration("Mongo", "Custom Metrics|Mongo|", PathResolver.resolveDirectory(AManagedMonitor.class), PowerMockito.mock(AMonitorJob.class));

    private String metricPrefix = "Custom Metrics|Mongo|";

    private Map<String, String> expectedValueMap;

    private ReplicaStats replicaStats;

    @Before
    public void before(){

        contextConfiguration.setConfigYml("src/test/resources/conf/config.yml");
        contextConfiguration.setMetricXml("src/test/resources/conf/metrics.xml", Stat.Stats.class);

        Mockito.when(serviceProvider.getMetricWriteHelper()).thenReturn(metricWriter);

        stat = (Stat.Stats) contextConfiguration.getMetricsXml();

        replicaStats = Mockito.spy(new ReplicaStats(stat.getStats()[1], adminDB, mongoClient, metricWriter, metricPrefix, phaser));

        PowerMockito.mockStatic(MongoUtils.class);

        PowerMockito.when(MongoUtils.executeMongoCommand(any(MongoDatabase.class), any(Document.class))).thenAnswer(
                new Answer() {
                    public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

                        String response = "{\"set\":\"rs0\",\"date\":1559910961868,\"myState\":1,\"term\":12,\"syncingTo\":\"\",\"syncSourceHost\":\"\",\"syncSourceId\":-1,\"heartbeatIntervalMillis\":2000,\"optimes\":{\"lastCommittedOpTime\":{\"ts\":{\"inc\":1,\"time\":1559910956},\"t\":12},\"readConcernMajorityOpTime\":{\"ts\":{\"inc\":1,\"time\":1559910956},\"t\":12},\"appliedOpTime\":{\"ts\":{\"inc\":1,\"time\":1559910956},\"t\":12},\"durableOpTime\":{\"ts\":{\"inc\":1,\"time\":1559910956},\"t\":12}},\"lastStableCheckpointTimestamp\":{\"inc\":1,\"time\":1559910946},\"members\":[{\"_id\":0,\"name\":\"mongo1:27017\",\"health\":1.0,\"state\":1,\"stateStr\":\"PRIMARY\",\"uptime\":835,\"optime\":{\"ts\":{\"inc\":1,\"time\":1559910956},\"t\":12},\"optimeDate\":1559910956000,\"syncingTo\":\"\",\"syncSourceHost\":\"\",\"syncSourceId\":-1,\"infoMessage\":\"\",\"electionTime\":{\"inc\":1,\"time\":1559910127},\"electionDate\":1559910127000,\"configVersion\":1,\"self\":true,\"lastHeartbeatMessage\":\"\"},{\"_id\":1,\"name\":\"mongo2:27017\",\"health\":1.0,\"state\":2,\"stateStr\":\"SECONDARY\",\"uptime\":499,\"optime\":{\"ts\":{\"inc\":1,\"time\":1559910956},\"t\":12},\n" +
                                "\"optimeDate\":1559910956000,\"syncingTo\":\"mongo1:27017\",\"syncSourceHost\":\"mongo1:27017\",\"syncSourceId\":0,\"infoMessage\":\"\",\"electionTime\" :{\"inc\":1,\"time\":1559910127},\"electionDate\":1559910127000,\"configVersion\":1,\"self\":true,\"lastHeartbeatMessage\":\" +\n" +
                                "\"}],\"ok\":1.0,\"operationTime\":{\"inc\":1,\"time\":1559910956},\"$clusterTime\":{\"clusterTime\":{\"inc\":1,\"time\":1559910956},\"signature\":{\"hash\":\"AAAAAAAAAAAAAAAAAAAAAAAAAAA=\",\"keyId\":0}}}";
                        BSONObject bson = (BSONObject)com.mongodb.util.JSON.parse(response);

                        return BasicDBObject.parse(bson.toString());
                    }
                });
    }

    @Test
    public void testReplicaStats() throws TaskExecutionException {

        expectedValueMap = getExpectedValueMap();
        replicaStats.run();

        validateMetrics();
        Assert.assertTrue("The expected values were not send. The missing values are " + expectedValueMap
                , expectedValueMap.isEmpty());
    }

    private Map<String, String> getExpectedValueMap() {
        Map<String, String> map = Maps.newHashMap();
        map.put("Custom Metrics|Mongo|Replica Stats|mongo2:27017|Health","1.0");
        map.put("Custom Metrics|Mongo|Replica Stats|mongo2:27017|Uptime","499");
        map.put("Custom Metrics|Mongo|Replica Stats|mongo2:27017|State","2");
        map.put("Custom Metrics|Mongo|Replica Stats|mongo1:27017|Health","1.0");
        map.put("Custom Metrics|Mongo|Replica Stats|mongo1:27017|Uptime","835");
        map.put("Custom Metrics|Mongo|Replica Stats|mongo1:27017|State","1");
        map.put("Custom Metrics|Mongo|Replica Stats|Primary Elected", "1");

        return map;
    }

    private void validateMetrics(){
        for(Metric metric: replicaStats.getMetrics()) {

            String actualValue = metric.getMetricValue();
            String metricName = metric.getMetricPath();
            if (expectedValueMap.containsKey(metricName)) {
                String expectedValue = expectedValueMap.get(metricName);
                Assert.assertEquals("The value of the metric " + metricName + " failed", expectedValue, actualValue);
                expectedValueMap.remove(metricName);
            } else {
                System.out.println("\"" + metricName + "\",\"" + actualValue + "\"");
                Assert.fail("Unknown Metric " + metricName);
            }
        }
    }
}
