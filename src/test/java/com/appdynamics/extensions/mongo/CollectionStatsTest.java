package com.appdynamics.extensions.mongo;

import com.appdynamics.extensions.AMonitorJob;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.util.PathResolver;
import com.appdynamics.extensions.mongo.input.Stat;
import com.appdynamics.extensions.mongo.stats.CollectionStats;
import com.appdynamics.extensions.mongo.utils.MongoUtils;
import com.google.common.collect.Maps;
import com.google.gson.JsonParser;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
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

import java.io.FileReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Phaser;

import static org.mockito.Matchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MongoUtils.class)
@PowerMockIgnore("javax.net.ssl.*")
public class CollectionStatsTest {


    @Mock
    private TasksExecutionServiceProvider serviceProvider;

    @Mock
    private MetricWriteHelper metricWriter;

    @Mock
    private Phaser phaser;

    private Stat.Stats stat;

    @Mock
    private DB db;

    @Mock
    private MongoClient mongoClient;

    @Mock
    private DBCollection dbCollection;

    @Mock
    private CommandResult commandResult;

    private MonitorContextConfiguration contextConfiguration = new MonitorContextConfiguration("Mongo", "Custom Metrics|Mongo|", PathResolver.resolveDirectory(AManagedMonitor.class), PowerMockito.mock(AMonitorJob.class));

    private String metricPrefix = "Custom Metrics|Mongo|";

    private Map<String, String> expectedValueMap;

    private CollectionStats collectionStats;

    @Before
    public void before(){

        contextConfiguration.setConfigYml("src/test/resources/conf/config.yml");
        contextConfiguration.setMetricXml("src/test/resources/conf/metrics.xml", Stat.Stats.class);

        Mockito.when(serviceProvider.getMetricWriteHelper()).thenReturn(metricWriter);

        stat = (Stat.Stats) contextConfiguration.getMetricsXml();

        collectionStats = Mockito.spy(new CollectionStats(stat.getStats()[3], mongoClient, metricWriter,  metricPrefix, phaser));

        PowerMockito.mockStatic(MongoUtils.class);

        MongoIterable<String> iterableMock = Mockito.mock(MongoIterable.class);
        MongoCursor<String> iteratorMock = Mockito.mock(MongoCursor.class);
        Mockito.when(iterableMock.iterator()).thenReturn(iteratorMock);
        Mockito.when(iteratorMock.hasNext()).thenReturn(true).thenReturn(false);
        Mockito.when(iteratorMock.next()).thenReturn("admin");
        Mockito.when(mongoClient.listDatabaseNames()).thenReturn(iterableMock);
        Mockito.when(mongoClient.getDB(any(String.class))).thenReturn(db);
        Set<String> dbs = new HashSet<>();
        dbs.add("admin");
        Mockito.when(db.getCollectionNames()).thenReturn(dbs);
        Mockito.when(db.getCollection("admin")).thenReturn(dbCollection);
        Mockito.when(dbCollection.getStats()).thenReturn(commandResult);

        PowerMockito.when(commandResult.toString()).thenAnswer(
                new Answer() {
                    public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

                        JsonParser parser = new JsonParser();
                        Object obj = parser.parse(new FileReader(
                                "src/test/resources/conf/collectionStats.txt"));



                        //BsonDocument bson = (BsonDocument)com.mongodb.util.JSON.parse(obj.toString());
                        /*BasicBSONEncoder encoder = new BasicBSONEncoder();
                        byte[] bson_byte = encoder.encode(bson);*/

                        return obj.toString();
                    }
                });
    }

    @Test
    public void testCollectionStats() throws TaskExecutionException {

        expectedValueMap = getExpectedValueMap();
        collectionStats.run();

        validateMetrics();
        Assert.assertTrue("The expected values were not send. The missing values are " + expectedValueMap
                , expectedValueMap.isEmpty());
    }

    private Map<String, String> getExpectedValueMap() {
        Map<String, String> map = Maps.newHashMap();
        map.put("Custom Metrics|Mongo|DB Stats|admin|Collection Stats|admin|count","2");
        map.put("Custom Metrics|Mongo|DB Stats|admin|Collection Stats|admin|nindexes","1");
        map.put("Custom Metrics|Mongo|DB Stats|admin|Collection Stats|admin|size","170");
        map.put("Custom Metrics|Mongo|DB Stats|admin|Collection Stats|admin|storageSize","16384");
        map.put("Custom Metrics|Mongo|DB Stats|admin|Collection Stats|admin|totalIndexSize","16384");
        return map;
    }

    private void validateMetrics(){
        for(Metric metric: collectionStats.getMetrics()) {

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
