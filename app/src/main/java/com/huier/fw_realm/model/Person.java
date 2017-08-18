package com.huier.fw_realm.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * 作者：张玉辉
 * 时间：2017/8/15.
 */

public class Person extends RealmObject {
    /**
     * @PrimaryKey 用来定义字段为主键，该字段类型必须为字符串（String）或整数（short、int 或 long）以及它们的包
     * 装类型（Short、Int 或 Long），不可以存在多个主键，使用支持索引的属性类型作为主键同时意味着为该字段建立索引。
     * 当创建Realm对象时,所有字段会被设置为默认值,为了避免与具有相同主键的另一个对象冲突，建议创建一个unmanaged
     * 对象，为字段的赋值，然后用copyToRealm()方法将该对象复制到 Realm。
     * 主键的存在意味着可以使用copyToRealmOrUpdate()方法，它会用此主键尝试寻找一个已存在的对象，如果对象存在，
     * 就更新该对象；反之，它会创建一个新的对象。当copyToRealmOrUpdate()的调用对象没有主键时，会抛出异常。
     * realm.createObject()会返回一个所有字段被设置为默认值的新对象,如果该模型类存在主键,那么有可能返回对象的
     * 主键的默认值与其它已存在的对象冲突,建议创建一个非托管（unmanaged）Realm 对象，并给其主键赋值，
     * 然后调用copyToRealm()来避免冲突。
     * 字符串(String)和包装类型(Short、Int或Long)的主键可以被赋予空值(null),除非它们同时被@Required修饰。
     */
    @PrimaryKey
    private long id;
    private String name;
    private String age;
    // Declare one-to-many relationships
    private RealmList<Dog> dogs;
    private boolean invited;
    private Dog favoriteDog;

    public Person(){
    }

    public Person(long id,String name){
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public RealmList<Dog> getDogs() {
        return dogs;
    }

    public void setDogs(RealmList<Dog> dogs) {
        this.dogs = dogs;
    }

    public boolean isInvited() {
        return invited;
    }

    public void setInvited(boolean invited) {
        this.invited = invited;
    }

    public Dog getFavoriteDog() {
        return favoriteDog;
    }

    public void setFavoriteDog(Dog favoriteDog) {
        this.favoriteDog = favoriteDog;
    }
}
