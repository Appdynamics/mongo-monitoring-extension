# AppDynamics MongoDB Monitoring Extension

## Use Case
The MongoDB custom monitor captures statistics from the MongoDB server and displays them in the AppDynamics Metric Browser.
This extension works only with the standalone machine agent.

## Prerequisite

In order to use this extension, you do need a [Standalone JAVA Machine Agent](https://docs.appdynamics.com/display/PRO44/Java+Agent) or [SIM Agent](https://docs.appdynamics.com/display/PRO44/Server+Visibility).  For more details on downloading these products, please  visit [here](https://download.appdynamics.com/).

The extension needs to be able to connect to the Mongo DB in order to collect and send metrics. To do this, you will have to either establish a remote connection in between the extension and the product, or have an agent on the same machine running the product in order for the extension to collect and send the metrics.


##Installation
1. Download and unzip the  the file MongoMonitor-[version].zip into `<MACHINE_AGENT_HOME>/monitors/` directory.
2. In the newly created directory "MongoMonitor", edit the config.yml configuring the parameters specified in the below section.
3. All metrics to be reported are configured in metrics.xml. Users can remove entries from metrics.xml to stop the metric from reporting, or add new entries as well.
4. Restart the machine agent.

Please place the extension in the **"monitors"** directory of your **Machine Agent** installation directory. Do not place the extension in the **"extensions"** directory of your **Machine Agent** installation directory.

## Configuration ##

Note : Please make sure to not use tab (\t) while editing yaml files. You may want to validate the yaml file using a [yaml validator](http://yamllint.com/)

1. Configure the Mongo instances by editing the config.yml file in `<MACHINE_AGENT_HOME>/monitors/MongoMonitor/`.

   For eg.
   ```
        # MongoDB host and port. If ReplicaSet is enabled, configure all OR subset of members of the cluster.
        servers:
          - host: "localhost"
            port: 27017
          - host: "localhost"
            port: 27018

        # Specify this key if Password Encryption Support is required else keep it empty
        # If specified, adminDBPassword and databases passwords are now the encrypted passwords.
        passwordEncryptionKey: ""

        # Admin DB username and password. Required if mongod is started with --auth (Authentication) else keep empty
        # The user should have clusterMonitor role as a minimum
        adminDBUsername: "admin"
        adminDBPassword: "admin"

        # Change ssl to true if mongod is started with ssl. Then specify the pemFilePath, if not keep empty.
        ssl: false
        pemFilePath: ""

        # https://docs.mongodb.com/manual/reference/command/serverStatus/
        serverStatusExcludeMetricFields: [locks, wiredTiger, tcmalloc, opcountersRepl, metrics]
        
        #prefix used to show up metrics in AppDynamics
        metricPathPrefix:  "Custom Metrics|Mongo DB|"
        #This will create it in specific Tier. Replace <TIER_ID>
        #metricPrefix:  "Server|Component:<TIER_ID>|Custom Metrics|Mongo DB|"
        
   ```
   
2. Configure the path to the config.yml file by editing the <task-arguments> in the monitor.xml file in the `<MACHINE_AGENT_HOME>/monitors/MongoMonitor/` directory. Below is the sample

     ```
     <task-arguments>
         <!-- config file-->
         <argument name="config-file" is-required="true" default-value="monitors/MongoMonitor/config.yml" />
          ....
     </task-arguments>
    ```

Note : By default, a Machine agent or a AppServer agent can send a fixed number of metrics to the controller. To change this limit, please follow the instructions mentioned [here](http://docs.appdynamics.com/display/PRO14S/Metrics+Limits).
For eg.  
```    
    java -Dappdynamics.agent.maxMetrics=2500 -jar machineagent.jar
```

3. Configure the "COMPONENT_ID" under which the metrics need to be reported. This can be done by changing the value of `<COMPONENT_ID>` in
          metricPrefix: "Server|Component:<COMPONENT_ID>|Custom Metrics|Mongo DB|".

          For example,
          metricPrefix: "Server|Component:100|Custom Metrics|Mongo DB|"
          
#### Metrics.xml

You can add/remove metrics of your choosing by modifying the provided metrics.xml file. This file consists of all the metrics that
will be monitored and sent to the controller. Please look at how the metrics have been defined and follow the same convention when
adding new metrics. You do have the ability to also chose your Rollup types as well as for each metric as well as set an alias name
that you would like displayed on the metric browser.



For configuring the metrics, the following properties can be used:

     |     Property      |   Default value |         Possible values         |                                              Description                                                                                                |
     | :---------------- | :-------------- | :------------------------------ | :------------------------------------------------------------------------------------------------------------- |
     | alias             | metric name     | Any string                      | The substitute name to be used in the metric browser instead of metric name.                                   |
     | aggregationType   | "AVERAGE"       | "AVERAGE", "SUM", "OBSERVATION" | [Aggregation qualifier](https://docs.appdynamics.com/display/PRO44/Build+a+Monitoring+Extension+Using+Java)    |
     | timeRollUpType    | "AVERAGE"       | "AVERAGE", "SUM", "CURRENT"     | [Time roll-up qualifier](https://docs.appdynamics.com/display/PRO44/Build+a+Monitoring+Extension+Using+Java)   |
     | clusterRollUpType | "INDIVIDUAL"    | "INDIVIDUAL", "COLLECTIVE"      | [Cluster roll-up qualifier](https://docs.appdynamics.com/display/PRO44/Build+a+Monitoring+Extension+Using+Java)|
     | multiplier        | 1               | Any number                      | Value with which the metric needs to be multiplied.                                                            |
     | convert           | null            | Any key value map               | Set of key value pairs that indicates the value to which the metrics need to be transformed. eg: UP:0, DOWN:1  |
     | delta             | false           | true, false                     | If enabled, gives the delta values of metrics instead of actual values.                                        |

     For example,
     - name: "uptime"
              alias: "Uptime"
              aggregationType: "OBSERVATION"
              timeRollUpType: "CURRENT"
              clusterRollUpType: "COLLECTIVE"
              delta: false
     **All these metric properties are optional, and the default value shown in the table is applied to the metric(if a property has not been specified) by default.**
```


## Metrics

### Server Stats

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

##### Metric Category: Asserts

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

##### Metric Category: Background Flushing 

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

##### Metric Category: Connections

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


##### Metric Category: Global Lock
 
 <table>
 <tbody>
 <tr>
 <th align="left"> Metric Name </th>
 <th align="left"> Description </th>
 </tr>
 <tr>
  <td align="left"> Total Time </td>
  <td align="left"> The total time since the globalLock was started and created </td>
  </tr>
 </tbody>
 </table>
 
######Active Clients
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

###### Current Queue

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


##### Metric Category: Memory

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


##### Metric Category: Network
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

##### Metric Category: Operations
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

### Replica Stats
For each replica the following metrics are reported.
<table><tbody>
<tr>
<th align="left"> Metric Name </th>
<th align="left"> Description </th>
</tr>
<tr>
<td class='confluenceTd'> Up Time (ms) </td>
<td class='confluenceTd'> The duration of time that the server is up </td>
</tr>
<tr>
<td class='confluenceTd'> Health </td>
<td class='confluenceTd'> Conveys if the member is up (i.e. 1) or down (i.e. 0.) </td>
</tr>
<tr>
<td class='confluenceTd'> [State](http://docs.mongodb.org/manual/reference/replica-states/) </td>
<td class='confluenceTd'> Value between 0 and 10 that represents the replica state of the member. </td>
</tr>
</tbody>
</table>

### DB Stats
##### <DB Name>

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

##### Metric Category: Collection Stats
##### <collection name>

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

```

### Credentials Encryption

Please visit [this page](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-Password-Encryption-with-Extensions/ta-p/29397) to get detailed instructions on password encryption. The steps in this document will guide you through the whole process.

### Extensions Workbench
Workbench is an inbuilt feature provided with each extension in order to assist you to fine tune the extension setup before you actually deploy it on the controller. Please review the following document on [How to use the Extensions WorkBench](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-the-Extensions-WorkBench/ta-p/30130)

### Troubleshooting
Please follow the steps listed in this [troubleshooting-document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) in order to troubleshoot your issue. These are a set of common issues that customers might have faced during the installation of the extension. If these don't solve your issue, please follow the last step on the [troubleshooting-document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) to contact the support team.

### Support Tickets
If after going through the [Troubleshooting Document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) you have not been able to get your extension working, please file a ticket and add the following information.

Please provide the following in order for us to assist you better.

    1. Stop the running machine agent.
    2. Delete all existing logs under <MachineAgent>/logs.
    3. Please enable debug logging by editing the file <MachineAgent>/conf/logging/log4j.xml. Change the level value of the following <logger> elements to debug.
        <logger name="com.singularity">
        <logger name="com.appdynamics">
    4. Start the machine agent and please let it run for 10 mins. Then zip and upload all the logs in the directory <MachineAgent>/logs/*.
    5. Attach the zipped <MachineAgent>/conf/* directory here.
    6. Attach the zipped <MachineAgent>/monitors/ExtensionFolderYouAreHavingIssuesWith directory here.

For any support related questions, you can also contact help@appdynamics.com.


## Contributing

Always feel free to fork and contribute any changes directly here on [GitHub](https://github.com/Appdynamics/mongo-monitoring-extension).

## Version
|          Name            |  Version   |
|--------------------------|------------|
|Extension Version         |2.0.0       |
|Controller Compatibility  |4.5 or Later|
|Product Tested On         |4.5.13+     |
|Last Update               |05/20/2020  |
