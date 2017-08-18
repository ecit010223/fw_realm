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

public class DogSerializer implements JsonSerializer<Dog> {
    @Override
    public JsonElement serialize(Dog src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name",src.getName());
        jsonObject.addProperty("age",src.getAge());
        return jsonObject;
    }
}
