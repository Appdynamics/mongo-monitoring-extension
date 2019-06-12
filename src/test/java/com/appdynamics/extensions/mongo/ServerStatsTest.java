package com.appdynamics.extensions.mongo;

import com.appdynamics.extensions.AMonitorJob;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.mongo.input.Stat;
import com.appdynamics.extensions.mongo.stats.ServerStats;
import com.appdynamics.extensions.mongo.utils.MongoUtils;
import com.appdynamics.extensions.util.PathResolver;
import com.google.common.collect.Maps;
import com.google.gson.JsonParser;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Phaser;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MongoUtils.class)
@PowerMockIgnore("javax.net.ssl.*")
public class ServerStatsTest {

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

    private ServerStats serverStats;

    @Before
    public void before(){

        contextConfiguration.setConfigYml("src/test/resources/conf/config.yml");
        contextConfiguration.setMetricXml("src/test/resources/conf/metrics.xml", Stat.Stats.class);

        Mockito.when(serviceProvider.getMetricWriteHelper()).thenReturn(metricWriter);

        stat = (Stat.Stats) contextConfiguration.getMetricsXml();

        serverStats = Mockito.spy(new ServerStats(stat.getStats()[0], adminDB, metricWriter, metricPrefix, phaser));

        PowerMockito.mockStatic(MongoUtils.class);

        PowerMockito.when(MongoUtils.executeMongoCommand(any(MongoDatabase.class), any(Document.class))).thenAnswer(
                new Answer() {
                    public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

                        JsonParser parser = new JsonParser();
                        Object obj = parser.parse(new FileReader(
                                "src/test/resources/conf/serverStat.json"));


                        BSONObject bson = (BSONObject)com.mongodb.util.JSON.parse(obj.toString());
                        return BasicDBObject.parse(bson.toString());
                    }
                });
    }

    @Test
    public void testServerStats() throws TaskExecutionException {

        expectedValueMap = getExpectedValueMap();
        serverStats.run();
        ArgumentCaptor<List> pathCaptor = ArgumentCaptor.forClass(List.class);
        verify(metricWriter).transformAndPrintMetrics(pathCaptor.capture());

        for(Metric metric: (List<Metric>)pathCaptor.getValue()) {

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
        Assert.assertTrue("The expected values were not send. The missing values are " + expectedValueMap
                , expectedValueMap.isEmpty());
    }

    private Map<String, String> getExpectedValueMap() {
        Map<String, String> map = Maps.newHashMap();
        map.put("Custom Metrics|Mongo|Server Stats|Process ID","3999");
        map.put("Custom Metrics|Mongo|Server Stats|Uptime","259.0");
        map.put("Custom Metrics|Mongo|Server Stats|Uptime Estimate","259");
        map.put("Custom Metrics|Mongo|Server Stats|Uptime(in millisec)","259367");
        map.put("Custom Metrics|Mongo|Server Stats|Ok","1.0");
        map.put("Custom Metrics|Mongo|Server Stats|asserts|message","0");
        map.put("Custom Metrics|Mongo|Server Stats|asserts|regular","0");
        map.put("Custom Metrics|Mongo|Server Stats|asserts|warning","0");
        map.put("Custom Metrics|Mongo|Server Stats|asserts|user","2");
        map.put("Custom Metrics|Mongo|Server Stats|asserts|rollovers","0");
        map.put("Custom Metrics|Mongo|Server Stats|connections|current","3");
        map.put("Custom Metrics|Mongo|Server Stats|connections|available","201");
        map.put("Custom Metrics|Mongo|Server Stats|connections|totalCreated","7");
        map.put("Custom Metrics|Mongo|Server Stats|global lock|totalTime","259361000");
        map.put("Custom Metrics|Mongo|Server Stats|global lock|active clients|total","42");
        map.put("Custom Metrics|Mongo|Server Stats|global lock|active clients|readers","0");
        map.put("Custom Metrics|Mongo|Server Stats|global lock|active clients|writers","0");
        map.put("Custom Metrics|Mongo|Server Stats|global lock|current queue|total","0");
        map.put("Custom Metrics|Mongo|Server Stats|global lock|current queue|readers","0");
        map.put("Custom Metrics|Mongo|Server Stats|global lock|current queue|writers","0");
        map.put("Custom Metrics|Mongo|Server Stats|network|bytesIn","5844");
        map.put("Custom Metrics|Mongo|Server Stats|network|bytesOut","119475");
        map.put("Custom Metrics|Mongo|Server Stats|network|number Requests","40");
        map.put("Custom Metrics|Mongo|Server Stats|operations|insert","0");
        map.put("Custom Metrics|Mongo|Server Stats|operations|query","4");
        map.put("Custom Metrics|Mongo|Server Stats|operations|update","0");
        map.put("Custom Metrics|Mongo|Server Stats|operations|delete","0");
        map.put("Custom Metrics|Mongo|Server Stats|operations|getmore","0");
        map.put("Custom Metrics|Mongo|Server Stats|operations|command","41");
        map.put("Custom Metrics|Mongo|Server Stats|memory|bits","64");
        map.put("Custom Metrics|Mongo|Server Stats|memory|resident","36");
        map.put("Custom Metrics|Mongo|Server Stats|memory|virtual","5040");
        map.put("Custom Metrics|Mongo|Server Stats|memory|mapped","0");
        map.put("Custom Metrics|Mongo|Server Stats|memory|mappedWithJournal","0");
        return map;
    }

}
