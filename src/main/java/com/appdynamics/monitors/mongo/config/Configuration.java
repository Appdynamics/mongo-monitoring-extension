package com.appdynamics.monitors.mongo.config;

/**
 * Created by balakrishnav on 18/5/15.
 */
public class Configuration {

    private Server[] servers;
    private String passwordEncryptionKey;
    private String adminDBUsername;
    private String adminDBPassword;

    private boolean ssl;
    private String pemFilePath;
    private String metricPathPrefix;

    public Server[] getServers() {
        return servers;
    }

    public void setServers(Server[] servers) {
        this.servers = servers;
    }

    public String getPasswordEncryptionKey() {
        return passwordEncryptionKey;
    }

    public void setPasswordEncryptionKey(String passwordEncryptionKey) {
        this.passwordEncryptionKey = passwordEncryptionKey;
    }

    public String getAdminDBUsername() {
        return adminDBUsername;
    }

    public void setAdminDBUsername(String adminDBUsername) {
        this.adminDBUsername = adminDBUsername;
    }

    public String getAdminDBPassword() {
        return adminDBPassword;
    }

    public void setAdminDBPassword(String adminDBPassword) {
        this.adminDBPassword = adminDBPassword;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public String getPemFilePath() {
        return pemFilePath;
    }

    public void setPemFilePath(String pemFilePath) {
        this.pemFilePath = pemFilePath;
    }

    public String getMetricPathPrefix() {
        return metricPathPrefix;
    }

    public void setMetricPathPrefix(String metricPathPrefix) {
        this.metricPathPrefix = metricPathPrefix;
    }
}
