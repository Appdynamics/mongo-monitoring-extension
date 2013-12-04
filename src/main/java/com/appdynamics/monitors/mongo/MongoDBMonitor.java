package com.appdynamics.monitors.mongo;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import javax.naming.AuthenticationException;

import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.apache.log4j.Logger;

import com.appdynamics.monitors.mongo.json.ServerStats;
import com.google.gson.Gson;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class MongoDBMonitor extends AManagedMonitor
{
    private static final String ARG_HOST = "host";
    private static final String ARG_PORT = "port";
    private static final String ARG_USER = "username";
    private static final String ARG_PASS = "password";
    private static final String ARG_DB = "db";
    private static final String ARG_XML_PATH = "properties-path";

    private static final String OK_RESPONSE = "1.0";
    Logger logger = Logger.getLogger(this.getClass().getName());
    private MongoClient mongoClient;
	private ServerStats serverStats;
    private String dbname;
    private List<MongoCredential> credentials;
    private List<DB> dbs = new ArrayList<DB>();
    private String host;
    private String port;

	/**
	 * Main execution method that uploads the metrics to the AppDynamics Controller
	 * @see com.singularity.ee.agent.systemagent.api.ITask#execute(java.util.Map, com.singularity.ee.agent.systemagent.api.TaskExecutionContext)
	 */
	public TaskOutput execute( Map<String, String> params, TaskExecutionContext arg1)
			throws TaskExecutionException
			{
		try
		{
            getCredentials(params);
            connect();

            while(true){
                for (DB db : dbs){
                    try {
                        serverStats = getServerStats(db);
                        dbname = db.getName();

                        printMetric("UP Time (Milliseconds)", serverStats.getUptimeMillis().doubleValue(),
                                MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                                MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
                                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
                        );

                        printGlobalLocksStats();
                        printMemoryStats();
                        printConnectionStats();
                        printIndexCounterStats();
                        printBackgroundFlushingStats();
                        printNetworkStats();
                        printOperationStats();
                        printAssertStats();
                    } catch (Exception e) {
                        logger.error("Failed to get metrics", e);
                    }
                }

                Thread.sleep(60000);
            }
		}
		catch (NullPointerException e){
			logger.error("NullPointerException", e);
		}
		catch (Exception e)
		{
			logger.error("Exception", e);
			return new TaskOutput("Mongo DB Metric Upload Failed." + e.toString());
		}
		finally {
			if(mongoClient != null) {
				mongoClient.close();
			}
		}
		return new TaskOutput("Mongo DB Metric Upload Complete");
	}

    private void getCredentials(final Map<String, String> args){
        credentials = new ArrayList<MongoCredential>();
        MongoCredential cred;

        host = args.get(ARG_HOST);
        port = args.get(ARG_PORT);

        cred = MongoCredential.createMongoCRCredential(
                args.get(ARG_USER),
                args.get(ARG_DB),
                args.get(ARG_PASS).toCharArray());

        credentials.add(cred);

        String xmlPath = args.get(ARG_XML_PATH);
        if (isNotEmpty(xmlPath)) {
            try {
                SAXReader reader = new SAXReader();
                Document doc = reader.read(xmlPath);
                Element root = doc.getRootElement();

                for (Element credElem : (List<Element>) root.elements("credentials")) {
                    if (credElem.elementText(ARG_DB).length() > 0) {
                        cred = MongoCredential.createMongoCRCredential(
                                credElem.elementText(ARG_USER),
                                credElem.elementText(ARG_DB),
                                credElem.elementText(ARG_PASS).toCharArray());
                        credentials.add(cred);
                    }
                }
            } catch (DocumentException e) {
                logger.error("Cannot read '" + xmlPath + "'. Monitor is running without additional credentials");
            }
        }
    }

	/**
	 * Connects to the Mongo DB Server
	 * @throws 				UnknownHostException
	 * @throws 				AuthenticationException
	 */
	public void connect() throws UnknownHostException, AuthenticationException
	{
        mongoClient = new MongoClient(host, Integer.parseInt(port));
        for (MongoCredential cred : credentials){
            DB db = mongoClient.getDB(cred.getSource());
            if ((cred.getUserName() == null && cred.getPassword() == null)
                    || (cred.getUserName().equals("") && cred.getPassword().length == 0)
                    || db.isAuthenticated()
                    || db.authenticate(cred.getUserName(),cred.getPassword())){
                dbs.add(db);
            } else {
                logger.error("User is not allowed to view statistics for database: " + db.getName());
            }
        }
	}

	/**
	 * Returns the metric to the AppDynamics Controller.
	 * @param 	metricName		Name of the Metric
	 * @param 	metricValue		Value of the Metric
	 * @param 	aggregation		Average OR Observation OR Sum
	 * @param 	timeRollup		Average OR Current OR Sum
	 * @param 	cluster			Collective OR Individual
	 */
	public void printMetric(String metricName, double metricValue, String aggregation, String timeRollup, String cluster)
	{
		try{
			MetricWriter metricWriter = getMetricWriter(getMetricPrefix() + metricName,
					aggregation,
					timeRollup,
					cluster
					);

			metricWriter.printMetric(String.valueOf((long) metricValue));
		} catch (NullPointerException e){
			logger.info("NullPointerException: " + e.getMessage());
		}
	}

	/**
	 * Prints the Connection Statistics
	 */
	public void printConnectionStats()
	{
		try{
			printMetric("Connections|Current", serverStats.getConnections().getCurrent().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Connections|Available", serverStats.getConnections().getAvailable().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

		} catch (NullPointerException e){
			logger.info("No information on Connections available");
		}
	}

	/**
	 * Prints the Memory Statistics
	 */
	public void printMemoryStats()
	{
		try{
			printMetric("Memory|Bits", serverStats.getMem().getBits().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Memory|Resident", serverStats.getMem().getResident().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Memory|Virtual", serverStats.getMem().getVirtual().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Memory|Mapped", serverStats.getMem().getMapped().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Memory|Mapped With Journal", serverStats.getMem().getMappedWithJournal().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

		} catch (NullPointerException e){
			logger.info("No information on Memory available");
		}
	}

	/**
	 * Prints the Global Lock Statistics
	 */
	public void printGlobalLocksStats()
	{
		try{
			printMetric("Global Lock|Total Time", serverStats.getGlobalLock().getTotalTime().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Global Lock|Current Queue|Total", serverStats.getGlobalLock().getCurrentQueue().getTotal().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Global Lock|Current Queue|Readers", serverStats.getGlobalLock().getCurrentQueue().getReaders().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Global Lock|Current Queue|Writers", serverStats.getGlobalLock().getCurrentQueue().getWriters().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Global Lock|Active Clients|Total", serverStats.getGlobalLock().getActiveClients().getTotal().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Global Lock|Active Clients|Readers", serverStats.getGlobalLock().getActiveClients().getReaders().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Global Lock|Active Clients|Writers", serverStats.getGlobalLock().getActiveClients().getWriters().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

		} catch (NullPointerException e){
			logger.info("No information on Global Lock available");
		}
	}

	/**
	 * Prints the Index Counter Statistics
	 */
	public void printIndexCounterStats()
	{
		try{
			printMetric("Index Counter|B-Tree|Accesses", serverStats.getIndexCounters().getBtree().getAccesses().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Index Counter|B-Tree|Hits", serverStats.getIndexCounters().getBtree().getHits().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Index Counter|B-Tree|Misses", serverStats.getIndexCounters().getBtree().getMisses().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Index Counter|B-Tree|Resets", serverStats.getIndexCounters().getBtree().getResets().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

		} catch (NullPointerException e){
			logger.info("No information on Index Counter available");
		}
	}

	/**
	 * Prints the Background Flushing Statistics
	 */
	public void printBackgroundFlushingStats()
	{
		try{
			printMetric("Background Flushing|Flushes", serverStats.getBackgroundFlushing().getFlushes().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Background Flushing|Total (ms)", serverStats.getBackgroundFlushing().getTotal_ms().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Background Flushing|Average (ms)", serverStats.getBackgroundFlushing().getAverage_ms().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Background Flushing|Last (ms)", serverStats.getBackgroundFlushing().getLast_ms().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);
		} catch (NullPointerException e){
			logger.info("No information on Background Flushing available");
		}
	}

	/**
	 * Prints the Network Statistics
	 */
	public void printNetworkStats()
	{
		try{
			printMetric("Network|Bytes In", serverStats.getNetwork().getBytesIn().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Network|Bytes Out", serverStats.getNetwork().getBytesOut().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Network|Number Requests", serverStats.getNetwork().getBytesIn().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);
		} catch (NullPointerException e){
			logger.info("No information on Network available");
		}
	}

	/**
	 * Prints the Operation Statistics
	 */
	public void printOperationStats()
	{
		try{
			printMetric("Operations|Insert", serverStats.getOpcounters().getInsert().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Operations|Query", serverStats.getOpcounters().getQuery().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Operations|Update", serverStats.getOpcounters().getUpdate().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Operations|Delete", serverStats.getOpcounters().getDelete().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Operations|Get More", serverStats.getOpcounters().getGetmore().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Operations|Command", serverStats.getOpcounters().getCommand().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);
		} catch (NullPointerException e){
			logger.info("No information on Operations available");
		}
	}

	/**
	 * Prints Assert Statistics
	 */
	public void printAssertStats()
	{
		try{
			printMetric("Asserts|Regular", serverStats.getAsserts().getRegular().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Asserts|Warning", serverStats.getAsserts().getWarning().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Asserts|Message", serverStats.getAsserts().getMsg().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Asserts|User", serverStats.getAsserts().getWarning().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);

			printMetric("Asserts|Rollover", serverStats.getAsserts().getRollovers().doubleValue(),
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
					);
		} catch (NullPointerException e){
			logger.info("No information on Asserts available");
		}
	}

	/**
	 * Metric Prefix
	 * @return	String
	 */
	public String getMetricPrefix()
	{
        return "Custom Metrics|Mongo Server|"  + dbname + "|";
	}

    private static boolean isNotEmpty(final String input) {
        return input != null && input.trim().length() > 0;
    }

    private ServerStats getServerStats(DB db){
        ServerStats serverStats = new Gson().fromJson(db.command("serverStatus").toString().trim(), ServerStats.class);
        if (serverStats != null && !serverStats.getOk().toString().equals(OK_RESPONSE)) {
            logger.error("Server status: " + db.command("serverStatus"));
            logger.error("Error retrieving server status. Invalid permissions set for this user.");
        }
        return serverStats;
    }
}
