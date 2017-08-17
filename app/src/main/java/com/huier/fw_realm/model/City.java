package com.huier.fw_realm.model;

import io.realm.RealmObject;

/**
 * 作者：张玉辉
 * 时间：2017/8/17.
 */

public class City extends RealmObject {
    private String city;
    private int id;
    private String name;
    private long votes;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getVotes() {
        return votes;
    }

    public void setVotes(long votes) {
        this.votes = votes;
    }
}
