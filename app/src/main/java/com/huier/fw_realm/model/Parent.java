package com.huier.fw_realm.model;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * 作者：张玉辉
 * 时间：2017/8/17.
 */

public class Parent extends RealmObject {
    @SuppressWarnings("unused")
    private RealmList<Counter> counterList;

    public RealmList<Counter> getCounterList() {
        return counterList;
    }
}
