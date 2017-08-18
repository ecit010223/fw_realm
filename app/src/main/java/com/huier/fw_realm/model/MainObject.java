package com.huier.fw_realm.model;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * 作者：张玉辉
 * 时间：2017/8/18.
 */

public class MainObject extends RealmObject {
    private String name;
    private RealmList<RealmInt> ints;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RealmList<RealmInt> getInts() {
        return ints;
    }

    public void setInts(RealmList<RealmInt> ints) {
        this.ints = ints;
    }
}
