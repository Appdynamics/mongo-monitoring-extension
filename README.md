# AppDynamics MongoDB Monitoring Extension

##Use Case

The MongoDB custom monitor captures statistics from the MongoDB server and displays them in the AppDynamics Metric Browser.

This extension works only with the standalone machine agent.

Metrics include:

* Server up time
* Global lock time
* Operations currently queued, number waiting for the read-lock or write-lock
* Total active connections, number of read and write operations
* Memory metrics including bits, resident RAM, virtual memory, mapped memory, mapped memory with journaling
* Current and available connections
* Index counters including index access, hits and misses, resets
* Background flushing metrics such as number of times, total time, average time, last time
* Network traffic sent and received (in bytes), number of distinct requests received
* Number of database operations including: insert, query, update, delete, get more, and total number of commands
* Number of asserts since the server process started: regular, warnings, message, user, and number of times the rollover counter has rolled
* Database related stats
* Cluster related stats

##Installation

1. Run 'ant package' from the mongo-monitoring-extension directory
2. Download the file MongoMonitor.zip found in the 'dist' directory into \<machineagent install dir\>/monitors/
3. Unzip the downloaded file
4. In the newly created directory "MongoMonitor", edit the monitor.xml configuring the parameters specified below.
5. If there are additional DB to be monitored, add the credentials to properties.xml
5. Restart the machineagent
6. In the AppDynamics Metric Browser, look for: Application Infrastructure Performance  | \<Tier\> | Custom Metrics | Mongo Server | \<DB\>.

##Rebuilding the Project

1.  At the command line, go to the root directory (where all the files are located).
2.  Type "ant" (without the quotes) and press Return.

    'dist' will be updated with the monitor.xml and mongo.jar

##Configuration

<table><tbody>
<tr>
<th align="left"> Parameter </th>
<th align="left"> Description </th>
</tr>
<tr>
<td class='confluenceTd'> host </td>
<td class='confluenceTd'> Mongo DB host </td>
</tr>
<tr>
<td class='confluenceTd'> port </td>
<td class='confluenceTd'> Mongo DB port </td>
</tr>
<tr>
<td class='confluenceTd'> username </td>
<td class='confluenceTd'> Username with cluster admin role to access mongo db server status </td>
</tr>
<tr>
<td class='confluenceTd'> password </td>
<td class='confluenceTd'> Password to access mongo db </td>
</tr>
</tbody>
</table>

#####Note: Ensure that the user has appropriate permissions to the database! Otherwise, metrics will not be displayed in the AppDynamics Metric Browser.

###Example Monitor XML

```

<monitor>
        <name>Mongo DBMonitor</name>
        <type>managed</type>
        <description>Mongo DB server monitor</description>
        <monitor-configuration></monitor-configuration>
        <monitor-run-task>
                <execution-style>periodic</execution-style>
                <execution-frequency-in-seconds>60</execution-frequency-in-seconds>
                <name>Mongo DB Monitor Run Task</name>
                <display-name>Mongo DB Monitor Task</display-name>
                <description>Mongo DB Monitor Task</description>
                <type>java</type>
                <execution-timeout-in-secs>60</execution-timeout-in-secs>
                <task-arguments>
                        <argument name="host" is-required="true" default-value="localhost" />
                        <argument name="port" is-required="true" default-value="27017" />
                        <argument name="username" is-required="true" default-value="admin" />
                        <argument name="password" is-required="true" default-value="admin" />

                        <!-- Additional MongoDB credentials (OPTIONAL)
                                Additional MongoDB credentials can be placed in properties.xml
                        -->
                        <argument name="properties-path" is-required="false" default-value="monitors/MongoMonitor/properties.xml" />
                </task-arguments>
                <java-task>
                    <classpath>MongoMonitor.jar;lib/gson-2.2.2.jar;lib/mongo-java-driver-2.11.3.jar;lib/dom4j-1.6.1.jar</classpath>
                        <impl-class>com.appdynamics.monitors.mongo.MongoDBMonitor</impl-class>
                </java-task>
        </monitor-run-task>
</monitor>


```

##Directory Structure

<table><tbody>
<tr>
<th align="left"> File/Folder </th>
<th align="left"> Description </th>
</tr>
<tr>
<td class='confluenceTd'> conf </td>
<td class='confluenceTd'> Contains the monitor.xml and properties.xml </td>
</tr>
<tr>
<td class='confluenceTd'> lib </td>
<td class='confluenceTd'> Contains third-party project references </td>
</tr>
<tr>
<td class='confluenceTd'> src </td>
<td class='confluenceTd'> Contains source code to Mongo DB Custom Monitor </td>
</tr>
<tr>
<td class='confluenceTd'> dist </td>
<td class='confluenceTd'> Only obtained when using ant. Run 'ant build' to get binaries. Run 'ant package' to get the distributable .zip file. </td>
</tr>
<tr>
<td class='confluenceTd'> build.xml </td>
<td class='confluenceTd'> Ant build script to package the project (required only if changing Java code) </td>
</tr>
</tbody>
</table>



*Main Java File*: **src/com/appdynamics/monitors/mongo/MongoDBMonitor.java**  -\> This file contains the metric parsing and printing.


##Metrics

###	Server Stats

<table><tbody>
<tr>
<th align="left"> Metric Name </th>
<th align="left"> Description </th>
</tr>
<tr>
<td class='confluenceTd'> Up Time (ms) </td>
<td class='confluenceTd'> The duration of time that the server is up </td>
</tr>
</tbody>
</table>

####Metric Category: Asserts

<table><tbody>
<tr>
<th align="left"> Metric Name </th>
<th align="left"> Description </th>
</tr>
<tr>
<td class='confluenceTd'> Message </td>
<td class='confluenceTd'>  </td>
</tr>
<tr>
<td class='confluenceTd'> Regular </td>
<td class='confluenceTd'>  </td>
</tr>
<tr>
<td class='confluenceTd'> Rollover </td>
<td class='confluenceTd'>  </td>
</tr>
<tr>
<td class='confluenceTd'> User </td>
<td class='confluenceTd'>  </td>
</tr>
<tr>
<td class='confluenceTd'> Warning </td>
<td class='confluenceTd'>  </td>
</tr>
</tbody>
</table>

####Metric Category: Background Flushing 

<table><tbody>
<tr>
<th align="left"> Metric Name </th>
<th align="left"> Description </th>
</tr>
<tr>
<td class='confluenceTd'> Flushes </td>
<td class='confluenceTd'> Number of times the database has been flushed </td>
</tr>
<tr>
<td class='confluenceTd'> Total (ms) </td>
<td class='confluenceTd'> Total time (ms) that the mongod process spent writing data to disk </td>
</tr>
<tr>
<td class='confluenceTd'> Average (ms) </td>
<td class='confluenceTd'> Average time (ms) that the mongod process spent writing data to disk </td>
</tr>
<tr>
<td class='confluenceTd'> Last (ms) </td>
<td class='confluenceTd'> Time (ms) that the mongod process last spent writing data to disk </td>
</tr>
</tbody>
</table>

####Metric Category: Connections

<table><tbody>
<tr>
<th align="left"> Metric Name </th>
<th align="left"> Description </th>
</tr>
<tr>
<td class='confluenceTd'> Current </td>
<td class='confluenceTd'> Number of current connections to the database server from clients.
This number includes the current shell connection as well as any inter-node connections to support
a replica set or sharded cluster.</td>
</tr>
<tr>
<td class='confluenceTd'> Available </td>
<td class='confluenceTd'> Number of unused available connections that the database can provide.
Consider this value in combination with the value of Current to understand the connection load on the database.</td>
</tr>
</tbody>
</table>


####Metric Category: Global Lock
 
 <table>
 <tbody>
 <tr>
 <th align="left"> Metric Name </th>
 <th align="left"> Description </th>
 </tr>
 <tr>
  <th align="left"> Total Time </th>
  <th align="left"> The total time since the globalLock was started and created </th>
  </tr>
 </tbody>
 </table>
 
#####Active Clients
 <table><tbody>
 <tr>
 <th align="left"> Metric Name </th>
 <th align="left"> Description </th>
 </tr>
 <tr>
 <td class='confluenceTd'> Total </td>
 <td class='confluenceTd'> Number of active client connections to the database </td>
 </tr>
 <tr>
 <td class='confluenceTd'> Readers </td>
 <td class='confluenceTd'> Number of readers performing read operations </td>
 </tr>
 <tr>
 <td class='confluenceTd'> Writers </td>
 <td class='confluenceTd'> Number of writers performing write operations </td>
 </tr>
 </tbody>
 </table>

#####Current Queue

<table><tbody>
<tr>
<th align="left"> Metric Name </th>
<th align="left"> Description </th>
</tr>
<tr>
<td class='confluenceTd'> Total </td>
<td class='confluenceTd'> Number of operations queued before a lock </td>
</tr>
<tr>
<td class='confluenceTd'> Readers </td>
<td class='confluenceTd'> Number of operations waiting for the read-lock </td>
</tr>
<tr>
<td class='confluenceTd'> Writers </td>
<td class='confluenceTd'> Number of operations waiting for the write-lock </td>
</tr>
</tbody>
</table>

####Metric Category: Memory

<table><tbody>
<tr>
<th align="left"> Metric Name </th>
<th align="left"> Description </th>
</tr>
<tr>
<td class='confluenceTd'> Bits </td>
<td class='confluenceTd'> Target Architecture </td>
</tr>
<tr>
<td class='confluenceTd'> Resident </td>
<td class='confluenceTd'> Amount of RAM (MB) currently used by the database process </td>
</tr>
<tr>
<td class='confluenceTd'> Virtual </td>
<td class='confluenceTd'> MB currently used by the mongod process </td>
</tr>
<tr>
<td class='confluenceTd'> Mapped </td>
<td class='confluenceTd'> Amount of mapped memory (MB) used by the database </td>
</tr>
<tr>
<td class='confluenceTd'> Mapped With Journal </td>
<td class='confluenceTd'> Amount of mapped memory (MB), including memory used for journaling. </td>
</tr>
</tbody>
</table>


####Metric Category: Network
<table><tbody>
<tr>
<th align="left"> Metric Name </th>
<th align="left"> Description </th>
</tr>
<tr>
<td class='confluenceTd'> Bytes In </td>
<td class='confluenceTd'> The amount of network traffic (bytes) received by the database </td>
</tr>
<tr>
<td class='confluenceTd'> Bytes Out </td>
<td class='confluenceTd'> The amount of network traffic (bytes) sent from the database </td>
</tr>
<tr>
<td class='confluenceTd'> Number Requests </td>
<td class='confluenceTd'> Number of distinct requests that the server has received </td>
</tr>
</tbody>
</table>

####Metric Category: Operations
 <table><tbody>
 <tr>
 <th align="left"> Metric Name </th>
 <th align="left"> Description </th>
 </tr>
 <tr>
 <td class='confluenceTd'> Insert </td>
 <td class='confluenceTd'> Number of insert operations </td>
 </tr>
 <tr>
 <td class='confluenceTd'> Query </td>
 <td class='confluenceTd'> Number of query operations </td>
 </tr>
 <tr>
 <td class='confluenceTd'> Update </td>
 <td class='confluenceTd'> Number of update operations </td>
 </tr>
 <tr>
 <td class='confluenceTd'> Delete </td>
 <td class='confluenceTd'> Number of delete operations </td>
 </tr>
 <tr>
 <td class='confluenceTd'> GetMore </td>
 <td class='confluenceTd'> Number of getmore operations </td>
 </tr>
 <tr>
 <td class='confluenceTd'> Command </td>
 <td class='confluenceTd'> Total number of commands issued to database </td>
 </tr>
 </tbody>
 </table>

###	DB Stats

####<DB Name>
<table><tbody>
 <tr>
 <th align="left"> Metric Name </th>
 <th align="left"> Description </th>
 </tr>
 <tr>
 <td class='confluenceTd'> avgObjSize </td>
 <td class='confluenceTd'> The average size of each document in bytes. This is the dataSize divided by the number of documents </td>
 </tr>
 <tr>
 <td class='confluenceTd'> collections </td>
 <td class='confluenceTd'> Contains a count of the number of collections in that database </td>
 </tr>
 <tr>
 <td class='confluenceTd'> dataSize </td>
 <td class='confluenceTd'> The total size in bytes of the data held in this database including the padding factor </td>
 </tr>
 <tr>
 <tr>
 <td class='confluenceTd'> fileSize </td>
 <td class='confluenceTd'> The total size in bytes of the data files that hold the database </td>
 </tr>
 <tr>
 <td class='confluenceTd'> indexes </td>
 <td class='confluenceTd'> Contains a count of the total number of indexes across all collections in the database </td>
 </tr>
 <tr>
 <td class='confluenceTd'> indexSize </td>
 <td class='confluenceTd'> The total size in bytes of all indexes created on this database </td>
 </tr>
 <tr>
 <td class='confluenceTd'> nsSizeMB </td>
 <td class='confluenceTd'> The total size of the namespace files (i.e. that end with .ns) for this database </td>
 </tr>
 <tr>
 <td class='confluenceTd'> numExtents </td>
 <td class='confluenceTd'> Contains a count of the number of extents in the database across all collections </td>
 </tr>
 <tr>
 <td class='confluenceTd'> objects </td>
 <td class='confluenceTd'> Contains a count of the number of objects (i.e. documents) in the database across all collections </td>
 </tr>
 <tr>
 <td class='confluenceTd'> storageSize </td>
 <td class='confluenceTd'> The total amount of space in bytes allocated to collections in this database for document storage </td>
 </tr>
 </tbody>
 </table>

####Metric Category: Collection Stats
#####<collection name>
<table><tbody>
 <tr>
 <th align="left"> Metric Name </th>
 <th align="left"> Description </th>
 </tr>
 <tr>
 <td class='confluenceTd'> count </td>
 <td class='confluenceTd'> The number of objects or documents in this collection </td>
 </tr>
 <tr>
 <td class='confluenceTd'> lastExtentSize </td>
 <td class='confluenceTd'> The size of the last extent allocated </td>
 </tr>
 <tr>
 <td class='confluenceTd'> nindexes </td>
 <td class='confluenceTd'> The number of indexes on the collection </td>
 </tr>
 <tr>
 <tr>
 <td class='confluenceTd'> numExtents </td>
 <td class='confluenceTd'> The total number of contiguously allocated data file regions </td>
 </tr>
 <tr>
 <td class='confluenceTd'> paddingFactor </td>
 <td class='confluenceTd'> The amount of space added to the end of each document at insert time </td>
 </tr>
 <tr>
 <td class='confluenceTd'> size </td>
 <td class='confluenceTd'> The size of the data stored in this collection </td>
 </tr>
 <tr>
 <td class='confluenceTd'> storageSize </td>
 <td class='confluenceTd'> The total amount of storage allocated to this collection for document storage </td>
 </tr>
 <tr>
 <td class='confluenceTd'> systemFlags </td>
 <td class='confluenceTd'> Reports the flags on this collection that reflect internal server options </td>
 </tr>
 <tr>
 <td class='confluenceTd'> totalIndexSize </td>
 <td class='confluenceTd'> The total size of all indexes. The scale argument affects this value </td>
 </tr>
 <tr>
 <td class='confluenceTd'> userFlags </td>
 <td class='confluenceTd'> Reports the flags on this collection set by the user </td>
 </tr>
 </tbody>
 </table>


##Contributing

Always feel free to fork and contribute any changes directly via [GitHub](https://github.com/Appdynamics/mongo-monitoring-extension).

##Community

Find out more in the [AppSphere](http://appsphere.appdynamics.com/t5/Extensions/MongoDB-Monitoring-Extension/idi-p/831) community.

##Support

For any questions or feature request, please contact [AppDynamics Center of Excellence](mailto:ace-request@appdynamics.com).
