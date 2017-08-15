package com.huier.fw_realm.model;

import io.realm.RealmObject;

/**
 * 作者：张玉辉
 * 时间：2017/8/15.
 */
public class Dog extends RealmObject {
    private String name;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
