package com.appdynamics.monitors.mongo;

import java.net.UnknownHostException;
import java.util.Map;

import javax.naming.AuthenticationException;

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

public class MongoDBMonitor extends AManagedMonitor
{
	Logger logger = Logger.getLogger(this.getClass().getName());
	private MongoClient mongoClient;
	private ServerStats serverStats;
	private DB db;

	/**
	 * Connects to the Mongo DB Server
	 * @param	host		Hostname of the Mongo DB Server				
	 * @param 	port		Port of the Mongo DB Server
	 * @throws 				UnknownHostException
	 * @throws 				AuthenticationException
	 */
	public void connect( String host, String port, String username, String password, String db) throws UnknownHostException, AuthenticationException
	{
		if (mongoClient == null)
		{
			mongoClient = new MongoClient(host, Integer.parseInt(port));
		}

		this.db = mongoClient.getDB(db);

		if ((username.isEmpty() && password.isEmpty()) || this.db.authenticate(username, password.toCharArray()))
		{
			return;
		}
		
		throw new AuthenticationException("User is not allowed to view statistics for database: " + db);
	}

	/**
	 * Populates the data from Mongo DB Server
	 */
	public void populate() 
	{
		serverStats = new Gson().fromJson(db.command("serverStatus").toString().trim(), ServerStats.class);
	}

	/**
	 * Main execution method that uploads the metrics to the AppDynamics Controller
	 * @see com.singularity.ee.agent.systemagent.api.ITask#execute(java.util.Map, com.singularity.ee.agent.systemagent.api.TaskExecutionContext)
	 */
	public TaskOutput execute( Map<String, String> params, TaskExecutionContext arg1)
			throws TaskExecutionException
	{
		try 
		{
			String host = params.get("host");
			String port = params.get("port");
			String username = params.get("username");
			String password = params.get("password");
			String db = params.get("db");

			connect(host, port, username, password, db);
			populate();

			printMetric("UP Time (Milliseconds)", serverStats.getUptimeMillis().longValue(),
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
		}
		catch (Exception e)
		{
			logger.error(e);
			return new TaskOutput("Mongo DB Metric Upload Failed." + e.toString());
		}
		return new TaskOutput("Mongo DB Metric Upload Complete");
	}

	/**
	 * Returns the metric to the AppDynamics Controller.
	 * @param 	metricName		Name of the Metric
	 * @param 	metricValue		Value of the Metric
	 * @param 	aggregation		Average OR Observation OR Sum
	 * @param 	timeRollup		Average OR Current OR Sum
	 * @param 	cluster			Collective OR Individual
	 */
	public void printMetric(String metricName, Object metricValue, String aggregation, String timeRollup, String cluster)
	{
		MetricWriter metricWriter = getMetricWriter(getMetricPrefix() + metricName, 
			aggregation,
			timeRollup,
			cluster
		);

		metricWriter.printMetric(String.valueOf(metricValue));
	}
	
	/**
	 * Prints the Connection Statistics
	 */
	public void printConnectionStats() 
	{
		printMetric("Connections|Current", serverStats.getConnections().getCurrent().intValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);
		
		printMetric("Connections|Available", serverStats.getConnections().getAvailable().intValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);
	}

	/**
	 * Prints the Memory Statistics
	 */
	public void printMemoryStats() 
	{
		printMetric("Memory|Bits", serverStats.getMem().getBits().longValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);
	
		printMetric("Memory|Resident", serverStats.getMem().getResident().longValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);
	
		printMetric("Memory|Virtual", serverStats.getMem().getVirtual().longValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);
		
		printMetric("Memory|Mapped", serverStats.getMem().getMapped().longValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);
		
		printMetric("Memory|Mapped With Journal", serverStats.getMem().getMappedWithJournal().longValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);
	}

	/**
	 * Prints the Global Lock Statistics
	 */
	public void printGlobalLocksStats() 
	{
		printMetric("Global Lock|Total Time", serverStats.getGlobalLock().getTotalTime().longValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);

		printMetric("Global Lock|Current Queue|Total", serverStats.getGlobalLock().getCurrentQueue().getTotal().longValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);

		printMetric("Global Lock|Current Queue|Readers", serverStats.getGlobalLock().getCurrentQueue().getReaders().intValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);

		printMetric("Global Lock|Current Queue|Writers", serverStats.getGlobalLock().getCurrentQueue().getWriters().intValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);

		printMetric("Global Lock|Active Clients|Total", serverStats.getGlobalLock().getActiveClients().getTotal().intValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);

		printMetric("Global Lock|Active Clients|Readers", serverStats.getGlobalLock().getActiveClients().getReaders().intValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);

		printMetric("Global Lock|Active Clients|Writers", serverStats.getGlobalLock().getActiveClients().getWriters().intValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);
	}

	/**
	 * Prints the Index Counter Statistics
	 */
	public void printIndexCounterStats() 
	{
		printMetric("Index Counter|B-Tree|Accesses", serverStats.getIndexCounters().getBtree().getAccesses().intValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);
		
		printMetric("Index Counter|B-Tree|Hits", serverStats.getIndexCounters().getBtree().getHits().intValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);

		printMetric("Index Counter|B-Tree|Misses", serverStats.getIndexCounters().getBtree().getMisses().intValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);
		
		printMetric("Index Counter|B-Tree|Resets", serverStats.getIndexCounters().getBtree().getResets().intValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);
		
		printMetric("Background Flushing|Flushes", serverStats.getBackgroundFlushing().getFlushes().intValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);
	}

	/**
	 * Prints the Background Flushing Statistics
	 */
	public void printBackgroundFlushingStats()
	{
		printMetric("Background Flushing|Flushes", serverStats.getBackgroundFlushing().getFlushes().intValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);

		printMetric("Background Flushing|Total (ms)", serverStats.getBackgroundFlushing().getTotal_ms().longValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);

		printMetric("Background Flushing|Average (ms)", serverStats.getBackgroundFlushing().getAverage_ms().longValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);

		printMetric("Background Flushing|Last (ms)", serverStats.getBackgroundFlushing().getLast_ms().longValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);
	}

	/**
	 * Prints the Network Statistics
	 */
	public void printNetworkStats() 
	{
		printMetric("Network|Bytes In", serverStats.getNetwork().getBytesIn().longValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);

		printMetric("Network|Bytes Out", serverStats.getNetwork().getBytesOut().longValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);

		printMetric("Network|Number Requests", serverStats.getNetwork().getBytesIn().intValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);
	}

	/**
	 * Prints the Operation Statistics
	 */
	public void printOperationStats()
	{
		printMetric("Operations|Insert", serverStats.getOpcounters().getInsert().intValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);

		printMetric("Operations|Query", serverStats.getOpcounters().getQuery().intValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);
		
		printMetric("Operations|Update", serverStats.getOpcounters().getUpdate().intValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);
		
		printMetric("Operations|Delete", serverStats.getOpcounters().getDelete().intValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);
		
		printMetric("Operations|Get More", serverStats.getOpcounters().getGetmore().intValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);
		
		printMetric("Operations|Command", serverStats.getOpcounters().getCommand().intValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);
	}

	/**
	 * Prints Assert Statistics
	 */
	public void printAssertStats()
	{
		printMetric("Asserts|Regular", serverStats.getAsserts().getRegular().intValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);

		printMetric("Asserts|Warning", serverStats.getAsserts().getWarning().intValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);
		
		printMetric("Asserts|Message", serverStats.getAsserts().getMsg().intValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);
		
		printMetric("Asserts|User", serverStats.getAsserts().getWarning().intValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);
		
		printMetric("Asserts|Rollover", serverStats.getAsserts().getRollovers().intValue(),
			MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
			MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
			MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
		);
	}

	/**
	 * Metric Prefix
	 * @return	String
	 */
	public String getMetricPrefix()
	{
		return "Custom Metrics|Mongo Server|Status|";
	}
}
