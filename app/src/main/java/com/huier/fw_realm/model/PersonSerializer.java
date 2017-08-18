package com.huier.fw_realm.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * 作者：张玉辉
 * 时间：2017/8/18.
 */

public class PersonSerializer implements JsonSerializer<Person> {
    @Override
    public JsonElement serialize(Person src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id",src.getId());
        jsonObject.addProperty("name",src.getName());
        jsonObject.addProperty("age",src.getAge());
        jsonObject.add("dogs",context.serialize(src.getDogs()));
        jsonObject.add("favoriteDog",context.serialize(src.getFavoriteDog()));
        return jsonObject;
    }
}
