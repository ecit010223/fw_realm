package com.huier.fw_realm.model;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * 作者：张玉辉
 * 时间：2017/8/17.
 */

public class Counter extends RealmObject {
    public static final String FIELD_COUNT = "count";

    private static AtomicInteger INTEGER_COUNTER = new AtomicInteger(0);

    @PrimaryKey
    private int count;

    public int getCount() {
        return count;
    }

    public String getCountString() {
        return Integer.toString(count);
    }

    //  create() & delete() needs to be called inside a transaction.
    static void create(Realm realm) {
        create(realm, false);
    }

    static void create(Realm realm, boolean randomlyInsert) {
        Parent parent = realm.where(Parent.class).findFirst();
        RealmList<Counter> counters = parent.getCounterList();
        Counter counter = realm.createObject(Counter.class, increment());
        if (randomlyInsert && counters.size() > 0) {
            Random rand = new Random();
            counters.listIterator(rand.nextInt(counters.size())).add(counter);
        } else {
            counters.add(counter);
        }
    }

    static void delete(Realm realm, long id) {
        Counter counter = realm.where(Counter.class).equalTo(FIELD_COUNT, id).findFirst();
        // Otherwise it has been deleted already.
        if (counter != null) {
            counter.deleteFromRealm();
        }
    }

    private static int increment() {
        return INTEGER_COUNTER.getAndIncrement();
    }
}
