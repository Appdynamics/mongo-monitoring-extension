package com.appdynamics.monitors.mongo.json.replSet;

/**
 * Created by balakrishnav on 20/5/15.
 */
public class ReplicaStats {

    private String set;
    private Member[] members;
    private String ok;

    public String getSet() {
        return set;
    }

    public void setSet(String set) {
        this.set = set;
    }

    public Member[] getMembers() {
        return members;
    }

    public void setMembers(Member[] members) {
        this.members = members;
    }

    public String getOk() {
        return ok;
    }

    public void setOk(String ok) {
        this.ok = ok;
    }
}
