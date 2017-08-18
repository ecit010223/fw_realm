package com.huier.fw_realm.model;

import io.realm.RealmObject;

/**
 * 作者：张玉辉
 * 时间：2017/8/18.
 */

public class RealmInt extends RealmObject {
    private int val;

    public RealmInt() {
    }

    public RealmInt(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }
}
