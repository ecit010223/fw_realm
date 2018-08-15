package com.huier.fw_realm.activity;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.BaseAdapter;

import com.huier.fw_realm.R;
import com.huier.fw_realm.model.User;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * 可以实时在不同线程中读取和写入Realm对象,不用担心其它线程会对同一对象进行操作,只需要在改变对象时使用事务,在另
 * 一线程中指向同一对象的数据会被即时更新(更新会在下一次事件循环时进行)。
 * 唯一局限是不能随意跨线程传递Realm对象,如果在另一线程使用同一对象,请在哪个线程使用查询重新获得该对象,请谨记所
 * 有的Realm对象都会在不同线程中保持更新,Realm会在数据改变时通知你。
 * 跨线程使用Realm
 * 请谨记：Realm、RealmObject和RealmResults实例都不可以跨线程使用,但是可以使用异步查询和异步事务来将部分操作
 * 放入后台线程进行，待完成时调用线程被通知以获取结果。
 * 当需要跨线程访问同一部分数据时，只需简单地在该线程重新获取一个Realm实例(例如：Realm.getInstance(RealmConfiguration
 * config)或是其他类似方法)，然后通过这个Realm实例来查询获得你需要的数据,查询获得的对象会映射到Realm中的相同数据，由此
 * 方法获得对象在其线程中任何地方都可读写！
 * 如下实例：
 * 假设我们的应用要展示一个用户列表。我们在一个后台线程中(一个安卓 IntentService)从远端获取新用户并将它们存储到
 * Realm中,但后台线程存储新用户时，UI线程中的数据会被自动更新。UI线程会通过RealmChangeListener得到通知，这时UI
 * 线程应刷新相应的控件,因为Realm的自动更新特性，无需重新查询数据。
 */
public class ThreadActivity extends AppCompatActivity {
    private static final String TAG = ThreadActivity.class.getSimpleName();
    private Realm mRealm;
    private RealmChangeListener<RealmResults<User>> mChangeListener;
    private BaseAdapter listAdapter;

    public static void entry(Context from){
        Intent intent = new Intent(from,ThreadActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread);
        // ... boilerplate omitted for brevity
        mRealm = Realm.getDefaultInstance();
        // get all the customers
        RealmResults<User> customers = mRealm.where(User.class).findAllAsync();
        // ... build a list adapter and set it to the ListView/RecyclerView/etc

        // set up a Realm change listener
        mChangeListener = new RealmChangeListener<RealmResults<User>>() {
            @Override
            public void onChange(RealmResults<User> results) {
                // This is called anytime the Realm database changes on any thread.
                // Please note, change listeners only work on Looper threads.
                // For non-looper threads, you manually have to use Realm.waitForChange() instead.
                listAdapter.notifyDataSetChanged(); // Update the UI
            }
        };
        // Tell Realm to notify our listener when the customers results
        // have changed (items added, removed, updated, anything of the sort).
        customers.addChangeListener(mChangeListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mRealm != null){
            mRealm.close();
        }
    }

    // In a background service, in another thread
    public class PollingService extends IntentService {
        public PollingService(String name){
            super(name);
        }
        @Override
        public void onHandleIntent(Intent intent) {
            Realm realm = Realm.getDefaultInstance();
            try {
                String json = null;
                // go do some network calls/etc and get some data and stuff it into a 'json' var
//                json = userApi.getCustomers();
                realm.beginTransaction();
                realm.createObjectFromJson(User.class, json); // Save a bunch of new Customer objects
                realm.commitTransaction();
                // At this point, the data in the UI thread is already up to date.
                // ...
            } finally {
                realm.close();
            }
        }
        // ...
    }
}
