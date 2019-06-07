package com.appdynamics.extensions.mongo;

import com.appdynamics.extensions.AMonitorJob;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.util.PathResolver;
import com.appdynamics.monitors.mongo.input.Stat;
import com.appdynamics.monitors.mongo.stats.DBStats;
import com.appdynamics.monitors.mongo.utils.MongoUtils;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
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
public class DBStatsTest {

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

    private DBStats dbStats;

    @Before
    public void before(){

        contextConfiguration.setConfigYml("src/test/resources/conf/config.yml");
        contextConfiguration.setMetricXml("src/test/resources/conf/metrics.xml", Stat.Stats.class);

        Mockito.when(serviceProvider.getMetricWriteHelper()).thenReturn(metricWriter);

        stat = (Stat.Stats) contextConfiguration.getMetricsXml();

        dbStats = Mockito.spy(new DBStats(stat.getStats()[2], adminDB, mongoClient, metricWriter,  metricPrefix, phaser));

        PowerMockito.mockStatic(MongoUtils.class);

        MongoIterable<String> iterableMock = Mockito.mock(MongoIterable.class);
        MongoCursor<String> iteratorMock = Mockito.mock(MongoCursor.class);
        Mockito.when(iterableMock.iterator()).thenReturn(iteratorMock);
        Mockito.when(iteratorMock.hasNext()).thenReturn(true).thenReturn(false);
        Mockito.when(iteratorMock.next()).thenReturn("admin");
        Mockito.when(mongoClient.listDatabaseNames()).thenReturn(iterableMock);

        PowerMockito.when(MongoUtils.executeMongoCommand(any(MongoDatabase.class), any(Document.class))).thenAnswer(
                new Answer() {
                    public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                        String response = "{ \"db\" : \"admin\", \"collections\" : 2, \"views\" : 0, \"objects\" : 3, \"avgObjSize\" : 76.333," +
                                " \"dataSize\" : 229.0, \"storageSize\" : 32768.0, \"numExtents\" : 0, \"indexes\" : 2, \"indexSize\" : 32768.0, " +
                                "\"fsUsedSize\" : 4.822, \"fsTotalSize\" : 5, \"ok\" : 1.0, " +
                                "\"operationTime\" : { \"$timestamp\" : { \"t\" : 1559843368, \"i\" : 1 } }, " +
                                "\"$clusterTime\" : { \"clusterTime\" : { \"$timestamp\" : { \"t\" : 1559843368, \"i\" : 1 } }," +
                                " \"signature\" : { \"hash\" : { \"$binary\" : \"AAAAAAAAAAAAAAAAAAAAAAAAAAA=\", \"$type\" : \"00\" }, " +
                                "\"keyId\" : { \"$numberLong\" : \"0\" } } } }";

                        BSONObject bson = (BSONObject)com.mongodb.util.JSON.parse(response);

                        return BasicDBObject.parse(bson.toString());
                    }
                });
    }

    @Test
    public void testDBStats() throws TaskExecutionException {

        expectedValueMap = getExpectedValueMap();
        dbStats.run();

        validateMetrics();
        Assert.assertTrue("The expected values were not send. The missing values are " + expectedValueMap
                , expectedValueMap.isEmpty());
    }

    private Map<String, String> getExpectedValueMap() {
        Map<String, String> map = Maps.newHashMap();
        map.put("Custom Metrics|Mongo|DB Stats|admin|collections","2");
        map.put("Custom Metrics|Mongo|DB Stats|admin|views","0");
        map.put("Custom Metrics|Mongo|DB Stats|admin|objects","3");
        map.put("Custom Metrics|Mongo|DB Stats|admin|avgObjSize","76.333");
        map.put("Custom Metrics|Mongo|DB Stats|admin|dataSize","229.0");
        map.put("Custom Metrics|Mongo|DB Stats|admin|storageSize","32768.0");
        map.put("Custom Metrics|Mongo|DB Stats|admin|numExtents","0");
        map.put("Custom Metrics|Mongo|DB Stats|admin|indexes","2");
        map.put("Custom Metrics|Mongo|DB Stats|admin|indexSize","32768.0");
        map.put("Custom Metrics|Mongo|DB Stats|admin|fsUsedSize","4.822");
        map.put("Custom Metrics|Mongo|DB Stats|admin|fsTotalSize","5");
        map.put("Custom Metrics|Mongo|DB Stats|admin|ok","1.0");
        return map;
    }

    private void validateMetrics(){
        for(Metric metric: dbStats.getMetrics()) {

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