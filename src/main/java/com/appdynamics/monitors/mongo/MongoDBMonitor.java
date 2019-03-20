/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.monitors.mongo;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.util.AssertUtils;
import com.appdynamics.monitors.mongo.config.Server;
import com.appdynamics.monitors.mongo.exception.MongoMonitorException;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static com.appdynamics.extensions.crypto.CryptoUtil.getPassword;
import static com.appdynamics.monitors.mongo.utils.Constants.*;
import static com.appdynamics.monitors.mongo.utils.MongoUtils.convertToString;

public class MongoDBMonitor extends ABaseMonitor {

    private static final Logger logger = LoggerFactory.getLogger(MongoDBMonitor.class);

    @Override
    protected String getDefaultMetricPrefix() {
        return CUSTOMMETRICS + METRICS_SEPARATOR + MONITORNAME;
    }

    @Override
    public String getMonitorName() {
        return MONITORNAME;
    }

    @Override
    protected int getTaskCount() {
        List<Map<String, String>> servers = (List<Map<String, String>>) getContextConfiguration().getConfigYml().get(SERVERS);
        AssertUtils.assertNotNull(servers, "The 'servers' section in config.yml is not initialised");
        return servers.size();
    }

    @Override
    protected void doRun(TasksExecutionServiceProvider taskExecutor) {
        Map<String, ?> config = getContextConfiguration().getConfigYml();
        if (config != null) {
            List<Map> servers = (List) config.get(SERVERS);
            AssertUtils.assertNotNull(servers, "The 'servers' section in config_old.yml is not initialised");
            if (servers != null && !servers.isEmpty()) {
                for (Map server : servers) {
                    try {
                        MongoDBMonitorTask task = createTask(server, taskExecutor);
                        taskExecutor.submit((String) server.get(NAME), task);
                    } catch (IOException e) {
                        logger.error("Cannot construct JMX uri for {}", convertToString(server.get(DISPLAY_NAME), ""));
                    }
                }
            } else {
                logger.error("There are no servers configured");
            }
        } else {
            logger.error("The config_old.yml is not loaded due to previous errors.The task will not run");
        }
    }

    private void buildMongoClient(List servers) {
//        String host = convertToString(server.get(HOST), EMPTY_STRING);
//        String portStr = convertToString(server.get(PORT), EMPTY_STRING);
//        int port = (portStr == null || portStr == EMPTY_STRING) ? -1 : Integer.parseInt(portStr);
        List<MongoCredential> credentials = getMongoCredentials(getCredentials());
        try {
            MongoClientOptions clientSSLOptions = MongoClientSSLOptions.getMongoClientSSLOptions(getContextConfiguration().getConfigYml());
            MongoCredential credential = getMongoCredential((Map<String, String>)getCredentials());
            MongoClient mongoClient = buildMongoClient(credential, clientSSLOptions, servers);
            MongoDatabase adminDB = mongoClient.getDatabase(ADMIN_DB);

        } catch (MongoMonitorException e) {
            logger.error("Error in building the MongoClient", e);
        }
    }


    private MongoClient buildMongoClient( List<MongoCredential> credentials, MongoClientOptions options, List<Map> servers) {

        MongoClient mongoClient ;
        List<ServerAddress> seeds = Lists.newArrayList();
        for (Map server : servers) {
            seeds.add(new ServerAddress(server.get(HOST).toString(), (Integer) server.get(PORT)));
        }
        if(options == null && credentials.size() == 0) {
            mongoClient = new MongoClient(seeds);
        } else if(options != null && credentials.size() == 0) {
            mongoClient = new MongoClient(seeds, options);
        } else if(options == null && credentials.size() > 0) {
            mongoClient = new MongoClient(seeds, credentials);
        } else {
            mongoClient = new MongoClient(seeds, credentials, options);
        }
        return mongoClient;
    }



    private List<MongoCredential> getMongoCredentials(Map<String, String> credentials) {
        List<MongoCredential> mongoCredentials = Lists.newArrayList();
        if (credentials.get("username") != null && credentials.get("password") != null) {
            MongoCredential adminDBCredential = MongoCredential.createCredential(credentials.get("username"), ADMIN_DB, credentials.get("password").toCharArray());
            mongoCredentials.add(adminDBCredential);
        } else {
            logger.info("adminDBUsername and adminDBPassword in config_old.yml is null or empty");
        }
        return mongoCredentials;
    }

    private MongoCredential getMongoCredential(Map<String, String> credentials) {
        MongoCredential adminDBCredential = null;
        if (credentials.get(USERNAME) != null && credentials.get(PASSWORD) != null) {
             adminDBCredential = MongoCredential.createCredential(credentials.get(USERNAME), ADMIN_DB, credentials.get(PASSWORD).toCharArray());
        } else {
            logger.info("username and password in config are null or empty");
        }
        return adminDBCredential;
    }

    private MongoClient buildMongoClient( MongoCredential credential, MongoClientOptions options, List<Map> servers) {

        MongoClient mongoClient = null ;
        List<ServerAddress> seeds = Lists.newArrayList();
        for (Map server : servers) {
            seeds.add(new ServerAddress(server.get(HOST).toString(), (Integer) server.get(PORT)));
        }
        if(options == null && credential == null) {
            mongoClient = new MongoClient(seeds);
        } else if(options != null && credential == null) {
            mongoClient = new MongoClient(seeds, options);
        } else if(options == null && credential != null) {
            // no such constructor
//            mongoClient = new MongoClient(seeds, credential);
        } else {
            mongoClient = new MongoClient(seeds, credential, options);
        }
        return mongoClient;
    }

    private Map getCredentials() {
        Map<String, String> credentials = new HashMap<String, String>();

        if (!Strings.isNullOrEmpty(getContextConfiguration().getConfigYml().get(USERNAME).toString())) {
            credentials.put(USERNAME, getContextConfiguration().getConfigYml().get(USERNAME).toString());
        }
        if (!Strings.isNullOrEmpty(getContextConfiguration().getConfigYml().get(PASSWORD).toString())) {
            credentials.put(PASSWORD, getContextConfiguration().getConfigYml().get(PASSWORD).toString());
        }
        if (!Strings.isNullOrEmpty(getContextConfiguration().getConfigYml().get(ENCRYPTED_PASSWORD).toString())) {
            credentials.put(ENCRYPTED_PASSWORD, getContextConfiguration().getConfigYml().get(ENCRYPTED_PASSWORD).toString());
        }
        if (!Strings.isNullOrEmpty(getContextConfiguration().getConfigYml().get(ENCRYPTION_KEY).toString())) {
            credentials.put(ENCRYPTION_KEY, getContextConfiguration().getConfigYml().get(ENCRYPTION_KEY).toString());
        }
        String password = getPassword(credentials);
        credentials.remove(ENCRYPTION_KEY);
        credentials.remove(ENCRYPTED_PASSWORD);
        credentials.put(PASSWORD, password);
        return credentials;
    }

    private MongoDBMonitorTask createTask(Map server, TasksExecutionServiceProvider taskExecutor) throws IOException {
        return new MongoDBMonitorTask.Builder()
                .metricWriter(taskExecutor.getMetricWriteHelper())
                .server(server)
                .credentials(getCredentials())
                .monitorConfiguration(getContextConfiguration())
                .build();
    }


    ////////// OLD STUFF
//
//    public static final String CONFIG_ARG = "config-file";
//
//    private static final String ADMIN_DB = "admin";
//    private static final String ENCRYPTION_KEY = "encryption-key";
//    private static final String PASSWORD_ENCRYPTED = "password-encrypted";
//    private static final String OK_RESPONSE = "1.0";
//    public static final String METRIC_SEPARATOR = "|";
//
//    private String metricPathPrefix;
//    private MongoClient mongoClient;
//
//    public MongoDBMonitor() {
//        System.out.println(logVersion());
//    }
//
//    public TaskOutput execute(Map<String, String> taskArgs, TaskExecutionContext arg1)
//            throws TaskExecutionException {
//        if (taskArgs != null) {
//            logger.info(logVersion());
//            String configFilename = getConfigFilename(taskArgs.get(CONFIG_ARG));
//            try {
//                Configuration config = YmlReader.readFromFile(configFilename, Configuration.class);
//                metricPathPrefix = config.getMetricPathPrefix();
//
//                List<MongoCredential> credentials = getMongoCredentials(config);
//                MongoClientOptions clientSSLOptions = getMongoClientSSLOptions(config);
//                mongoClient = buildMongoClient(config, credentials, clientSSLOptions);
//                MongoDatabase adminDB = mongoClient.getDatabase(ADMIN_DB);
//
//                fetchAndPrintServerStats(adminDB, config.getServerStatusExcludeMetricFields());
//                fetchAndPrintReplicaSetStats(adminDB);
//                fetchAndPrintDBStats();
//                fetchAndPrintCollectionStats();
//
//                logger.info("Mongo Monitoring Task completed successfully");
//                return new TaskOutput("Mongo Monitoring Task completed successfully");
//            } catch (Exception e) {
//                logger.error("Metrics Collection Failed: ", e);
//            } finally {
//                if (mongoClient != null) {
//                    mongoClient.close();
//                }
//            }
//        }
//        throw new TaskExecutionException("Mongo Monitoring Task completed with failures.");
//    }
//
//    private List<MongoCredential> getMongoCredentials(Configuration config) {
//        List<MongoCredential> credentials = Lists.newArrayList();
//        if(!Strings.isNullOrEmpty(config.getAdminDBUsername()) && !Strings.isNullOrEmpty(config.getAdminDBPassword())) {
//            MongoCredential adminDBCredential = MongoCredential.createCredential(config.getAdminDBUsername(), ADMIN_DB, getAdminDBPassword(config).toCharArray());
//            credentials.add(adminDBCredential);
//        } else {
//            logger.info("adminDBUsername and adminDBPassword in config_old.yml is null or empty");
//        }
//        return credentials;
//    }
//
//    private MongoClientOptions getMongoClientSSLOptions(Configuration config) throws MongoMonitorException {
//        MongoClientOptions clientOpts = null;
//        if (config.isSsl()) {
//            String filePath = config.getPemFilePath();
//            if (!Strings.isNullOrEmpty(filePath)) {
//                try {
//                    clientOpts = new MongoClientOptions.Builder().socketFactory(getSocketFactoryFromPEM(filePath)).build();
//                } catch (Exception e) {
//                    logger.error("Error establishing ssl socket factory", e);
//                    throw new MongoMonitorException("Error establishing ssl socket factory");
//                }
//            } else {
//                String msg = "The argument pemFilePath is null or empty in config_old.yml";
//                logger.error(msg);
//                throw new MongoMonitorException(msg);
//            }
//        }
//        return clientOpts;
//    }
//
//    private MongoClient buildMongoClient(Configuration config, List<MongoCredential> credentials, MongoClientOptions options) {
//        List<ServerAddress> seeds = Lists.newArrayList();
//        for (Server server : config.getServers()) {
//            seeds.add(new ServerAddress(server.getHost(), server.getPort()));
//        }
//        if(options == null && credentials.size() == 0) {
//            mongoClient = new MongoClient(seeds);
//        } else if(options == null && credentials.size() > 0) {
//            mongoClient = new MongoClient(seeds, credentials);
//        } else if(options != null && credentials.size() == 0) {
//            mongoClient = new MongoClient(seeds, options);
//        } else {
//            mongoClient = new MongoClient(seeds, credentials, options);
//        }
//        return mongoClient;
//    }
//
//    private SSLSocketFactory getSocketFactoryFromPEM(String filePath) throws Exception {
//        Security.addProvider(new BouncyCastleProvider());
//
//        PEMParser pemParser = new PEMParser(new FileReader(getConfigFilename(filePath)));
//        pemParser.readObject();
//        PemObject pemObject = pemParser.readPemObject();
//        pemParser.close();
//
//        X509CertificateHolder holder = new X509CertificateHolder(pemObject.getContent());
//        X509Certificate bc = new JcaX509CertificateConverter().setProvider("BC")
//                .getCertificate(holder);
//
//        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
//        keyStore.load(null, null);
//        keyStore.setCertificateEntry("ca", bc);
//
//        TrustManager trustManager = TrustManagerUtils.getDefaultTrustManager(keyStore);
//        SSLContext sslContext = SSLContextUtils.createSSLContext("TLS", null, trustManager);
//
//        return sslContext.getSocketFactory();
//    }
//
//    private String getAdminDBPassword(Configuration config) {
//        String encryptionKey = config.getPasswordEncryptionKey();
//        if (Strings.isNullOrEmpty(encryptionKey)) {
//            return config.getAdminDBPassword();
//        } else {
//            String encryptedPassword = config.getAdminDBPassword();
//            return getDecryptedPassword(encryptionKey, encryptedPassword);
//        }
//    }
//
//    private String getDecryptedPassword(String encryptionKey, String encryptedPassword) {
//        Map<String, String> argsForDecryption = new HashMap<String, String>();
//        argsForDecryption.put(PASSWORD_ENCRYPTED, encryptedPassword);
//        argsForDecryption.put(ENCRYPTION_KEY, encryptionKey);
//        return getPassword(argsForDecryption);
//    }
//
//    private DBObject executeMongoCommand(MongoDatabase db, Document command) {
//        DBObject dbObject = null;
//        try {
//            dbObject = (DBObject) JSON.parse(db.runCommand(command).toJson());
//            /*if (dbStats != null && !dbStats.getOk().toString().equals(OK_RESPONSE)) {
//                logger.error("Error retrieving db stats. Invalid permissions set for this user.DB= " + db.getName());
//            }*/
//        } catch (MongoCommandException e) {
//            logger.error("Error while executing " + command + " for db " + db, e);
//        }
//        return dbObject;
//    }
//
//    private void fetchAndPrintServerStats(MongoDatabase adminDB, List<String> serverStatusExcludeMetricCategories) {
//        Document commandJson = new Document();
//        commandJson.append("serverStatus", 1);
//        for (String suppressCategory : serverStatusExcludeMetricCategories) {
//            commandJson.append(suppressCategory, 0);
//        }
//        DBObject serverStats = executeMongoCommand(adminDB, commandJson);
//        printServerStats(serverStats);
//    }
//
//    private void fetchAndPrintReplicaSetStats(MongoDatabase adminDB) {
//        if(mongoClient.getReplicaSetStatus() != null) {
//            Document commandJson = new Document();
//            commandJson.append("replSetGetStatus", 1);
//            DBObject replicaStats = executeMongoCommand(adminDB, commandJson);
//            printReplicaStats(replicaStats);
//        } else {
//            logger.info("not running with --replSet, skipping replicaset stats");
//        }
//    }
//
//    private void fetchAndPrintDBStats() {
//        Document commandJson = new Document();
//        commandJson.append("dbStats", 1);
//        for(String databaseName: mongoClient.listDatabaseNames()) {
//            MongoDatabase db = mongoClient.getDatabase(databaseName);
//            DBObject dbStats = executeMongoCommand(db, commandJson);
//            printDBStats(dbStats);
//        }
//    }
//
//    private void fetchAndPrintCollectionStats() {
//        for(String databaseName: mongoClient.listDatabaseNames()) {
//            DB db = mongoClient.getDB(databaseName);
//            Set<String> collectionNames = db.getCollectionNames();
//            if (collectionNames != null && collectionNames.size() > 0) {
//                for (String collectionName : collectionNames) {
//                    DBCollection collection = db.getCollection(collectionName);
//                    CommandResult collectionStatsResult = collection.getStats();
//                    if (collectionStatsResult != null && collectionStatsResult.ok()) {
//                        DBObject collectionStats = (DBObject) JSON.parse(collectionStatsResult.toString());
//                        printCollectionStats(db.getName(), collectionName, collectionStats);
//                    } else {
//                        String errorMessage = "Retrieving stats for collection " + collectionName + " of " + db.getName() + " failed";
//                        if (collectionStatsResult != null) {
//                            errorMessage = errorMessage.concat(" with error message " + collectionStatsResult.getErrorMessage());
//                        }
//                        logger.error(errorMessage);
//                    }
//                }
//            }
//        }
//    }
//
//    private void printServerStats(DBObject serverStats) {
//        if(serverStats != null) {
//            String metricPath = getServerStatsMetricPrefix();
//            printNumericMetricsFromMap(serverStats.toMap(), metricPath);
//        }
//    }
//
//    private void printReplicaStats(DBObject replicaStats) {
//        if (replicaStats != null) {
//            String replicaStatsPath = getReplicaStatsMetricPrefix();
//            BasicDBList members = (BasicDBList) replicaStats.get("members");
//            for(int i = 0; i < members.size(); i++) {
//                DBObject member = (DBObject) members.get(i);
//                printMetric(replicaStatsPath + member.get("name") + METRIC_SEPARATOR + "Health", (Number) member.get("health"));
//                printMetric(replicaStatsPath + member.get("name") + METRIC_SEPARATOR + "State", (Number) member.get("state"));
//                printMetric(replicaStatsPath + member.get("name") + METRIC_SEPARATOR + "Uptime", (Number) member.get("uptime"));
//            }
//        }
//    }
//
//    private void printDBStats(DBObject dbStats) {
//        if (dbStats != null) {
//            String dbStatsPath = getDBStatsMetricPrefix(dbStats.get("db").toString());
//            printNumericMetricsFromMap(dbStats.toMap(), dbStatsPath);
//        }
//    }
//
//    private void printCollectionStats(String dbName, String collectionName, DBObject collectionStats) {
//        if (collectionStats != null) {
//            String collectionStatsPath = getCollectionStatsMetricPrefix(dbName, collectionName);
//            printNumericMetricsFromMap(collectionStats.toMap(), collectionStatsPath);
//        } else {
//            logger.info("CollectionStats for db " + dbName + "found to be NULL");
//        }
//    }
//
//    public void printNumericMetricsFromMap(Map<String, Object> map, String metricPath) {
//        for (Map.Entry<String, Object> entry : map.entrySet()) {
//            if (entry.getValue() instanceof Map) {
//                // Map found, digging further
//                printNumericMetricsFromMap((Map<String, Object>) entry.getValue(), metricPath + entry.getKey() + METRIC_SEPARATOR);
//            } else {
//                if(entry.getValue() instanceof Number) {
//                    printMetric(metricPath + entry.getKey(), (Number) entry.getValue());
//                }
//            }
//        }
//    }
//
//    /**
//     * Returns the metric to the AppDynamics Controller.
//     *
//     * @param metricName  Name of the Metric
//     * @param metricValue Value of the Metric
//     */
//    public void printMetric(String metricName, Number metricValue) {
//        if (metricValue != null) {
//            if(metricName.contains(",")) {
//                metricName = metricName.replaceAll(",", ":");
//            }
//            try {
//                MetricWriter metricWriter = getMetricWriter(metricName,
//                        MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
//                        MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
//                        MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL
//                );
//                metricWriter.printMetric(MetricUtils.toWholeNumberString(metricValue));
//
//            } catch (Exception e) {
//                logger.error("Exception while reporting metric " + metricName + " : " + metricValue, e);
//            }
//        } else {
//            logger.warn("Metric " + metricName + " is null");
//        }
//    }
//
//    private String getMetricPathPrefix() {
//        if (!metricPathPrefix.endsWith(METRIC_SEPARATOR)) {
//            metricPathPrefix += METRIC_SEPARATOR;
//        }
//        return metricPathPrefix;
//    }
//
//    /**
//     * Metric Prefix
//     *
//     * @return String
//     */
//    private String getServerStatsMetricPrefix() {
//        return getMetricPathPrefix() + "Server Stats" + METRIC_SEPARATOR;
//    }
//
//    private String getDBStatsMetricPrefix(String dbName) {
//        return getMetricPathPrefix() + "DB Stats|" + dbName + METRIC_SEPARATOR;
//    }
//
//    private String getCollectionStatsMetricPrefix(String dbName, String collectionName) {
//        return getDBStatsMetricPrefix(dbName) + "Collection Stats|" + collectionName + METRIC_SEPARATOR;
//    }
//
//    private String getReplicaStatsMetricPrefix() {
//        return getMetricPathPrefix() + "Replica Stats" + METRIC_SEPARATOR;
//    }
//
//    private String getConfigFilename(String filename) {
//        if (filename == null) {
//            return "";
//        }
//        //for absolute paths
//        if (new File(filename).exists()) {
//            return filename;
//        }
//        //for relative paths
//        File jarPath = PathResolver.resolveDirectory(AManagedMonitor.class);
//        String configFileName = "";
//        if (!Strings.isNullOrEmpty(filename)) {
//            configFileName = jarPath + File.separator + filename;
//        }
//        return configFileName;
//    }
//
//    private String logVersion() {
//        String msg = String.format("Using Monitor Version [%s]", getImplementationVersion());
//        return msg;
//    }
//
//    public static String getImplementationVersion() {
//        return MongoDBMonitor.class.getPackage().getImplementationTitle();
//    }
}
