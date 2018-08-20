package com.huier.fw_realm.model;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.Required;

/**
 * 作者：张玉辉
 * 时间：2017/8/15.
 * Realm数据模型不仅仅支持private成员变量，你还可以使用public、protected以及自定义的成员方法。
 * 字段类型：
 * Realm支持以下字段类型：boolean、byte、short、int、long、float、double、String、Date和byte[]，
 * 整数类型short、int和long都被映射到Realm内的相同类型（实际上为long），再者，还可以使用RealmObject
 * 的子类和 RealmList<? extends RealmObject>来表示模型关系。
 * Realm对象中还可以声明包装类型（boxed type）属性，包括：Boolean、Byte、Short、Integer、Long、Float
 * 和Double，通过使用包装类型，可以使这些属性存取空值（null）。
 */

public class User extends RealmObject {
    /**
     * @Index 会为字段增加搜索索引,这会导致插入速度变慢,同时数据文件体积有所增加,但能加速查询,
     * 因此建议仅在需要加速查询时才添加索引,目前仅支持索引的属性类型包括：String、byte、short、
     * int、long、boolean和Date.
     */
    @Index
    private long id;
    /**
     * @Required 表示强制禁止空值
     * 只有Boolean、Byte、Short、Integer、Long、Float、Double、String、byte[]以及Date可以被@Required
     * 修饰。在其它类型属性上使用@Required修饰会导致编译失败，基本数据类型（primitive types）不需要
     * 使用注解 @Required，因为他们本身就不可为空，RealmObject属性永远可以为空。
     */
    @Required
    private String name;
    private int age;
    /**
     * @Ignore 意味着一个字段不应该被保存到 Realm
     */
    @Ignore
    private int sessionId;

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

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }
}
