package com.huier.fw_realm.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.huier.fw_realm.R;
import com.huier.fw_realm.model.Dog;
import com.huier.fw_realm.model.DogLink;
import com.huier.fw_realm.model.DogPublic;
import com.huier.fw_realm.model.Person;
import com.huier.fw_realm.model.PersonLink;
import com.huier.fw_realm.model.User;

import io.realm.OrderedRealmCollectionSnapshot;
import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;

public class DocumentActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = DocumentActivity.class.getSimpleName();
    private Context mContext;
    private Realm mRealm;
    /** 异步事务 **/
    private RealmAsyncTask mRealmAsyncTask;

    public static void entry(Context from){
        Intent intent = new Intent(from,DocumentActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);
        mContext = this;
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Get a Realm instance for this thread
        mRealm = Realm.getDefaultInstance();
    }

    private void initView(){
        ((Button)findViewById(R.id.btn_write)).setOnClickListener(this);
        ((Button)findViewById(R.id.btn_doc_simple)).setOnClickListener(this);
        ((Button)findViewById(R.id.btn_custom_object)).setOnClickListener(this);
        ((Button)findViewById(R.id.btn_link_query)).setOnClickListener(this);
        ((Button)findViewById(R.id.btn_execute_transaction)).setOnClickListener(this);
        ((Button)findViewById(R.id.btn_transaction_async)).setOnClickListener(this);
        ((Button)findViewById(R.id.btn_snapshots)).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_write:
                writeData();
                break;
            case R.id.btn_doc_simple:
                docSimple();
                break;
            case R.id.btn_custom_object:
                customObject();
                break;
            case R.id.btn_link_query:
                linkQuery();
                break;
            case R.id.btn_execute_transaction:
                executeTransaction();
                break;
            case R.id.btn_transaction_async:
                transactionAsync();
                break;
            case R.id.btn_snapshots:
                snapshots();
                break;
        }
    }

    /**
     * 快照
     * 所有的Realm集合都会自动更新,它们总会被更新到最新的数据,这在大多数情况下是符合预期的,
     * 但是在遍历并且同时修改集合元素时，自动更新的特性会给你带来麻烦,例如：
     */
    private void snapshots(){
        Log.d(TAG,"snapshots");
        RealmResults<Person> guests = mRealm.where(Person.class).equalTo("invited", false).findAll();
        mRealm.beginTransaction();
        for (int i = 0; i<guests.size(); i++) {
            guests.get(i).setInvited(true);
        }
        mRealm.commitTransaction();
        /**
         * 这段代码预期通过一个简单循环来邀请所有的Guest对象，因为RealmResults在每次循环都会被更新，
         * 你会发现最终的运行结果是只有一半的Guest对象收到了邀请，当Guest对象收到邀请以后，它会被立即
         * 从集合中移除，因为它不再满足查询条件，集合的大小在此刻发生了改变，所以当i增加时，循环会错
         * 过一个集合元素。
         * 可以通过使用集合数据的快照来解决这个问题，集合快照保证其中的元素及其顺序不会改变，
         * 即使在元素被修改甚至删除的情况下。
         * Realm集合的迭代子(Iterator)会自动使用快照，也可以通过RealmResults和RealmList的createSnapshot()
         * 方法来创建一个快照，修改如下：
         */
        RealmResults<Person> guests2 = mRealm.where(Person.class).equalTo("invited", false).findAll();
        //Use an iterator to invite all guests
        mRealm.beginTransaction();
        for (Person guest : guests2) {
            guest.setInvited(true);
        }
        mRealm.commitTransaction();
        // Use a snapshot to invite all guests
        mRealm.beginTransaction();
        OrderedRealmCollectionSnapshot<Person> guestsSnapshot = guests.createSnapshot();
        for (int i = 0; i<guestsSnapshot.size(); i++) {
            guestsSnapshot.get(i).setInvited(true);
        }
        mRealm.commitTransaction();
    }

    /**
     * Realm的写操作针对的是整个字符串或byte数组属性而非该属性中的单独元素，假设你需要修改某byte数组中的第五
     * 个字符，操作如下
     */
    private void updateArrayItem(){
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
//                //先从realmObject中取出数组变量
//                byte[] bytes = realmObject.binary;
//                bytes[4] = 'a';
//                //修改完成后，将结果赋值给realmObject中的相应数组变量
//                realmObject.binary = bytes;
//                //原因是Realm的MVCC架构需要在确定旧版本数据可以被舍弃之前仍然保留旧版本的数据。
            }
        });
    }

    /**
     * 事务会相互阻塞其所在的线程，在后台线程中开启事务进行写入操作可以有效避免UI线程被阻塞。
     * 通过使用异步事务，Realm会在后台线程中进行写入操作，并在事务完成时将结果传回调用线程。
     * OnSuccess和OnError并不是必须重载的，重载了的回调函数会在事务成功或者失败时在被调用发生的线程执行,
     * 回调函数是通过Looper被执行的，所以在非Looper线程中只有空（null）回调函数被允许使用。
     * 异步事务调用会返回一个RealmAsyncTask对象。当你退出Activity或者Fragment时可以使用该对象取消异步事务,
     * 如果你在回调函数中更新UI，那么忘记取消异步事务可能会造成你的应用崩溃。
     */
    private void transactionAsync(){
        Log.d(TAG,"transactionAsync");
        mRealmAsyncTask = mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                User user = realm.createObject(User.class);
                user.setName("John");
                user.setAge(26);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Toast.makeText(mContext,"异步写入数据成功",Toast.LENGTH_LONG).show();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                Toast.makeText(mContext,"异步写入数据失败",Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 除手动调用mRealm.beginTransaction()、realm.commitTransaction()和realm.cancelTransaction()之外
     * 还可以使用mRealm.executeTransaction()方法，它会自动处理写入事物的开始和提交，
     * 并在错误发生时取消写入事物。
     */
    private void executeTransaction(){
        Log.d(TAG,"executeTransaction");
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                User user = realm.createObject(User.class);
                user.setName("execute transaction");
                user.setAge(24);
            }
        });
    }

    /**
     * 数据库数据初始化
     * Dog Table:
     * ID   Name    Color
     * A    Fido    Brown
     * B    Fluffy  Red
     * C    Fluffy  Brown
     * D    Fluffy  Yellow
     * Person Table:
     * ID   Name    Dogs
     * U1   Jane    A,B
     * U2   John    B,C,D
     */
    private void writeData(){
        Log.d(TAG,"writeData");
        mRealm.beginTransaction();
        DogLink dogA = mRealm.createObject(DogLink.class);
        dogA.setId("A");
        dogA.setName("Fido");
        dogA.setColor("Brown");
        DogLink dogB = mRealm.createObject(DogLink.class);
        dogB.setId("B");
        dogB.setName("Fluffy");
        dogB.setColor("Red");
        DogLink dogC = mRealm.createObject(DogLink.class);
        dogC.setId("C");
        dogC.setName("Fluffy");
        dogC.setColor("Brown");
        DogLink dogD = mRealm.createObject(DogLink.class);
        dogD.setId("D");
        dogD.setName("Fluffy");
        dogD.setColor("Yellow");

        PersonLink personU1 = mRealm.createObject(PersonLink.class);
        personU1.setId("U1");
        personU1.setName("Jane");
        personU1.getDogs().add(dogA);
        personU1.getDogs().add(dogB);

        PersonLink personU2 = mRealm.createObject(PersonLink.class);
        personU2.setId("U2");
        personU2.setName("John");
        personU2.getDogs().add(dogB);
        personU2.getDogs().add(dogC);
        personU2.getDogs().add(dogD);
        /**
         * 写入事务之间会互相阻塞，如果一个写入事务正在进行，那么其它的线程的写入事务就会阻塞它们所在的线程。
         * 同时在UI线程和后台线程使用写入事务有可能导致ANR问题,可以使用异步事务（async transactions）以避免阻塞UI线程。
         * Realm数据文件不会因为程序崩溃而损坏,当有异常在事务块中被抛出时，当前事务中所做出的数据修改会被丢弃。
         * 如果在该情况下程序需要继续运行，那么请调用cancelTransaction()来中止事务，或者使用executeTransaction()来执行事务。
         * 由但得益于Realm的MVCC架构，当正在进行一个写入事务时读取操作并不会被阻塞,这意味着，除非你需要从多个
         * 线程进行并发写入操作，否则，你可以尽量使用更大的写入事务来做更多的事情而不是使用多个更小的写入事务。
         * 当写入事务被提交到Realm时，该Realm的所有其他实例都将被通知，读入隐式事务将自动刷新你每个Realm对象。
         */
        mRealm.commitTransaction();
        //也可不提交，放弃事务提交
//        mRealm.cancelTransaction();
    }

    /** 关联查询 **/
    private void linkQuery(){
        Log.d(TAG,"linkQuery");
        Log.d(TAG,"------------------------1-----------------------------");
        /**
         * 所有至少含有一个color为Brown的PersonLink
         * 会查询出所有的PersonLink、所有的DogLink
         */
        RealmResults<PersonLink> persons = mRealm.where(PersonLink.class)
                .equalTo("dogs.color", "Brown")
                .findAll();
        showPersons(persons);

        Log.d(TAG,"-------------------------2----------------------------");
        /**
         * 列表中至少有一个DogLink对象满足查询条件
         * 会查询出所有的PersonLink、所有的DogLink
         */
        RealmResults<PersonLink> r1 = mRealm.where(PersonLink.class)
                .equalTo("dogs.name", "Fluffy")
                .findAll();
        showPersons(r1);

        Log.d(TAG,"--------------------------3---------------------------");
        /**
         * 建立在第一个的PersonLink结果（r1）以及r1的每个PersonLink的DogLink列表之上
         * 会查询出所有的PersonLink、所有的DogLink
         */
        RealmResults<PersonLink> r2 = r1.where()
                .equalTo("dogs.color", "Brown")
                .findAll();
        showPersons(r2);

        Log.d(TAG,"--------------------------4---------------------------");
        /**
         * 表示找到所有的PersonLink他至少有一个DogLink的名字为Fluffy,并且找到所有PersonLink它至少有
         * 一个DogLink的颜色是brown,然后返回这两个结果的交集.
         * 两个条件分别是equalTo("dogs.name", "Fluffy")和equalTo("dogs.color","Brown")。
         * u1和u2完全满足第一个条件 —— 称其c1集合,u1和u2也同时完全满足第二个条件 —— 称其c2
         * 集合,查询中的逻辑与即是c1与c2的交集,c1与c2的交集就是u1和u2.
         * 会查询出所有的PersonLink、所有的DogLink
         */
        RealmResults<PersonLink> r3 = mRealm.where(PersonLink.class)
                .equalTo("dogs.name", "Fluffy")
                .equalTo("dogs.color", "Brown")
                .findAll();
        showPersons(r3);

        Log.d(TAG,"--------------------------5---------------------------");
        /**
         * 找到所有的PersonLink它至少有一个DogLink的名字为Fluffy,然后在这个结果之上找到所有的
         * PersonLink它至少有一个DogLink的颜色为Brown，最后在之前的结果之上找到所有的PersonLink它
         * 至少有一个DogLink的颜色为Yellow。
         * RealmResults<PersonLink> r2a = mRealm.where(PersonLink.class).equalto("dogs.name","Fluffy").findAll(),
         * 它的结果包含u1和u2,然后r2b = r2a.where().equalTo("dogs.color", "brown").findAll();的结果仍然包含u1和
         * u2（两个PersonLink都有颜色为brown的DogLink）,最后的查询r2 = r2b.where().equalto("dogs.color", "yellow").findAll();
         * 结果只包含u2，因为只有u2同时有一个颜色为Brown的DogLink和一个颜色为Yellow的DogLink。
         * 会查询出U2
         */
        RealmResults<PersonLink> r4 = mRealm.where(PersonLink.class)
                .equalTo("dogs.name", "Fluffy")
                .findAll()
                .where()
                .equalTo("dogs.color", "Brown")
                .findAll()
                .where()
                .equalTo("dogs.color", "Yellow")
                .findAll();
        showPersons(r4);
    }

    /** 定制对象 **/
    private void customObject(){
        Log.d(TAG,"customObject");
        mRealm.executeTransaction(new Realm.Transaction(){
            @Override
            public void execute(Realm realm) {
                DogPublic dogPublic = realm.createObject(DogPublic.class);
                dogPublic.name="Fido";
                dogPublic.age=5;
            }
        });
    }

    /** 简单使用 **/
    private void docSimple(){
        Log.d(TAG,"docSimple");
        Dog unmanagedDog = new Dog();
        unmanagedDog.setName("Rex");
        unmanagedDog.setAge(1);
        // Query Realm for all dogs younger than 2 years old
        final RealmResults<Dog> puppies = mRealm.where(Dog.class).lessThan("age",2).findAll();
        Log.d(TAG,"out puppies.size():"+String.valueOf(puppies.size()));

        // Persist your data in a transaction
        mRealm.beginTransaction();
        // Persist unmanaged objects
        final Dog managedDog = mRealm.copyToRealm(unmanagedDog);
        // Create managed objects directly
        Person person = mRealm.createObject(Person.class,1L);
        person.getDogs().add(managedDog);
        mRealm.commitTransaction();

        // Listeners will be notified when data changes
        puppies.addChangeListener(new RealmChangeListener<RealmResults<Dog>>() {
            @Override
            public void onChange(RealmResults<Dog> element) {
                Log.d(TAG,"RealmChangeListener puppies.size():"+puppies.size());
            }
        });

        // Asynchronously update objects on a background thread
        mRealm.executeTransactionAsync(new Realm.Transaction(){
            @Override
            public void execute(Realm realm) {
                //此处不能使用外部创建的mRealm，Realm只能在他创建的线程中使用
                Dog dog = realm.where(Dog.class).equalTo("age",2).findFirst();
                if(dog!=null){
                    dog.setAge(3);
                }
            }
        },new Realm.Transaction.OnSuccess(){
            @Override
            public void onSuccess() {
                // Original queries and Realm objects are automatically updated.
                // 0 because there are no more puppies younger than 2 years old
                Log.d(TAG,"OnSuccess() puppies.size():"+puppies.size());
                // 3 the dogs age is updated
                Log.d(TAG,"OnSuccess() managedDog.getAge():"+managedDog.getAge());
            }
        });
    }

    /** 显示person列表中的数据 **/
    private void showPersons(RealmResults<PersonLink> persons){
        for(int i=0;i<persons.size();i++){
            PersonLink person = persons.get(i);
            Log.d(TAG,"person.id:"+person.getId()+",person.name:"+person.getName());
            RealmList<DogLink> dogs = person.getDogs();
            for(int j=0;j<dogs.size();j++){
                DogLink dog = dogs.get(j);
                Log.d(TAG,"dog.id:"+dog.getId()+",dog.name:"+dog.getName()+",dog.color:"+dog.getColor());
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelAsyncTask();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelAsyncTask();
        if(mRealm!=null)
            mRealm.close();
    }

    private void cancelAsyncTask(){
        if(mRealmAsyncTask!=null&&!mRealmAsyncTask.isCancelled()){
            mRealmAsyncTask.cancel();
        }
    }
}
