package com.huier.fw_realm.model;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * 作者：张玉辉
 * 时间：2017/8/15.
 */

public class PersonLink extends RealmObject {
    private String id;
    private String name;
    private RealmList<DogLink> dogs;

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

    public RealmList<DogLink> getDogs() {
        return dogs;
    }

    public void setDogs(RealmList<DogLink> dogs) {
        this.dogs = dogs;
    }
}
