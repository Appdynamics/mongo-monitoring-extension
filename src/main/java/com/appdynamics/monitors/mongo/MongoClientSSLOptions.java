package com.appdynamics.monitors.mongo;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.util.PathResolver;
import com.appdynamics.monitors.mongo.exception.MongoMonitorException;
import com.google.common.base.Strings;
import com.mongodb.MongoClientOptions;
import org.apache.commons.net.util.SSLContextUtils;
import org.apache.commons.net.util.TrustManagerUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.io.pem.PemObject;
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
import java.util.Map;

/**
 * Created by bhuvnesh.kumar on 3/19/19.
 */
public class MongoClientSSLOptions {
    private static final Logger logger = LoggerFactory.getLogger(MongoClientSSLOptions.class);

    public static MongoClientOptions getMongoClientSSLOptions(Map config) throws MongoMonitorException {
        MongoClientOptions clientOpts = null;
        Boolean ssl = false;
        if (config.get("ssl") != null) {
            ssl = (Boolean) config.get("ssl");
        }
        if (ssl) {
            if (config.get("pemFilePath") != null) {
                String filePath = config.get("pemFilePath").toString();
                if (!Strings.isNullOrEmpty(filePath)) {
                    try {
                        clientOpts = new MongoClientOptions.Builder().socketFactory(getSocketFactoryFromPEM(filePath)).build();
                    } catch (Exception e) {
                        logger.error("Error establishing ssl socket factory", e);
                        throw new MongoMonitorException("Error establishing ssl socket factory");
                    }
                } else {
                    String msg = "The argument pemFilePath is null or empty in config_old.yml";
                    logger.error(msg);
                    throw new MongoMonitorException(msg);
                }
            }
        }
        return clientOpts;
    }

    private static SSLSocketFactory getSocketFactoryFromPEM(String filePath) throws Exception {
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

    private static String getConfigFilename(String filename) {
        if (filename == null) {
            return "";
        }
        //for absolute paths
        if (new File(filename).exists()) {
            return filename;
        }
        //for relative paths
        File jarPath = PathResolver.resolveDirectory(ABaseMonitor.class);
        String configFileName = "";
        if (!Strings.isNullOrEmpty(filename)) {
            configFileName = jarPath + File.separator + filename;
        }
        return configFileName;
    }

}
