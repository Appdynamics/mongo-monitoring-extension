package com.appdynamics.monitors.mongo.config;

/**
 * Created by balakrishnav on 18/5/15.
 */
public class Database {
    private String dbName;
    private String username;
    private String password;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
