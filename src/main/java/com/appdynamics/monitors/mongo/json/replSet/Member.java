package com.appdynamics.monitors.mongo.json.replSet;

/**
 * Created by balakrishnav on 20/5/15.
 */
public class Member {

    private String name;
    private int health;
    private int state;
    private int uptime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getUptime() {
        return uptime;
    }

    public void setUptime(int uptime) {
        this.uptime = uptime;
    }
}
