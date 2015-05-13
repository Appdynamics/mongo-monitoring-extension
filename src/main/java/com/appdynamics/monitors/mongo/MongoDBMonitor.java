/**
 * Copyright 2013 AppDynamics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamics.monitors.mongo;

import com.appdynamics.extensions.crypto.CryptoUtil;
import com.appdynamics.monitors.mongo.json.db.CollectionStats;
import com.appdynamics.monitors.mongo.json.db.DBStats;
import com.appdynamics.monitors.mongo.json.server.ServerStats;
import com.google.gson.Gson;
import com.mongodb.*;
import com.singularity.ee.agent.systemagent.SystemAgent;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.commons.net.util.SSLContextUtils;
import org.apache.commons.net.util.TrustManagerUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.io.pem.PemObject;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.naming.AuthenticationException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.security.CodeSource;
import java.security.KeyStore;
import java.security.ProtectionDomain;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.*;

public class MongoDBMonitor extends AManagedMonitor {

    private static final Logger logger = Logger.getLogger(MongoDBMonitor.class);

    private static final String ARG_HOST = "host";
    private static final String ARG_PORT = "port";
    private static final String ARG_USER = "username";
    private static final String ARG_PASS = "password";
    private static final String USE_SSL = "use-ssl";
    private static final String PEM_FILE_PATH = "pem-file";
    private static final String ARG_DB = "db";
    private static final String ADMIN_DB = "admin";
    private static final String ARG_XML_PATH = "properties-path";
    private static final String ENCRYPTION_KEY = "encryption-key";
    private static final String PASSWORD_ENCRYPTED = "password-encrypted";
    private static final String OK_RESPONSE = "1.0";
    public static final String METRIC_SEPARATOR = "|";

    private static String encryptionKey;
    private String metricPathPrefix;
    private MongoClient mongoClient;
    private String host;
    private String port;
    private File installDir;

    public MongoDBMonitor() {
        String msg = "Using Monitor Version [" + getImplementationVersion() + "]";
        logger.info(msg);
        System.out.println(msg);
        installDir = resolveInstallDir();
    }

    /**
     * Main execution method that uploads the metrics to the AppDynamics Controller
     *
     * @see com.singularity.ee.agent.systemagent.api.ITask#execute(java.util.Map, com.singularity.ee.agent.systemagent.api.TaskExecutionContext)
     */
    public TaskOutput execute(Map<String, String> params, TaskExecutionContext arg1)
            throws TaskExecutionException {
        if (params != null) {
            logger.info("Starting Mongo Monitoring Task");
            metricPathPrefix = params.get("metricPathPrefix");
            try {
                MongoCredential adminCredentials = getAdminCredentials(params);

                MongoClientOptions options = getMongoClientOptions(params);

                DB adminDB = connectToAdminDB(adminCredentials, options);

                ServerStats serverStats = getServerStats(adminDB);

                printServerStats(serverStats);

                List<MongoCredential> additionalDBDetails = getAdditionalDBDetails(params);

                List<DB> additionalDBs = connect(additionalDBDetails);

                for (DB db : additionalDBs) {
                    try {
                        DBStats dbStats = getDBStats(db);
                        printDBStats(dbStats);

                        Set<String> collectionNames = db.getCollectionNames();
                        if (collectionNames != null && collectionNames.size() > 0) {
                            for (String collectionName : collectionNames) {
                                DBCollection collection = db.getCollection(collectionName);
                                CommandResult collectionStatsResult = collection.getStats();
                                if (collectionStatsResult != null && collectionStatsResult.ok()) {
                                    CollectionStats collectionStats = new Gson().fromJson(collectionStatsResult.toString(), CollectionStats.class);
                                    printCollectionStats(db.getName(), collectionStats);
                                } else {
                                    String errorMessage = "Retrieving stats for collection " + collectionName + " of " + db.getName() + " failed";
                                    if (collectionStatsResult != null) {
                                        errorMessage = errorMessage.concat(" with error message " + collectionStatsResult.getErrorMessage());
                                    }
                                    logger.error(errorMessage);
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
                logger.info("Mongo Monitoring Task completed successfully");
                return new TaskOutput("Mongo DB Metric Upload Complete");
            } catch (Exception e) {
                logger.error("Exception", e);
            } finally {
                if (mongoClient != null) {
                    mongoClient.close();
                }
            }
        }
        throw new TaskExecutionException("Mongo monitoring task completed with failures.");
    }

    private DBStats getDBStats(DB db) {
        DBStats dbStats = new Gson().fromJson(db.command("dbStats").toString().trim(), DBStats.class);
        if (dbStats != null && !dbStats.getOk().toString().equals(OK_RESPONSE)) {
            logger.error("Error retrieving db stats. Invalid permissions set for this user.DB= " + db.getName());
        }
        return dbStats;
    }

    private List<MongoCredential> getAdditionalDBDetails(Map<String, String> params) {

        String xmlPath = params.get(ARG_XML_PATH);
        List<MongoCredential> additionalDBCredentials = new ArrayList<MongoCredential>();

        if (isNotEmpty(xmlPath)) {
            File file = resolvePath(xmlPath);
            if (file.exists()) {
                try {
                    SAXReader reader = new SAXReader();
                    Document doc = reader.read(file);
                    Element root = doc.getRootElement();

                    for (Element credElem : (List<Element>) root.elements("credentials")) {
                        if (credElem.elementText(ARG_DB).length() > 0) {
                            String password = getDBPassword(credElem);
                            MongoCredential cred = MongoCredential.createMongoCRCredential(
                                    credElem.elementText(ARG_USER),
                                    credElem.elementText(ARG_DB),
                                    password.toCharArray());
                            additionalDBCredentials.add(cred);
                        }
                    }
                } catch (DocumentException e) {
                    logger.error("Cannot read '" + file.getAbsolutePath() + "'. Monitor is running without additional credentials", e);
                }
            } else {
                logger.error("Cannot read '" + xmlPath + "'. Monitor is running without additional credentials." +
                        "The absolute path is " + file.getAbsolutePath());
            }
        }
        return additionalDBCredentials;
    }

    private File resolvePath(String xmlPath) {
        File file = new File(xmlPath);
        if (file.exists()) {
            return file;
        } else {
            return new File(installDir, xmlPath);
        }
    }

    private File resolveInstallDir() {
        File installDir = null;
        try {
            ProtectionDomain pd = SystemAgent.class.getProtectionDomain();
            if (pd != null) {
                CodeSource cs = pd.getCodeSource();
                if (cs != null) {
                    URL url = cs.getLocation();
                    String path = URLDecoder.decode(url.getFile(), "UTF-8");
                    File dir = new File(path).getParentFile();
                    if (dir.exists()) {
                        installDir = dir;
                    } else {
                        logger.error("Install dir resolved to " + dir.getAbsolutePath() + ", however it doesnt exist.");
                    }
                }

            }
        } catch (Exception e) {
            logger.error("Error while resolving the Install Dir ", e);
        }
        if (installDir != null) {
            logger.info("Install dir resolved to " + installDir.getAbsolutePath());
            return installDir;
        } else {
            File workDir = new File("");
            logger.info("Failed to resolve install dir, returning current work dir" + workDir.getAbsolutePath());
            return workDir;
        }
    }

    private DB connectToAdminDB(MongoCredential adminCredentials, MongoClientOptions options) {
        try {
            if (options != null) {
                mongoClient = new MongoClient(new ServerAddress(host, Integer.parseInt(port)), options);
            } else {
                mongoClient = new MongoClient(host, Integer.parseInt(port));
            }
        } catch (UnknownHostException e) {
            String msg = String.format("Unable to connect to mongodb; host=%s, port=%s", host, port);
            logger.error(msg, e);
            throw new RuntimeException(msg, e);
        }

        DB db = mongoClient.getDB(adminCredentials.getSource());

        boolean authenticated = db.authenticate(adminCredentials.getUserName(), adminCredentials.getPassword());
        if (!authenticated) {
            String msg = String.format("Unable to authenticate with the db %s, user=%s, using password ****",
                    adminCredentials.getSource(), adminCredentials.getUserName());
            logger.error(msg);
            throw new RuntimeException(msg);
        }
        return db;
    }

    private MongoClientOptions getMongoClientOptions(Map<String, String> params) {
        String useSSL = params.get(USE_SSL);
        MongoClientOptions clientOpts = null;
        if (Boolean.valueOf(useSSL)) {
            String filePath = params.get(PEM_FILE_PATH);
            if (isNotEmpty(filePath)) {
                try {
                    clientOpts = new MongoClientOptions.Builder().socketFactory(getSocketFactoryFromPEM(filePath)).build();
                } catch (Exception e) {
                    logger.error("Error establishing ssl socket factory", e);
                    throw new RuntimeException("Error establishing ssl socket factory");
                }
            } else {
                String msg = "The argument " + PEM_FILE_PATH + "is null or empty in monitor.xml";
                logger.error(msg);
                throw new RuntimeException(msg);
            }
        } else {
            logger.debug(USE_SSL + " value in monitor.xml set to " + useSSL);
        }
        return clientOpts;
    }

    private SSLSocketFactory getSocketFactoryFromPEM(String filePath) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        PEMParser pemParser = new PEMParser(new FileReader(resolvePath(filePath)));
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

    private String getPassword(Map<String, String> taskArguments) {
        if (taskArguments.get(ARG_PASS) != null) {
            return taskArguments.get(ARG_PASS);
        } else if (taskArguments.containsKey(PASSWORD_ENCRYPTED)) {
            encryptionKey = taskArguments.get(ENCRYPTION_KEY);
            String encryptedPassword = taskArguments.get(PASSWORD_ENCRYPTED);
            return getDecryptedPassword(encryptionKey, encryptedPassword);
        }
        return "";
    }

    private String getDBPassword(Element credElem) {
        if (credElem.elements().contains(ARG_PASS)) {
            return credElem.elementText(ARG_PASS);
        } else if (credElem.elements().contains(PASSWORD_ENCRYPTED)) {
            String encryptedPassword = credElem.elementText(PASSWORD_ENCRYPTED);
            return getDecryptedPassword(encryptionKey, encryptedPassword);
        }
        return "";
    }

    private String getDecryptedPassword(String encryptionKey, String encryptedPassword) {
        Map<String, String> argsForDecryption = new HashMap<String, String>();
        argsForDecryption.put(PASSWORD_ENCRYPTED, encryptedPassword);
        argsForDecryption.put(ENCRYPTION_KEY, encryptionKey);
        return CryptoUtil.getPassword(argsForDecryption);
    }

    private MongoCredential getAdminCredentials(final Map<String, String> params) {
        MongoCredential cred;
        host = params.get(ARG_HOST);
        port = params.get(ARG_PORT);

        String password = getPassword(params);

        cred = MongoCredential.createMongoCRCredential(
                params.get(ARG_USER),
                ADMIN_DB,
                password.toCharArray());
        return cred;
    }

    /**
     * Connects to the Mongo DB Server
     *
     * @param additionalDBDetails additional db information
     * @throws UnknownHostException
     * @throws AuthenticationException
     */
    public List<DB> connect(List<MongoCredential> additionalDBDetails) throws UnknownHostException, AuthenticationException {
        List<DB> mongoDBs = new ArrayList<DB>();
        for (MongoCredential cred : additionalDBDetails) {
            DB db = mongoClient.getDB(cred.getSource());
            if ((cred.getUserName() == null && cred.getPassword() == null)
                    || (cred.getUserName().equals("") && cred.getPassword().length == 0)
                    || db.isAuthenticated()
                    || db.authenticate(cred.getUserName(), cred.getPassword())) {
                mongoDBs.add(db);
            } else {
                logger.error("User is not allowed to view statistics for database: " + db.getName());
            }
        }
        return mongoDBs;
    }

    /**
     * Returns the metric to the AppDynamics Controller.
     *
     * @param metricName  Name of the Metric
     * @param metricValue Value of the Metric
     */
    public void printMetric(String metricName, Number metricValue) {
        if (metricValue != null) {
            try {
                MetricWriter metricWriter = getMetricWriter(metricName,
                        MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                        MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
                        MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
                );
                metricWriter.printMetric(String.valueOf(Math.round(metricValue.doubleValue())));
            } catch (Exception e) {
                logger.error(e);
            }
        } else {
            logger.warn("Metric " + metricName + "is null");
        }
    }

    private void printServerStats(ServerStats serverStats) {
        if (serverStats != null) {
            printUpTimeStats(serverStats);
            printAssertStats(serverStats);
            printBackgroundFlushingStats(serverStats);
            printConnectionStats(serverStats);
            printCursorsStats(serverStats);
            printDurStats(serverStats);
            printExtraInfoStats(serverStats);
            printGlobalLocksStats(serverStats);
            printIndexCounterStats(serverStats);
            printNetworkStats(serverStats);
            printOperationStats(serverStats);
            printOpCountersReplStats(serverStats);
            printMemoryStats(serverStats);
        }
    }

    private void printDBStats(DBStats dbStats) {
        if (dbStats != null) {
            String dbStatsPath = getDBStatsMetricPrefix(dbStats.getDb());

            printMetric(dbStatsPath + "collections", dbStats.getCollections());
            printMetric(dbStatsPath + "objects", dbStats.getObjects());
            printMetric(dbStatsPath + "avgObjSize", dbStats.getAvgObjSize());
            printMetric(dbStatsPath + "dataSize", dbStats.getDataSize());
            printMetric(dbStatsPath + "storageSize", dbStats.getStorageSize());
            printMetric(dbStatsPath + "numExtents", dbStats.getNumExtents());
            printMetric(dbStatsPath + "indexes", dbStats.getIndexes());
            printMetric(dbStatsPath + "indexSize", dbStats.getIndexSize());
            printMetric(dbStatsPath + "fileSize", dbStats.getFileSize());
            printMetric(dbStatsPath + "nsSizeMB", dbStats.getNsSizeMB());
        }
    }

    private void printCollectionStats(String dbName, CollectionStats collectionStats) {
        if (collectionStats != null) {
            String collectionStatsPath = getCollectionStatsMetricPrefix(dbName, collectionStats.getNs());

            printMetric(collectionStatsPath + "count", collectionStats.getCount());
            printMetric(collectionStatsPath + "size", collectionStats.getSize());
            printMetric(collectionStatsPath + "storageSize", collectionStats.getStorageSize());
            printMetric(collectionStatsPath + "numExtents", collectionStats.getNumExtents());
            printMetric(collectionStatsPath + "nindexes", collectionStats.getNindexes());
            printMetric(collectionStatsPath + "lastExtentSize", collectionStats.getLastExtentSize());
            printMetric(collectionStatsPath + "paddingFactor", collectionStats.getPaddingFactor());
            printMetric(collectionStatsPath + "systemFlags", collectionStats.getSystemFlags());
            printMetric(collectionStatsPath + "userFlags", collectionStats.getUserFlags());
            printMetric(collectionStatsPath + "totalIndexSize", collectionStats.getTotalIndexSize());

            for (Map.Entry<String, Number> index : collectionStats.getIndexSizes().entrySet()) {
                printMetric(collectionStatsPath + "Index Size|" + index.getKey(), index.getValue());
            }
        } else {
            logger.info("CollectionStats for db " + dbName + "found to be NULL");
        }
    }

    private void printUpTimeStats(ServerStats serverStats) {
        printMetric(getServerStatsMetricPrefix() + "UP Time (Milliseconds)", serverStats.getUptimeMillis()
        );
    }

    private void printOpCountersReplStats(ServerStats serverStats) {
        if (serverStats.getOpcountersRepl() != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(getServerStatsMetricPrefix()).append("OpCountersRepl").append(METRIC_SEPARATOR);
            printMetric(sb.toString() + "Insert", serverStats.getOpcountersRepl().getInsert());
            printMetric(sb.toString() + "Query", serverStats.getOpcountersRepl().getQuery());
            printMetric(sb.toString() + "Update", serverStats.getOpcountersRepl().getUpdate());
            printMetric(sb.toString() + "Delete", serverStats.getOpcountersRepl().getDelete());
            printMetric(sb.toString() + "GetMore", serverStats.getOpcountersRepl().getGetmore());
            printMetric(sb.toString() + "Command", serverStats.getOpcountersRepl().getCommand());
        } else {
            logger.warn("No information on OpCountersRepl available in db.serverStatus()");
        }

    }

    private void printExtraInfoStats(ServerStats serverStats) {
        if (serverStats.getExtra_info() != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(getServerStatsMetricPrefix()).append("ExtraInfo").append(METRIC_SEPARATOR);
            printMetric(sb.toString() + "Heap Usage Bytes", serverStats.getExtra_info().getHeap_usage_bytes());
            printMetric(sb.toString() + "Page Faults", serverStats.getExtra_info().getPage_faults());
        } else {
            logger.warn("No information on ExtraInfo available in db.serverStatus()");
        }
    }

    private void printDurStats(ServerStats serverStats) {
        if (serverStats.getDur() != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(getServerStatsMetricPrefix()).append("Dur").append(METRIC_SEPARATOR);
            printMetric(sb.toString() + "Commits", serverStats.getDur().getCommits());
            printMetric(sb.toString() + "Journaled MB", serverStats.getDur().getJournaledMB());
            printMetric(sb.toString() + "WriteToDataFilesMB", serverStats.getDur().getWriteToDataFilesMB());
            printMetric(sb.toString() + "Compression", serverStats.getDur().getCompression());
            printMetric(sb.toString() + "Commits In Write Lock", serverStats.getDur().getCommitsInWriteLock());
            printMetric(sb.toString() + "Early Commits", serverStats.getDur().getEarlyCommits());

        } else {
            logger.warn("No information on Dur available in db.serverStatus()");
        }
    }

    private void printCursorsStats(ServerStats serverStats) {
        if (serverStats.getCursors() != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(getServerStatsMetricPrefix()).append("Cursors").append(METRIC_SEPARATOR);
            printMetric(sb.toString() + "clientCursors_size", serverStats.getCursors().getClientCursors_size());
            printMetric(sb.toString() + "totalOpen", serverStats.getCursors().getTotalOpen());
            printMetric(sb.toString() + "pinned", serverStats.getCursors().getPinned());
            printMetric(sb.toString() + "totalNoTimeout", serverStats.getCursors().getTotalNoTimeout());
            printMetric(sb.toString() + "timedOut", serverStats.getCursors().getTimedOut());

        } else {
            logger.warn("No information on Cursors available in db.serverStatus()");
        }
    }

    /**
     * Prints the Connection Statistics
     *
     * @param serverStats
     */
    public void printConnectionStats(ServerStats serverStats) {
        if (serverStats.getConnections() != null) {
            printMetric(getServerStatsMetricPrefix() + "Connections|Current", serverStats.getConnections().getCurrent());

            printMetric(getServerStatsMetricPrefix() + "Connections|Available", serverStats.getConnections().getAvailable());
        } else {
            logger.warn("No information on Connections available in db.serverStatus()");
        }
    }

    /**
     * Prints the Memory Statistics
     *
     * @param serverStats
     */
    public void printMemoryStats(ServerStats serverStats) {
        if (serverStats.getMem() != null) {
            printMetric(getServerStatsMetricPrefix() + "Memory|Bits", serverStats.getMem().getBits());

            printMetric(getServerStatsMetricPrefix() + "Memory|Resident", serverStats.getMem().getResident());

            printMetric(getServerStatsMetricPrefix() + "Memory|Virtual", serverStats.getMem().getVirtual());

            printMetric(getServerStatsMetricPrefix() + "Memory|Mapped", serverStats.getMem().getMapped());

            printMetric(getServerStatsMetricPrefix() + "Memory|Mapped With Journal", serverStats.getMem().getMappedWithJournal());
        } else {
            logger.warn("No information on Memory available in db.serverStatus()");
        }
    }

    /**
     * Prints the Global Lock Statistics
     *
     * @param serverStats Server stats
     */
    public void printGlobalLocksStats(ServerStats serverStats) {
        if (serverStats.getGlobalLock() != null) {
            printMetric(getServerStatsMetricPrefix() + "Global Lock|Total Time", serverStats.getGlobalLock().getTotalTime());

            printMetric(getServerStatsMetricPrefix() + "Global Lock|Current Queue|Total", serverStats.getGlobalLock().getCurrentQueue().getTotal());

            printMetric(getServerStatsMetricPrefix() + "Global Lock|Current Queue|Readers", serverStats.getGlobalLock().getCurrentQueue().getReaders());

            printMetric(getServerStatsMetricPrefix() + "Global Lock|Current Queue|Writers", serverStats.getGlobalLock().getCurrentQueue().getWriters());

            printMetric(getServerStatsMetricPrefix() + "Global Lock|Active Clients|Total", serverStats.getGlobalLock().getActiveClients().getTotal());

            printMetric(getServerStatsMetricPrefix() + "Global Lock|Active Clients|Readers", serverStats.getGlobalLock().getActiveClients().getReaders());

            printMetric(getServerStatsMetricPrefix() + "Global Lock|Active Clients|Writers", serverStats.getGlobalLock().getActiveClients().getWriters());
        } else {
            logger.warn("No information on Global Lock available in db.serverStatus()");
        }
    }

    /**
     * Prints the Index Counter Statistics
     *
     * @param serverStats
     */
    public void printIndexCounterStats(ServerStats serverStats) {
        if (serverStats.getIndexCounters() != null) {
            printMetric(getServerStatsMetricPrefix() + "Index Counter|Accesses", serverStats.getIndexCounters().getAccesses());

            printMetric(getServerStatsMetricPrefix() + "Index Counter|Hits", serverStats.getIndexCounters().getHits());

            printMetric(getServerStatsMetricPrefix() + "Index Counter|Misses", serverStats.getIndexCounters().getMisses());

            printMetric(getServerStatsMetricPrefix() + "Index Counter|Resets", serverStats.getIndexCounters().getResets());
        } else {
            logger.warn("No information on Index Counter available in db.serverStatus()");
        }
    }

    /**
     * Prints the Background Flushing Statistics
     *
     * @param serverStats
     */
    public void printBackgroundFlushingStats(ServerStats serverStats) {
        if (serverStats.getBackgroundFlushing() != null) {
            printMetric(getServerStatsMetricPrefix() + "Background Flushing|Flushes", serverStats.getBackgroundFlushing().getFlushes());

            printMetric(getServerStatsMetricPrefix() + "Background Flushing|Total (ms)", serverStats.getBackgroundFlushing().getTotal_ms());

            printMetric(getServerStatsMetricPrefix() + "Background Flushing|Average (ms)", serverStats.getBackgroundFlushing().getAverage_ms());

            printMetric(getServerStatsMetricPrefix() + "Background Flushing|Last (ms)", serverStats.getBackgroundFlushing().getLast_ms());
        } else {
            logger.warn("No information on Background Flushing available in db.serverStatus()");
        }
    }

    /**
     * Prints the Network Statistics
     *
     * @param serverStats
     */
    public void printNetworkStats(ServerStats serverStats) {
        if (serverStats.getNetwork() != null) {
            printMetric(getServerStatsMetricPrefix() + "Network|Bytes In", serverStats.getNetwork().getBytesIn());

            printMetric(getServerStatsMetricPrefix() + "Network|Bytes Out", serverStats.getNetwork().getBytesOut());

            printMetric(getServerStatsMetricPrefix() + "Network|Number Requests", serverStats.getNetwork().getBytesIn());
        } else {
            logger.warn("No information on Network available in db.serverStatus()");
        }
    }

    /**
     * Prints the Operation Statistics
     *
     * @param serverStats
     */
    public void printOperationStats(ServerStats serverStats) {
        if (serverStats.getOpcounters() != null) {
            printMetric(getServerStatsMetricPrefix() + "Operations|Insert", serverStats.getOpcounters().getInsert());

            printMetric(getServerStatsMetricPrefix() + "Operations|Query", serverStats.getOpcounters().getQuery());

            printMetric(getServerStatsMetricPrefix() + "Operations|Update", serverStats.getOpcounters().getUpdate());

            printMetric(getServerStatsMetricPrefix() + "Operations|Delete", serverStats.getOpcounters().getDelete());

            printMetric(getServerStatsMetricPrefix() + "Operations|Get More", serverStats.getOpcounters().getGetmore());

            printMetric(getServerStatsMetricPrefix() + "Operations|Command", serverStats.getOpcounters().getCommand());
        } else {
            logger.warn("No information on Operations available in db.serverStatus()");
        }
    }

    /**
     * Prints Assert Statistics
     *
     * @param serverStats
     */
    public void printAssertStats(ServerStats serverStats) {
        if (serverStats.getAsserts() != null) {
            printMetric(getServerStatsMetricPrefix() + "Asserts|Regular", serverStats.getAsserts().getRegular());

            printMetric(getServerStatsMetricPrefix() + "Asserts|Warning", serverStats.getAsserts().getWarning());

            printMetric(getServerStatsMetricPrefix() + "Asserts|Message", serverStats.getAsserts().getMsg());

            printMetric(getServerStatsMetricPrefix() + "Asserts|User", serverStats.getAsserts().getWarning());

            printMetric(getServerStatsMetricPrefix() + "Asserts|Warning", serverStats.getAsserts().getWarning());

            printMetric(getServerStatsMetricPrefix() + "Asserts|Message", serverStats.getAsserts().getMsg());

            printMetric(getServerStatsMetricPrefix() + "Asserts|User", serverStats.getAsserts().getWarning());

            printMetric(getServerStatsMetricPrefix() + "Asserts|Rollover", serverStats.getAsserts().getRollovers());
        } else {
            logger.warn("No information on Asserts available in db.serverStatus()");
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

    private static boolean isNotEmpty(final String input) {
        return input != null && input.trim().length() > 0;
    }

    private ServerStats getServerStats(DB db) {
        ServerStats serverStats = new Gson().fromJson(db.command("serverStatus").toString().trim(), ServerStats.class);
        if (serverStats != null && !serverStats.getOk().toString().equals(OK_RESPONSE)) {
            logger.error("Server status: " + db.command("serverStatus"));
            logger.error("Error retrieving server status. Invalid permissions set for this user.DB = " + db.getName());
        }
        return serverStats;
    }

    public static String getImplementationVersion() {
        return MongoDBMonitor.class.getPackage().getImplementationTitle();
    }

}
