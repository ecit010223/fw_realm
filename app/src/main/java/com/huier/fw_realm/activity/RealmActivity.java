package com.huier.fw_realm.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.huier.fw_realm.R;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmResults;

/**
 * Realm.init(this)在Application的onCreate()执行，用于对Realm进行初始化，初始化操作只要进行一次。
 * 自动更新(Auto-Refresh)
 * 如果Realm实例存在于一个带有Looper的线程，那么这个Realm实例即具有自动更新的功能,这意味这如果发生了Realm数据库
 * 的变化，那么该Realm实例会在下一个事件循环(event loop)中自动更新,这个便捷的功能使你不必花费太多的精力就能保证
 * 的UI与数据的实时同步。
 * 如果Realm的实例所在线程没有绑定Looper，那么该实例不会被更新直到你手动调用waitForChange()方法,请注意，不更新
 * Realm以保持对旧数据的引用会造成而外的磁盘和内存开销,这也是为什么要在线程结束时调用close()关闭Realm实例的一个
 * 重要原因。
 * 如果你想确定当前Realm实例是否有自动更新功能，可以通过调用isAutoRefresh()方法查询。
 */
public class RealmActivity extends AppCompatActivity {
    private Context mContext;
    private Realm mRealm;

    public static void entry(Context from){
        Intent intent = new Intent(from,RealmActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm);
        mContext = this;
    }

    @Override
    protected void onStart() {
        super.onStart();
        /**
         * 该静态方法会在当前线程返回一个Realm实例，它对应了Context.getFilesDir()目录中的default.realm文件。
         * 该文件位于应用的可写根目录中,默认情况下的Realm使用内部存储(internal storage)，应用并不需要取得任
         * 何读写权限，一般来说，这个文件位于/data/data/<packagename>/files/。
         * 可以通过realm.getPath()来获得该Realm的绝对路径。
         * Realm的实例是线程单例化的，也就是说，在同一个线程内多次调用静态方法获得针对同路径的Realm，会返回同一个Realm实例。
         */
        mRealm = Realm.getDefaultInstance();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /**
         * Realm实现了Closeable接口以便与释放native内存和文件描述符，请务必在使用完毕后关闭Realm实例。Realm实
         * 例是基于引用计数的,也就是说假设你在同一个线程中调用了getInstance()两次，你需要同样调用close()两次以
         * 关闭该实例。举例来说，如果你需要实现Runnable，简单地在函数开始的时候调用getInstance()，在函数结束的
         * 时候调用close()即可！
         * 对于UI线程，你可以选择在onDestroy()方法内调用realm.close()。
         */
        if(mRealm != null)
            mRealm.close();
    }

    /** 简单配置 **/
    private void simpleRealmConfiguration(){
        //配置的Realm会被存储在Context.getFilesDir()并且命名为default.realm。
        RealmConfiguration config = new RealmConfiguration.Builder().build();
        // Use the config
        Realm realm = Realm.getInstance(config);
    }

    /** 典型配置 **/
    private void typicalRealmConfiguration(){
        // The RealmConfiguration is created using the builder pattern.
        // The Realm file will be located in Context.getFilesDir() with name "myrealm.realm"
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("myrealm.realm")
                .encryptionKey(getKey())
                .schemaVersion(42)
                .modules(new MySchemaModule())
                .migration(new MyMigration())
                .build();
        // Use the config
        Realm realm = Realm.getInstance(config);
    }

    /** 多个RealmConfiguration配置 **/
    private void multiRealmConfiguration(){
        RealmConfiguration myConfig = new RealmConfiguration.Builder()
                .name("myrealm.realm")
                .schemaVersion(2)
                .modules(new MySchemaModule())
                .build();

        RealmConfiguration otherConfig = new RealmConfiguration.Builder()
                .name("otherrealm.realm")
                .schemaVersion(5)
                .modules(new MySchemaModule())
                .build();

        Realm myRealm = Realm.getInstance(myConfig);
        Realm otherRealm = Realm.getInstance(otherConfig);
    }

    /**
     * 默认RealmConfiguration
     * RealmConfiguration可以保存为默认配置,通过在自定义的Application设置默认的Realm配置,可以使你在代码中
     * 的其他地方更加方便地创建针对该默认配置的Realm.
     * Application类中
        //The Realm file will be located in Context.getFilesDir() with name "default.realm"
        RealmConfiguration config = new RealmConfiguration.Builder().build();
        Realm.setDefaultConfiguration(config);
     */
    private void defaultRealmconfiguration(){
        //采用默认的配置
        Realm realm = Realm.getDefaultInstance();
        try {
            // ... Do something ...
        } finally {
            realm.close();
        }
    }

    /**
     * In-Memory Realm
     * 定义一个非持久化的、存在于内存中的Realm实例。
     * “内存中的”Realm在内存紧张的情况下仍有可能使用到磁盘存储，但是这些磁盘空间都会在Realm实例完全关闭的时候被释放。
     * 请注意使用同样的名称同时创建“内存中的”Realm 和常规的（持久化）Realm 是不允许的。
     * 当某个“内存中的”Realm的所有实例引用都被释放，该Realm下的数据也同时会被清除，建议在你的应用生命周期中保
     * 持对“内存中的”Realm实例的引用以避免非期望的数据丢失。
     */
    private void inMemoryRealm(){
        RealmConfiguration myConfig = new RealmConfiguration.Builder()
                .name("myrealm.realm")
                .inMemory()
                .build();
    }

    /**
     * Dynamic Realm
     * 对于普通的Realm来说,数据模型被定义成了RealmObject的子类,这样做保证了类型安全,但有时候某些数据模型在
     * 编译期是无法获得的,例如在处理数据迁移（migration）或CSV文件的时候。
     * DynamicRealm是普通Realm的一个变种,它可以在没有RealmObject子类的情况下操作Realm数据,其对数据的访问
     * 是基于字符串而非RealmObject的定义。
     * 创建Dynamic Realm使用与创建普通Realm相同的RealmConfiguration，但是它的创建过程会忽略对schema、
     * migration以及schema版本的检查。
     */
    private void dynamicRealm(){
        RealmConfiguration realmConfig = new RealmConfiguration.Builder().build();
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(realmConfig);

        // In a DynamicRealm all objects are DynamicRealmObjects
        DynamicRealmObject person = dynamicRealm.createObject("Person");

        // All fields are accessed using strings
        String name = person.getString("name");
        int age = person.getInt("age");

        // An underlying schema still exists, so accessing a field that does not exist will throw an exception
        person.getString("I don't exist");

        // Queries still work normally
        RealmResults<DynamicRealmObject> persons = dynamicRealm.where("Person")
                .equalTo("name", "John")
                .findAll();
    }

    /************************************* 模拟的方法与类 ***************************************/
    private byte[] getKey(){
        return null;
    }

    class MySchemaModule{

    }

    class MyMigration implements RealmMigration{

        @Override
        public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

        }
    }
}
