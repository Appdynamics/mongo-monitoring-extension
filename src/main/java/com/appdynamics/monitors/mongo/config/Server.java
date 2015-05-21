package com.appdynamics.monitors.mongo.config;

/**
 * Created by balakrishnav on 18/5/15.
 */
public class Server {
    private String host;
    private int port;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
