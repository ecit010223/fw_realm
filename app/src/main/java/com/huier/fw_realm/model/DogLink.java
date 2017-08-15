package com.huier.fw_realm.model;

import io.realm.RealmObject;

/**
 * 作者：张玉辉
 * 时间：2017/8/15.
 */

public class DogLink extends RealmObject {
    private String id;
    private String name;
    private String color;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
