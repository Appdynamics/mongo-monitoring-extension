/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.monitors.mongo;

import com.appdynamics.extensions.PathResolver;
import com.appdynamics.extensions.crypto.CryptoUtil;
import com.appdynamics.extensions.util.MetricUtils;
import com.appdynamics.extensions.yml.YmlReader;
import com.appdynamics.monitors.mongo.config.Configuration;
import com.appdynamics.monitors.mongo.config.Server;
import com.appdynamics.monitors.mongo.exception.MongoMonitorException;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.commons.net.util.SSLContextUtils;
import org.apache.commons.net.util.TrustManagerUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.io.pem.PemObject;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.File;
import java.io.FileReader;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MongoDBMonitor extends AManagedMonitor {

    private static final Logger logger = LoggerFactory.getLogger(MongoDBMonitor.class);
    public static final String CONFIG_ARG = "config-file";

    private static final String ADMIN_DB = "admin";
    private static final String ENCRYPTION_KEY = "encryption-key";
    private static final String PASSWORD_ENCRYPTED = "password-encrypted";
    private static final String OK_RESPONSE = "1.0";
    public static final String METRIC_SEPARATOR = "|";

    private String metricPathPrefix;
    private MongoClient mongoClient;

    public MongoDBMonitor() {
        System.out.println(logVersion());
    }

    /**
     * Main execution method that uploads the metrics to the AppDynamics Controller
     *
     * @see com.singularity.ee.agent.systemagent.api.ITask#execute(java.util.Map, com.singularity.ee.agent.systemagent.api.TaskExecutionContext)
     */
    public TaskOutput execute(Map<String, String> taskArgs, TaskExecutionContext arg1)
            throws TaskExecutionException {
        if (taskArgs != null) {
            logger.info(logVersion());
            String configFilename = getConfigFilename(taskArgs.get(CONFIG_ARG));
            try {
                Configuration config = YmlReader.readFromFile(configFilename, Configuration.class);
                metricPathPrefix = config.getMetricPathPrefix();

                List<MongoCredential> credentials = getMongoCredentials(config);
                MongoClientOptions clientSSLOptions = getMongoClientSSLOptions(config);
                mongoClient = buildMongoClient(config, credentials, clientSSLOptions);
                MongoDatabase adminDB = mongoClient.getDatabase(ADMIN_DB);

                fetchAndPrintServerStats(adminDB, config.getServerStatusExcludeMetricFields());
                fetchAndPrintReplicaSetStats(adminDB);
                fetchAndPrintDBStats();
                //fetchAndPrintCollectionStats();

                logger.info("Mongo Monitoring Task completed successfully");
                return new TaskOutput("Mongo Monitoring Task completed successfully");
            } catch (Exception e) {
                logger.error("Metrics Collection Failed: ", e);
            } finally {
                if (mongoClient != null) {
                    mongoClient.close();
                }
            }
        }
        throw new TaskExecutionException("Mongo Monitoring Task completed with failures.");
    }

    private List<MongoCredential> getMongoCredentials(Configuration config) {
        List<MongoCredential> credentials = Lists.newArrayList();
        if(!Strings.isNullOrEmpty(config.getAdminDBUsername()) && !Strings.isNullOrEmpty(config.getAdminDBPassword())) {
            MongoCredential adminDBCredential = MongoCredential.createCredential(config.getAdminDBUsername(), ADMIN_DB, getAdminDBPassword(config).toCharArray());
            credentials.add(adminDBCredential);
        } else {
            logger.info("adminDBUsername and adminDBPassword in config.yml is null or empty");
        }
        return credentials;
    }

    private MongoClientOptions getMongoClientSSLOptions(Configuration config) throws MongoMonitorException {
        MongoClientOptions clientOpts = null;
        if (config.isSsl()) {
            String filePath = config.getPemFilePath();
            if (!Strings.isNullOrEmpty(filePath)) {
                try {
                    clientOpts = new MongoClientOptions.Builder().socketFactory(getSocketFactoryFromPEM(filePath)).build();
                } catch (Exception e) {
                    logger.error("Error establishing ssl socket factory", e);
                    throw new MongoMonitorException("Error establishing ssl socket factory");
                }
            } else {
                String msg = "The argument pemFilePath is null or empty in config.yml";
                logger.error(msg);
                throw new MongoMonitorException(msg);
            }
        }
        return clientOpts;
    }

    private MongoClient buildMongoClient(Configuration config, List<MongoCredential> credentials, MongoClientOptions options) {
        List<ServerAddress> seeds = Lists.newArrayList();
        for (Server server : config.getServers()) {
            seeds.add(new ServerAddress(server.getHost(), server.getPort()));
        }
        if(options == null && credentials.size() == 0) {
            mongoClient = new MongoClient(seeds);
        } else if(options == null && credentials.size() > 0) {
            mongoClient = new MongoClient(seeds, credentials);
        } else if(options != null && credentials.size() == 0) {
            mongoClient = new MongoClient(seeds, options);
        } else {
            mongoClient = new MongoClient(seeds, credentials, options);
        }
        return mongoClient;
    }

    private SSLSocketFactory getSocketFactoryFromPEM(String filePath) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        PEMParser pemParser = new PEMParser(new FileReader(getConfigFilename(filePath)));
        pemParser.readObject();
        PemObject pemObject = pemParser.readPemObject();
        pemParser.close();

        X509CertificateHolder holder = new X509CertificateHolder(pemObject.getContent());
        X509Certificate bc = new JcaX509CertificateConverter().setProvider("BC")
                .getCertificate(holder);

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", bc);

        TrustManager trustManager = TrustManagerUtils.getDefaultTrustManager(keyStore);
        SSLContext sslContext = SSLContextUtils.createSSLContext("TLS", null, trustManager);

        return sslContext.getSocketFactory();
    }

    private String getAdminDBPassword(Configuration config) {
        String encryptionKey = config.getPasswordEncryptionKey();
        if (Strings.isNullOrEmpty(encryptionKey)) {
            return config.getAdminDBPassword();
        } else {
            String encryptedPassword = config.getAdminDBPassword();
            return getDecryptedPassword(encryptionKey, encryptedPassword);
        }
    }

    private String getDecryptedPassword(String encryptionKey, String encryptedPassword) {
        Map<String, String> argsForDecryption = new HashMap<String, String>();
        argsForDecryption.put(PASSWORD_ENCRYPTED, encryptedPassword);
        argsForDecryption.put(ENCRYPTION_KEY, encryptionKey);
        return CryptoUtil.getPassword(argsForDecryption);
    }

    private DBObject executeMongoCommand(MongoDatabase db, Document command) {
        DBObject dbObject = null;
        try {
            dbObject = (DBObject) JSON.parse(db.runCommand(command).toJson());
            /*if (dbStats != null && !dbStats.getOk().toString().equals(OK_RESPONSE)) {
                logger.error("Error retrieving db stats. Invalid permissions set for this user.DB= " + db.getName());
            }*/
        } catch (MongoCommandException e) {
            logger.error("Error while executing " + command + " for db " + db, e);
        }
        return dbObject;
    }

    private void fetchAndPrintServerStats(MongoDatabase adminDB, List<String> serverStatusExcludeMetricCategories) {
        Document commandJson = new Document();
        commandJson.append("serverStatus", 1);
        for (String suppressCategory : serverStatusExcludeMetricCategories) {
            commandJson.append(suppressCategory, 0);
        }
        DBObject serverStats = executeMongoCommand(adminDB, commandJson);
        printServerStats(serverStats);
    }

    private void fetchAndPrintReplicaSetStats(MongoDatabase adminDB) {
        if(mongoClient.getReplicaSetStatus() != null) {
            Document commandJson = new Document();
            commandJson.append("replSetGetStatus", 1);
            DBObject replicaStats = executeMongoCommand(adminDB, commandJson);
            printReplicaStats(replicaStats);
        } else {
            logger.info("not running with --replSet, skipping replicaset stats");
        }
    }

    private void fetchAndPrintDBStats() {
        Document commandJson = new Document();
        commandJson.append("dbStats", 1);
        for(String databaseName: mongoClient.listDatabaseNames()) {
            MongoDatabase db = mongoClient.getDatabase(databaseName);
            DBObject dbStats = executeMongoCommand(db, commandJson);
            printDBStats(dbStats);
        }
    }

    private void fetchAndPrintCollectionStats() {
        for(String databaseName: mongoClient.listDatabaseNames()) {
            DB db = mongoClient.getDB(databaseName);
            Set<String> collectionNames = db.getCollectionNames();
            if (collectionNames != null && collectionNames.size() > 0) {
                for (String collectionName : collectionNames) {
                    DBCollection collection = db.getCollection(collectionName);
                    CommandResult collectionStatsResult = collection.getStats();
                    if (collectionStatsResult != null && collectionStatsResult.ok()) {
                        DBObject collectionStats = (DBObject) JSON.parse(collectionStatsResult.toString());
                        printCollectionStats(db.getName(), collectionName, collectionStats);
                    } else {
                        String errorMessage = "Retrieving stats for collection " + collectionName + " of " + db.getName() + " failed";
                        if (collectionStatsResult != null) {
                            errorMessage = errorMessage.concat(" with error message " + collectionStatsResult.getErrorMessage());
                        }
                        logger.error(errorMessage);
                    }
                }
            }
        }
    }

    private void printServerStats(DBObject serverStats) {
        if(serverStats != null) {
            String metricPath = getServerStatsMetricPrefix();
            printNumericMetricsFromMap(serverStats.toMap(), metricPath);
        }
    }

    private void printReplicaStats(DBObject replicaStats) {
        if (replicaStats != null) {
            String replicaStatsPath = getReplicaStatsMetricPrefix();
            BasicDBList members = (BasicDBList) replicaStats.get("members");
            for(int i = 0; i < members.size(); i++) {
                DBObject member = (DBObject) members.get(i);
                printMetric(replicaStatsPath + member.get("name") + METRIC_SEPARATOR + "Health", (Number) member.get("health"));
                printMetric(replicaStatsPath + member.get("name") + METRIC_SEPARATOR + "State", (Number) member.get("state"));
                printMetric(replicaStatsPath + member.get("name") + METRIC_SEPARATOR + "Uptime", (Number) member.get("uptime"));
            }
        }
    }

    private void printDBStats(DBObject dbStats) {
        if (dbStats != null) {
            String dbStatsPath = getDBStatsMetricPrefix(dbStats.get("db").toString());
            printNumericMetricsFromMap(dbStats.toMap(), dbStatsPath);
        }
    }

    private void printCollectionStats(String dbName, String collectionName, DBObject collectionStats) {
        if (collectionStats != null) {
            String collectionStatsPath = getCollectionStatsMetricPrefix(dbName, collectionName);
            printNumericMetricsFromMap(collectionStats.toMap(), collectionStatsPath);
        } else {
            logger.info("CollectionStats for db " + dbName + "found to be NULL");
        }
    }

    public void printNumericMetricsFromMap(Map<String, Object> map, String metricPath) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                // Map found, digging further
                printNumericMetricsFromMap((Map<String, Object>) entry.getValue(), metricPath + entry.getKey() + METRIC_SEPARATOR);
            } else {
                if(entry.getValue() instanceof Number) {
                    printMetric(metricPath + entry.getKey(), (Number) entry.getValue());
                }
            }
        }
    }

    /**
     * Returns the metric to the AppDynamics Controller.
     *
     * @param metricName  Name of the Metric
     * @param metricValue Value of the Metric
     */
    public void printMetric(String metricName, Number metricValue) {
        if (metricValue != null) {
            if(metricName.contains(",")) {
                metricName = metricName.replaceAll(",", ":");
            }
            try {
                MetricWriter metricWriter = getMetricWriter(metricName,
                        MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
                        MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
                        MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL
                );
                metricWriter.printMetric(MetricUtils.toWholeNumberString(metricValue));

            } catch (Exception e) {
                logger.error("Exception while reporting metric " + metricName + " : " + metricValue, e);
            }
        } else {
            logger.warn("Metric " + metricName + " is null");
        }
    }

    private String getMetricPathPrefix() {
        if (!metricPathPrefix.endsWith(METRIC_SEPARATOR)) {
            metricPathPrefix += METRIC_SEPARATOR;
        }
        return metricPathPrefix;
    }

    /**
     * Metric Prefix
     *
     * @return String
     */
    private String getServerStatsMetricPrefix() {
        return getMetricPathPrefix() + "Server Stats" + METRIC_SEPARATOR;
    }

    private String getDBStatsMetricPrefix(String dbName) {
        return getMetricPathPrefix() + "DB Stats|" + dbName + METRIC_SEPARATOR;
    }

    private String getCollectionStatsMetricPrefix(String dbName, String collectionName) {
        return getDBStatsMetricPrefix(dbName) + "Collection Stats|" + collectionName + METRIC_SEPARATOR;
    }

    private String getReplicaStatsMetricPrefix() {
        return getMetricPathPrefix() + "Replica Stats" + METRIC_SEPARATOR;
    }

    private String getConfigFilename(String filename) {
        if (filename == null) {
            return "";
        }
        //for absolute paths
        if (new File(filename).exists()) {
            return filename;
        }
        //for relative paths
        File jarPath = PathResolver.resolveDirectory(AManagedMonitor.class);
        String configFileName = "";
        if (!Strings.isNullOrEmpty(filename)) {
            configFileName = jarPath + File.separator + filename;
        }
        return configFileName;
    }

    private String logVersion() {
        String msg = String.format("Using Monitor Version [%s]", getImplementationVersion());
        return msg;
    }

    public static String getImplementationVersion() {
        return MongoDBMonitor.class.getPackage().getImplementationTitle();
    }
}
