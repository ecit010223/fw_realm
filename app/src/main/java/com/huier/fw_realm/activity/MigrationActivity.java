package com.huier.fw_realm.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.huier.fw_realm.R;

import java.security.SecureRandom;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 *
 */
public class MigrationActivity extends AppCompatActivity {

    public static void entry(Context from){
        Intent intent = new Intent(from,MigrationActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_migration);
    }

    /**
     * Realm文件可以通过传递一个512位(64字节)的密钥参数给Realm.getInstance().encryptionKey()来加密存储在磁盘上。
     * 这保证了所有永久性存储在磁盘上的数据都是通过标准AES-256加密的,每次创建新的Realm实例的时候,都需要提供相同的密钥。
     */
    private void encryption(){
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .encryptionKey(key)
                .build();
//        Realm realm = Realm.getInstance(config);
    }

    /**
     * 如果没有旧Realm数据文件存在，那么迁移并不需要，在这种情况下，Realm会创建一个新的以.realm为后缀，基于
     * 新的对象模型的数据文件。在开发和调试过程中，假如你需要频繁改变数据模型，并且不介意损失旧数据，你可以直接
     * 删除.realm文件(这里包含所有的数据)而不用关心迁移的问题。这在你应用的开发早期阶段非常有用。
     */
    private void deleteMigration(){
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
    }

    private void basicMigration(){
        setContentView(R.layout.activity_migration);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .schemaVersion(2) // Must be bumped when the schema changes
                .migration(migration) // Migration to run instead of throwing an exception
                .build();
    }

    // Example migration adding a new class
    RealmMigration migration = new RealmMigration() {
        @Override
        public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
            // DynamicRealm exposes an editable schema
            RealmSchema schema = realm.getSchema();

            // Migrate to version 1: Add a new class
            if (oldVersion == 0) {
                schema.create("Person")
                        .addField("name", String.class)
                        .addField("age", int.class);
                oldVersion++;
            }

            // Migrate to version 2: Add a primary key + object references

            if (oldVersion == 1) {
                schema.get("Person")
                        .addField("id", long.class, FieldAttribute.PRIMARY_KEY)
                        .addRealmObjectField("favoriteDog", schema.get("Dog"))
                        .addRealmListField("dogs", schema.get("Dog"));
                oldVersion++;
            }
        }
    };
}
