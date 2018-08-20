package com.huier.fw_realm.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.huier.fw_realm.R;
import com.huier.fw_realm.model.DogSerializer;
import com.huier.fw_realm.model.MainObject;
import com.huier.fw_realm.model.Person;
import com.huier.fw_realm.model.PersonSerializer;
import com.huier.fw_realm.model.RealmInt;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

public class Json2Activity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = Json2Activity.class.getSimpleName();
    private Realm mRealm;

    public static void entry(Context from){
        Intent intent = new Intent(from,Json2Activity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json_2);
        mRealm = Realm.getDefaultInstance();
        initView();
    }

    private void initView(){
        ((Button)findViewById(R.id.btn_realm_to_json)).setOnClickListener(this);
        ((Button)findViewById(R.id.btn_json_to_realm)).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_realm_to_json:
                serialRealmToJson();
                break;
            case R.id.btn_json_to_realm:
                serialJsonToRealm();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    private void serialJsonToRealm(){
        Type token = new TypeToken<RealmList<RealmInt>>(){}.getType();
        Gson gson = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .registerTypeAdapter(token, new TypeAdapter<RealmList<RealmInt>>() {
                    @Override
                    public void write(JsonWriter out, RealmList<RealmInt> value) throws IOException {

                    }

                    @Override
                    public RealmList<RealmInt> read(JsonReader in) throws IOException {
                        RealmList<RealmInt> list = new RealmList<RealmInt>();
                        in.beginArray();
                        while(in.hasNext()){
                            list.add(new RealmInt(in.nextInt()));
                        }
                        in.endArray();
                        return list;
                    }
                })
                .create();
        try {
            InputStream inputStream = getAssets().open("example.json");
            JsonElement json = new JsonParser().parse(new InputStreamReader(inputStream));
            List<MainObject> objects = gson.fromJson(json,new TypeToken<MainObject>(){}.getType());

            // Copy objects to Realm
            mRealm.beginTransaction();
            mRealm.copyToRealm(objects);
            mRealm.commitTransaction();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Serialize Realm objects to JSON using GSON **/
    private void serialRealmToJson(){
        try {
            Gson gson = new GsonBuilder()
                    .setExclusionStrategies(new ExclusionStrategy() {
                        @Override
                        public boolean shouldSkipField(FieldAttributes f) {
                            return f.getDeclaringClass().equals(RealmObject.class);
                            /**
                             * 当实现RealmObject的Person类中包含如下属性时GSON会抛出StackOverflowError：
                             * public class Person extends RealmObject {
                             *     @Ignore
                             *     Drawable avatar;
                             *     // other fields, etc
                             * }
                             * 当GSON序列化时，Drawable被读取并且造成了堆栈溢出。添加如下代码以避免类似问题：
                             * public boolean shouldSkipField(FieldAttributes f) {
                             *     return f.getDeclaringClass().equals(RealmObject.class) ||
                             *              f.getDeclaringClass().equals(Drawable.class);
                             * }
                             */
                        }

                        @Override
                        public boolean shouldSkipClass(Class<?> clazz) {
                            return false;
                        }
                    })
                    .registerTypeAdapter(Class.forName("io.realm.PersonRealmProxy"),new PersonSerializer())
                    .registerTypeAdapter(Class.forName("io.realm.DogRealmProxy"),new DogSerializer())
                    .create();
            String json = gson.toJson(mRealm.where(Person.class).findFirst());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
