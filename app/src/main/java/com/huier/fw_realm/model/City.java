package com.huier.fw_realm.model;

import io.realm.RealmObject;

/**
 * 作者：张玉辉
 * 时间：2017/8/17.
 */

public class City extends RealmObject {
    private String city;
    private int id;

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
}
