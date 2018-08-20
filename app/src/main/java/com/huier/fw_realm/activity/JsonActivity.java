package com.huier.fw_realm.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.huier.fw_realm.R;
import com.huier.fw_realm.model.City;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import io.realm.Realm;

/**
 * 可以直接将JSON对象添加到Realm中,这些JSON对象可以是一个String、一个JSONObject或者是一个InputStream,
 * Realm会忽略JSON中存在但未定义在Realm模型类里的字段,单独对象可以通过Realm.createObjectFromJson()
 * 添加,对象列表可以通过Realm.createAllFromJson()添加。
 * Realm 解析 JSON 时遵循如下规则:
 * 1）对于包含空值(null)的JSON创建对象:对于可为空值的属性,设置其值为null;对于不可为空值的属性抛出异常;
 * 2）使用包含空值(null)的JSON更新对象:对于可为空值的属性,设置其值为null;对于不可为空值的属性抛出异常;
 * 3）使用不包含对应属性的JSON：该属性保持不变。
 */
public class JsonActivity extends AppCompatActivity {
    private static final String TAG = JsonActivity.class.getSimpleName();
    private Realm mRealm;

    public static void entry(Context from){
        Intent intent = new Intent(from,JsonActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json);
        mRealm = Realm.getDefaultInstance();
    }

    private void parseJson(){
        // Insert from a string
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createObjectFromJson(City.class, "{ city: \"Copenhagen\", id: 1 }");
            }
        });

        // Insert multiple items using an InputStream
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                try {
                    InputStream is = new FileInputStream(new File("path_to_file"));
                    realm.createAllFromJson(City.class, is);
                } catch (IOException e) {
                    throw new RuntimeException();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mRealm != null){
            mRealm.close();
        }
    }
}
