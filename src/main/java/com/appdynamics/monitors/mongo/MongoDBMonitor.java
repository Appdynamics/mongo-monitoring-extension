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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.AuthenticationException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

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

import com.appdynamics.extensions.crypto.CryptoUtil;
import com.appdynamics.monitors.mongo.json.ServerStats;
import com.google.gson.Gson;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.singularity.ee.agent.systemagent.SystemAgent;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;

public class MongoDBMonitor extends AManagedMonitor {
    private static final String ENCRYPTION_KEY = "encryption-key";

	private static final String PASSWORD_ENCRYPTED = "password-encrypted";

	private static String encryptionKey;

	private static final Logger logger = Logger.getLogger(MongoDBMonitor.class);

    private static final String ARG_HOST = "host";
    private static final String ARG_PORT = "port";
    private static final String ARG_USER = "username";
    private static final String ARG_PASS = "password";
    private static final String USE_SSL = "use-ssl";
    private static final String PEM_FILE_PATH = "pem-file";
    private static final String ARG_DB   = "db";   
    private static final String ADMIN_DB = "admin";
    private static final String ARG_XML_PATH = "properties-path";
    private static final String metricPathPrefix = "Custom Metrics|Mongo Server|";
    private static final String OK_RESPONSE = "1.0";

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
    	logger.info("Starting Mongo Monitoring Task");
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
                                String errorMessage = "Retrieving stats for collection " + collectionName + " of " + db.getName()+" failed";
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
            return new TaskOutput("Mongo DB Metric Upload Failed." + e.toString());
        } finally {
            if (mongoClient != null) {
                mongoClient.close();
            }
        }
    }
    
    private DBStats getDBStats(DB db) {
        DBStats dbStats = new Gson().fromJson(db.command("dbStats").toString().trim(), DBStats.class);
        if (dbStats != null && !dbStats.getOk().toString().equals(OK_RESPONSE)) {
            logger.error("Error retrieving db stats. Invalid permissions set for this user.DB= "+db.getName());
        }
        return dbStats;
    }

    private List<MongoCredential> getAdditionalDBDetails(Map<String, String> params) {

        String xmlPath = params.get(ARG_XML_PATH);
        List<MongoCredential> additionalDBCredentials = new ArrayList<MongoCredential>();

        if (isNotEmpty(xmlPath)) {
            File file = resolvePath(xmlPath);
            if(file.exists()){
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
                    logger.error("Cannot read '" + file.getAbsolutePath() + "'. Monitor is running without additional credentials");
                }
            } else{
                logger.error("Cannot read '" + xmlPath + "'. Monitor is running without additional credentials." +
                        "The absolute path is " + file.getAbsolutePath());
            }
        }
        return additionalDBCredentials;
    }

    private File resolvePath(String xmlPath) {
        File file = new File(xmlPath);
        if(file.exists()){
            return file;
        }else{
            return new File(installDir,xmlPath);
        }
    }

    private File resolveInstallDir() {
        File installDir=null;
        try{
            ProtectionDomain pd = SystemAgent.class.getProtectionDomain();
            if(pd!=null){
                CodeSource cs = pd.getCodeSource();
                if(cs!=null){
                    URL url = cs.getLocation();
                    String path = URLDecoder.decode(url.getFile(),"UTF-8");
                    File dir = new File(path).getParentFile();
                    if(dir.exists()){
                        installDir = dir;
                    } else{
                        logger.error("Install dir resolved to "+dir.getAbsolutePath()+", however it doesnt exist.");
                    }
                }

            }
        }catch (Exception e){
            logger.error("Error while resolving the Install Dir",e);
        }
        if(installDir!=null){
            logger.info("Install dir resolved to "+installDir.getAbsolutePath());
            return installDir;
        } else{
            File workDir = new File("");
            logger.info("Failed to resolve install dir, returning current work dir"+workDir.getAbsolutePath());
            return workDir;
        }
    }

    private DB connectToAdminDB(MongoCredential adminCredentials, MongoClientOptions options) {
        try {
        	if(options != null) {
        		mongoClient = new MongoClient(new ServerAddress(host, Integer.parseInt(port)), options);
        	} else {
        		mongoClient = new MongoClient(host, Integer.parseInt(port));
        	}
        } catch (UnknownHostException e) {
            String msg = String.format("Unable to connect to mongodb; host=%s, port=%s",host,port);
            logger.error(msg, e);
            throw new RuntimeException(msg, e);
        }

        DB db = mongoClient.getDB(adminCredentials.getSource());
        
        boolean authenticated = db.authenticate(adminCredentials.getUserName(), adminCredentials.getPassword());
        if(!authenticated) {
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
    	if(Boolean.valueOf(useSSL)) {
    		String filePath = params.get(PEM_FILE_PATH);
    		if(isNotEmpty(filePath)) {
    			try {
					clientOpts = new MongoClientOptions.Builder().socketFactory(getSocketFactoryFromPEM(filePath)).build();
				} catch (Exception e) {
					logger.error("Error establishing ssl socket factory", e);
					throw new RuntimeException("Error establishing ssl socket factory");
				}
    		} else {
    			String msg = "The argument "+ PEM_FILE_PATH + "is null or empty in monitor.xml";
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

    private String getPassword(Map<String,String> taskArguments){
        if(taskArguments.get(ARG_PASS) != null){
            return taskArguments.get(ARG_PASS);
        }
        else if(taskArguments.containsKey(PASSWORD_ENCRYPTED)) {
        	encryptionKey = taskArguments.get(ENCRYPTION_KEY);
        	String encryptedPassword = taskArguments.get(PASSWORD_ENCRYPTED);
        	return getDecryptedPassword(encryptionKey, encryptedPassword);
        }
        return "";
    }
    
    private String getDBPassword(Element credElem) {
    	if(credElem.elements().contains(ARG_PASS)) {
    		return credElem.elementText(ARG_PASS);
    	} else if (credElem.elements().contains(PASSWORD_ENCRYPTED)) {
    		String encryptedPassword = credElem.elementText(PASSWORD_ENCRYPTED);
    		return getDecryptedPassword(encryptionKey, encryptedPassword);
    	}
    	return "";
    }
    
    private String getDecryptedPassword(String encryptionKey, String encryptedPassword) {
    	Map<String,String> argsForDecryption = new HashMap<String, String>();
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
    public void printMetric(String metricName, double metricValue) {
        try {
            MetricWriter metricWriter = getMetricWriter(metricName,
                    MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                    MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
                    MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
            );

            metricWriter.printMetric(String.valueOf((long) metricValue));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void printServerStats(ServerStats serverStats) {
        printUpTimeStats(serverStats);
        printGlobalLocksStats(serverStats);
        printMemoryStats(serverStats);
        printConnectionStats(serverStats);
        printIndexCounterStats(serverStats);
        printBackgroundFlushingStats(serverStats);
        printNetworkStats(serverStats);
        printOperationStats(serverStats);
        printAssertStats(serverStats);
    }

    private void printDBStats(DBStats dbStats) {
    	if(dbStats != null) {
    		String dbStatsPath = getDBStatsMetricPrefix(dbStats.getDb());

            printMetric(dbStatsPath + "collections", dbStats.getCollections().doubleValue());
            printMetric(dbStatsPath + "objects", dbStats.getObjects().doubleValue());
            printMetric(dbStatsPath + "avgObjSize", dbStats.getAvgObjSize().doubleValue());
            printMetric(dbStatsPath + "dataSize", dbStats.getDataSize().doubleValue());
            printMetric(dbStatsPath + "storageSize", dbStats.getStorageSize().doubleValue());
            printMetric(dbStatsPath + "numExtents", dbStats.getNumExtents().doubleValue());
            printMetric(dbStatsPath + "indexes", dbStats.getIndexes().doubleValue());
            printMetric(dbStatsPath + "indexSize", dbStats.getIndexSize().doubleValue());
            printMetric(dbStatsPath + "fileSize", dbStats.getFileSize().doubleValue());
            printMetric(dbStatsPath + "nsSizeMB", dbStats.getNsSizeMB().doubleValue());
    	}
    }

    private void printCollectionStats(String dbName, CollectionStats collectionStats) {

        String collectionStatsPath = getCollectionStatsMetricPrefix(dbName, collectionStats.getNs());

        printMetric(collectionStatsPath + "count", collectionStats.getCount().doubleValue());
        printMetric(collectionStatsPath + "size", collectionStats.getSize().doubleValue());
        printMetric(collectionStatsPath + "storageSize", collectionStats.getStorageSize().doubleValue());
        printMetric(collectionStatsPath + "numExtents", collectionStats.getNumExtents().doubleValue());
        printMetric(collectionStatsPath + "nindexes", collectionStats.getNindexes().doubleValue());
        printMetric(collectionStatsPath + "lastExtentSize", collectionStats.getLastExtentSize().doubleValue());
        printMetric(collectionStatsPath + "paddingFactor", collectionStats.getPaddingFactor().doubleValue());
        printMetric(collectionStatsPath + "systemFlags", collectionStats.getSystemFlags().doubleValue());
        printMetric(collectionStatsPath + "userFlags", collectionStats.getUserFlags().doubleValue());
        printMetric(collectionStatsPath + "totalIndexSize", collectionStats.getTotalIndexSize().doubleValue());

        for (Map.Entry<String, Number> index : collectionStats.getIndexSizes().entrySet()) {
            printMetric(collectionStatsPath + "Index Size|" + index.getKey(), index.getValue().doubleValue());
        }

    }

    private void printUpTimeStats(ServerStats serverStats) {
        printMetric(getServerStatsMetricPrefix() + "UP Time (Milliseconds)", serverStats.getUptimeMillis().doubleValue()
        );
    }

    /**
     * Prints the Connection Statistics
     *
     * @param serverStats
     */
    public void printConnectionStats(ServerStats serverStats) {
        try {
            printMetric(getServerStatsMetricPrefix() + "Connections|Current", serverStats.getConnections().getCurrent().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Connections|Available", serverStats.getConnections().getAvailable().doubleValue());

        } catch (Exception e) {
            logger.warn("No information on Connections available");
        }
    }

    /**
     * Prints the Memory Statistics
     *
     * @param serverStats
     */
    public void printMemoryStats(ServerStats serverStats) {
        try {
            printMetric(getServerStatsMetricPrefix() + "Memory|Bits", serverStats.getMem().getBits().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Memory|Resident", serverStats.getMem().getResident().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Memory|Virtual", serverStats.getMem().getVirtual().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Memory|Mapped", serverStats.getMem().getMapped().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Memory|Mapped With Journal", serverStats.getMem().getMappedWithJournal().doubleValue());

        } catch (Exception e) {
            logger.warn("No information on Memory available");
        }
    }

    /**
     * Prints the Global Lock Statistics
     *
     * @param serverStats Server stats
     */
    public void printGlobalLocksStats(ServerStats serverStats) {
        try {
            printMetric(getServerStatsMetricPrefix() + "Global Lock|Total Time", serverStats.getGlobalLock().getTotalTime().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Global Lock|Current Queue|Total", serverStats.getGlobalLock().getCurrentQueue().getTotal().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Global Lock|Current Queue|Readers", serverStats.getGlobalLock().getCurrentQueue().getReaders().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Global Lock|Current Queue|Writers", serverStats.getGlobalLock().getCurrentQueue().getWriters().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Global Lock|Active Clients|Total", serverStats.getGlobalLock().getActiveClients().getTotal().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Global Lock|Active Clients|Readers", serverStats.getGlobalLock().getActiveClients().getReaders().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Global Lock|Active Clients|Writers", serverStats.getGlobalLock().getActiveClients().getWriters().doubleValue());

        } catch (Exception e) {
            logger.warn("No information on Global Lock available");
        }
    }

    /**
     * Prints the Index Counter Statistics
     *
     * @param serverStats
     */
    public void printIndexCounterStats(ServerStats serverStats) {
        try {
            printMetric(getServerStatsMetricPrefix() + "Index Counter|B-Tree|Accesses", serverStats.getIndexCounters().getBtree().getAccesses().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Index Counter|B-Tree|Hits", serverStats.getIndexCounters().getBtree().getHits().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Index Counter|B-Tree|Misses", serverStats.getIndexCounters().getBtree().getMisses().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Index Counter|B-Tree|Resets", serverStats.getIndexCounters().getBtree().getResets().doubleValue());

        } catch (Exception e) {
            logger.warn("No information on Index Counter available");
        }
    }

    /**
     * Prints the Background Flushing Statistics
     *
     * @param serverStats
     */
    public void printBackgroundFlushingStats(ServerStats serverStats) {
        try {
            printMetric(getServerStatsMetricPrefix() + "Background Flushing|Flushes", serverStats.getBackgroundFlushing().getFlushes().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Background Flushing|Total (ms)", serverStats.getBackgroundFlushing().getTotal_ms().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Background Flushing|Average (ms)", serverStats.getBackgroundFlushing().getAverage_ms().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Background Flushing|Last (ms)", serverStats.getBackgroundFlushing().getLast_ms().doubleValue());
        } catch (Exception e) {
            logger.warn("No information on Background Flushing available");
        }
    }

    /**
     * Prints the Network Statistics
     *
     * @param serverStats
     */
    public void printNetworkStats(ServerStats serverStats) {
        try {
            printMetric(getServerStatsMetricPrefix() + "Network|Bytes In", serverStats.getNetwork().getBytesIn().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Network|Bytes Out", serverStats.getNetwork().getBytesOut().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Network|Number Requests", serverStats.getNetwork().getBytesIn().doubleValue());
        } catch (Exception e) {
            logger.warn("No information on Network available");
        }
    }

    /**
     * Prints the Operation Statistics
     *
     * @param serverStats
     */
    public void printOperationStats(ServerStats serverStats) {
        try {
            printMetric(getServerStatsMetricPrefix() + "Operations|Insert", serverStats.getOpcounters().getInsert().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Operations|Query", serverStats.getOpcounters().getQuery().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Operations|Update", serverStats.getOpcounters().getUpdate().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Operations|Delete", serverStats.getOpcounters().getDelete().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Operations|Get More", serverStats.getOpcounters().getGetmore().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Operations|Command", serverStats.getOpcounters().getCommand().doubleValue());
        } catch (Exception e) {
            logger.warn("No information on Operations available");
        }
    }

    /**
     * Prints Assert Statistics
     *
     * @param serverStats
     */
    public void printAssertStats(ServerStats serverStats) {
        try {
            printMetric(getServerStatsMetricPrefix() + "Asserts|Regular", serverStats.getAsserts().getRegular().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Asserts|Warning", serverStats.getAsserts().getWarning().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Asserts|Message", serverStats.getAsserts().getMsg().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Asserts|User", serverStats.getAsserts().getWarning().doubleValue());

            printMetric(getServerStatsMetricPrefix() + "Asserts|Rollover", serverStats.getAsserts().getRollovers().doubleValue());
        } catch (Exception e) {
            logger.warn("No information on Asserts available");
        }
    }

    /**
     * Metric Prefix
     *
     * @return String
     */
    private String getServerStatsMetricPrefix() {
        return metricPathPrefix + "Server Stats|";
    }

    private String getDBStatsMetricPrefix(String dbName) {
        return metricPathPrefix + "DB Stats|" + dbName + "|";
    }

    private String getCollectionStatsMetricPrefix(String dbName, String collectionName) {
        return getDBStatsMetricPrefix(dbName) + "Collection Stats|" + collectionName + "|";
    }

    private static boolean isNotEmpty(final String input) {
        return input != null && input.trim().length() > 0;
    }

    private ServerStats getServerStats(DB db) {
        ServerStats serverStats = new Gson().fromJson(db.command("serverStatus").toString().trim(), ServerStats.class);
        if (serverStats != null && !serverStats.getOk().toString().equals(OK_RESPONSE)) {
            logger.error("Server status: " + db.command("serverStatus"));
            logger.error("Error retrieving server status. Invalid permissions set for this user.DB = "+db.getName());
        }
        return serverStats;
    }

    public static String getImplementationVersion() {
        return MongoDBMonitor.class.getPackage().getImplementationTitle();
    }
}
