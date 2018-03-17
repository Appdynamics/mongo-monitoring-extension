/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.monitors.mongo.config;

import java.util.List;

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

    private List<String> serverStatusExcludeMetricFields;
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

    public List<String> getServerStatusExcludeMetricFields() {
        return serverStatusExcludeMetricFields;
    }

    public void setServerStatusExcludeMetricFields(List<String> serverStatusExcludeMetricFields) {
        this.serverStatusExcludeMetricFields = serverStatusExcludeMetricFields;
    }
}
