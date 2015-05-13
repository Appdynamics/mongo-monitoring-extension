package com.appdynamics.monitors.mongo.json.server;

/**
 * Created by balakrishnav on 13/5/15.
 */
public class OpcountersRepl {
    private Number insert;
    private Number query;
    private Number update;
    private Number delete;
    private Number getmore;
    private Number command;

    public Number getInsert() {
        return insert;
    }

    public void setInsert(Number insert) {
        this.insert = insert;
    }

    public Number getQuery() {
        return query;
    }

    public void setQuery(Number query) {
        this.query = query;
    }

    public Number getUpdate() {
        return update;
    }

    public void setUpdate(Number update) {
        this.update = update;
    }

    public Number getDelete() {
        return delete;
    }

    public void setDelete(Number delete) {
        this.delete = delete;
    }

    public Number getGetmore() {
        return getmore;
    }

    public void setGetmore(Number getmore) {
        this.getmore = getmore;
    }

    public Number getCommand() {
        return command;
    }

    public void setCommand(Number command) {
        this.command = command;
    }
}
