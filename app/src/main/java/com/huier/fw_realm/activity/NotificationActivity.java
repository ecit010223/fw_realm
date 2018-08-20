package com.huier.fw_realm.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.huier.fw_realm.R;
import com.huier.fw_realm.model.Dog;
import com.huier.fw_realm.model.Person;

import io.realm.ObjectChangeSet;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObjectChangeListener;
import io.realm.RealmResults;

/**
 * 可以通过针对Realm、RealmResults或者RealmList注册监听器来获取数据更新通知。
 * 可以通过调用removeChangeListener()、removeAllChangeListeners()来停止通知,侦听器所注册的相应对象
 * 被垃圾回收时，通知也会停止,请在所期望的通知生命周期内，保持对侦听器注册的相应对象的强引用。
 * 通知回调函数永远只会在注册通知的对应线程上进行调用，并且该线程需要一个正在运行的Looper。
 * 如果相关的写操作发生在一个另外的线程之上，那么侦听器会在事务提交后被异步调用。
 * 如果相关写操作发生在相同线程之上，那么侦听器会在事务提交时被同步调用。如果当本线程中的Realm数据不
 * 是最新版本，且最新版本的数据改变与当前注册的监听器相关，那么当事务开始(beginTransaction())的时候，
 * 侦听器也会被同步调用。
 * 这些情况里侦听器会被beginTransaction()和commitTransaction()同步调用，这意味着这时在回调函数中开
 * 启事务(beginTransaction())会由于事务嵌套的原因抛出异常。假如你的应用架构有可能触发这种情况，
 * 请通过Realm.isTransaction()来判断是否当前Realm实例的事务状态。
 * 因为异步的通知是通过looper事件来传递的，队列中的其他无关事件有可能因为通知送达延迟。
 * 这意味着多个事务中改变的数据不一定总是通过多个通知来送达侦听器，它们有可能被合并为一个通知送达。
 */
public class NotificationActivity extends AppCompatActivity {
    private static final String TAG = NotificationActivity.class.getSimpleName();

    private Realm mRealm;
    private RealmChangeListener mRealmChangeListener;
    private OrderedRealmCollectionChangeListener<RealmResults<Person>> mOrderedRealmCollectionChangeListener;
    private RealmObjectChangeListener<Dog> mRealmObjectChangeListener;

    public static void entry(Context from){
        Intent intent = new Intent(from,NotificationActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        mRealm = Realm.getDefaultInstance();
    }

    /**
     * 当后台线程向Realm添加数据，你的UI线程或者其它线程可以添加一个监听器来获取数据改变的通知,
     * 监听器在Realm数据改变的时候会被触发。
     */
    private void realmNotifications(){
        mRealmChangeListener = new RealmChangeListener() {
            @Override
            public void onChange(Object element) {
                // ... do something with the updates (UI, etc.) ...
            }
        };
        mRealm.addChangeListener(mRealmChangeListener);
    }

    /**
     * 集合通知与Realm通知不同,它会包含针对那些集合数据改变的信息,这些信息会指明从上次通知到达以来,
     * 集合中的插入、删除以及改变的元素索引。
     * 对于异步查询(findAllAsync())来说通知第一次到达时改变集合为空,表明了这是异步查询完成后的第一
     * 次通知,之后的通知则包含相应的集合改变信息。
     * 可以通过传递给监听器的OrderedCollectionChangeSet对象来获取集合中删除(deletions)、插入
     * (insertions)和修改(changes)的元素索引。
     * 插入(insertions)和删除(deletions)指有元素被添加进或移除出集合从而引起集合元素构成的改变,
     * 这些改变有可能在创建或者删除相关Realm对象时发生,对于RealmResults来说，当修改了某个Realm对
     * 象的属性,而这个属性修改会导致查询结果的不同,这时集合的插入和修改通知也会被触发。
     * 修改(changes)通知会当集合中的某对象的属性改变时被触发,当然这个属性的改变需能保证该元素仍然
     * 满足查询条件从而使其仍然存在于集合当中。
     */
    private void collectionNotifications(){
        final RealmResults<Person> puppies = mRealm.where(Person.class).findAll();
        /**
         * 假设在监听Person对象集合，会在如下情况收到通知：
         * 1）集合中某Person对象姓名改变；
         * 2）在dogs中添加或者移除了一个dog对象。
         * 这使得我们可以分别控制UI不同部分的动画和更新而不需要在数据改变时刷新整个UI。
         */
        mOrderedRealmCollectionChangeListener = new OrderedRealmCollectionChangeListener<RealmResults<Person>>() {
            @Override
            public void onChange(RealmResults<Person> collection, OrderedCollectionChangeSet changeSet) {
                // `null`  means the async query returns the first time.
                if (changeSet == null) {
                    notifyDataSetChanged();
                    return;
                }
                // For deletions, the adapter has to be notified in reverse order.
                OrderedCollectionChangeSet.Range[] deletions = changeSet.getDeletionRanges();
                for (int i = deletions.length - 1; i >= 0; i--) {
                    OrderedCollectionChangeSet.Range range = deletions[i];
                    notifyItemRangeRemoved(range.startIndex, range.length);
                }

                OrderedCollectionChangeSet.Range[] insertions = changeSet.getInsertionRanges();
                for (OrderedCollectionChangeSet.Range range : insertions) {
                    notifyItemRangeInserted(range.startIndex, range.length);
                }

                OrderedCollectionChangeSet.Range[] modifications = changeSet.getChangeRanges();
                for (OrderedCollectionChangeSet.Range range : modifications) {
                    notifyItemRangeChanged(range.startIndex, range.length);
                }
            }
        };
        puppies.addChangeListener(mOrderedRealmCollectionChangeListener);
    }

    /**
     * 可以为某个Realm对象注册监听器以获得针对对象属性修改的细粒度通知。
     * 只有托管RealmObject可以注册监听器。
     * 可以通过监听器传递的ObjectChangeSet来获取对象属性改变的详细信息，包括哪些属性被修改以及该对象
     * 已被删除。在监听对象被删除时ObjectChangeSet.isDeleted()会返回true。
     * ObjectChangeSet.getChangedFields()会返回哪些字段有所改变,也可以通过
     * ObjectChangeSet.isFieldChanged()来判断一个指定字段是否改变。
     */
    private void ObjectNotifications(){
        final RealmResults<Dog> puppies = mRealm.where(Dog.class).findAll();
        mRealmObjectChangeListener = new RealmObjectChangeListener<Dog>() {
            @Override
            public void onChange(Dog object, ObjectChangeSet changeSet) {
                if (changeSet.isDeleted()) {
                    Log.i(TAG, "The dog was deleted");
                    return;
                }

                for (String fieldName : changeSet.getChangedFields()) {
                    Log.i(TAG, "Field " + fieldName + " was changed.");

                }
            }
        };
//        mRealm.addChangeListener(mRealmObjectChangeListener);
    }

        

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mRealm!=null){
            // Close the Realm instance.
            mRealm.close();
            if(mRealmChangeListener !=null){
                // Remove the listener.
                mRealm.removeChangeListener(mRealmChangeListener);
            }
        }
    }

    /**************************** 无实际意义的模拟方法 *************************/
    private void notifyDataSetChanged(){

    }
    private void notifyItemRangeRemoved(int startIndex,int length){

    }
    private void notifyItemRangeInserted(int startIndex,int length){

    }
    private void notifyItemRangeChanged(int startIndex, int length){

    }
}
