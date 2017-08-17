package com.huier.fw_realm.model;

import io.realm.RealmModel;
import io.realm.annotations.RealmClass;

/**
 * 作者：张玉辉
 * 时间：2017/8/15.
 * 除直接继承于RealmObject来声明Realm数据模型之外，还可以通过实现RealmModel接口并添加@RealmClass修饰符来声明。
 * RealmObject的所有方法都有其相对应的静态方法:
    // With RealmObject
    user.isValid();
    user.addChangeListener(listener);

    // With RealmModel
    RealmObject.isValid(user);
    RealmObject.addChangeListener(user, listener)
 */
@RealmClass
public class UserRealmModel implements RealmModel {
    public String id;
}
