package com.huier.fw_realm.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.huier.fw_realm.R;
import com.huier.fw_realm.model.Dog;
import com.huier.fw_realm.model.Person;
import com.huier.fw_realm.model.User;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Realm中的所有读取（包括查询）操作都是延迟执行的，且数据绝不会被拷贝。
 */
public class QueryActivity extends AppCompatActivity implements View.OnClickListener {
    private Context mContext;
    private Realm mRealm;
    private Button btnBasicQuery;
    private Button btnLogicOperator;
    private RealmResults<User> mAsyncResult;

    public static void entry(Context from){
        Intent intent = new Intent(from,QueryActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);
        mContext = this;
        mRealm = Realm.getDefaultInstance();
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initView(){
        btnBasicQuery = (Button)findViewById(R.id.btn_basic_query);
        btnBasicQuery.setOnClickListener(this);
        btnLogicOperator = (Button)findViewById(R.id.btn_logic_operator);
        btnLogicOperator.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_basic_query:
                basicQuery();
                break;
            case R.id.btn_logic_operator:
                logicOperator();
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAsyncResult!=null){
            //步骤三：在退出Activity或者Fragment时移除监听器的注册以避免内存泄漏
            mAsyncResult.removeChangeListener(callback); // remove a particular listener
            // or
            mAsyncResult.removeChangeListeners(); // remove all registered listeners
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mRealm != null)
            mRealm.close();
    }

    /**
     * 异步查询
     * 可以在Looper线程中使用异步查询,异步查询需要使用Handler来传递查询结果,在没有Looper的线程中使用异步查询
     * 会导致IllegalStateException异常被抛出。
     */
    private void asynchronousQueries(){
        /**
         * 步骤一：创建异步查询
         * 这里的调用并不会阻塞,而是立即返回一个RealmResults<User>,这类似于标准Java中Future的概念,查询将
         * 会在后台线程中被执行,当其完成时,之前返回的RealmResults实例会被更新。
         * 如果希望当查询完成、RealmResults被更新时获得通知,可以注册一个RealmChangeListener,这个监听器会
         * 在RealmResults被更新时被调用(通常是在事务被提交后).
         */
        mAsyncResult = mRealm.where(User.class)
                .equalTo("name", "John")
                .or()
                .equalTo("name", "Peter")
                .findAllAsync();
        //同步查询返回的RealmResults实例的isLoaded方法会永远返回true
        if(mAsyncResult.isLoaded()){
            // Results are now available
        }
        //步骤二：注册回调
        mAsyncResult.addChangeListener(callback);

        /************************或：强制装载异步查询 *******************************/
        //可以选择性地等待异步查询完成，而这将会阻塞当前线程，使查询变成同步
        RealmResults<User> result = mRealm.where(User.class).findAllAsync();
        result.load(); // be careful, this will block the current thread until it returns
    }

    /** 删除 **/
    private void delete(){
        /************* 从查询结果中删除数据 ***************/
        // obtain the results of a query
        final RealmResults<Dog> results = mRealm.where(Dog.class).findAll();

        // All changes to data must happen in a transaction
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                // remove single match
                results.deleteFirstFromRealm();
                results.deleteLastFromRealm();

                // remove a single object
                Dog dog = results.get(5);
                dog.deleteFromRealm();

                // Delete all matches
                results.deleteAllFromRealm();
            }
        });
    }

    /** 迭代 */
    private void iteration(){
        //方式一
        RealmResults<User> results1 = mRealm.where(User.class).findAll();
        for (User u : results1) {
            // ... do something with the object ...
        }
        //方式二
        RealmResults<User> results2 = mRealm.where(User.class).findAll();
        for (int i = 0; i < results2.size(); i++) {
            User u = results2.get(i);
            // ... do something with the object ...
        }
        //RealmResults的自动更新会通过looper事件触发，但在事件到来之前，某些元素有可能不再满足查询条件或者其已被删除。
        final RealmResults<User> users1 = getUsers();
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                users1.get(0).deleteFromRealm(); // indirectly delete object
            }
        });

        for (User user : users1) {
            showUser(user); // Will crash for the deleted user
        }
        //为避免该问题，可以使用RealmResults的deleteFromRealm(int)方法：
        final RealmResults<User> users2 = getUsers();
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                users2.deleteFromRealm(0); // Delete and remove object directly
            }
        });

        for (User user : users2) {
            showUser(user); // Deleted user will not be shown
        }
    }

    /** 聚合 */
    private void aggregation(){
        RealmResults<User> results = mRealm.where(User.class).findAll();
        long sum = results.sum("age").longValue();
        long min = results.min("age").longValue();
        long max = results.max("age").longValue();
        double average = results.average("age");
        long matches = results.size();
    }

    /**
     * RealmResults是对其所包含数据的自动更新视图，这意味着它永远不需要被重新查询获取,数据对象的改变会立刻被反映到相应的查询结果。
     */
    private void autoUpdatingResults(){
        final RealmResults<Dog> puppies = mRealm.where(Dog.class).lessThan("age", 2).findAll();
        puppies.size(); // => 0

        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Dog dog = realm.createObject(Dog.class);
                dog.setName("Fido");
                dog.setAge(1);
            }
        });

        puppies.addChangeListener(new RealmChangeListener<RealmResults<Dog>>() {
            @Override
            public void onChange(RealmResults<Dog> results) {
                // results and puppies point are both up to date
                results.size(); // => 1
                puppies.size(); // => 1
            }
        });
    }

    /**
     * 链式查询
     * 因为查询结果并不会被复制，且在查询提交时并不会被执行，可以链式串起查询并逐步进行分类筛选
     */
    private void chain(){
        RealmResults<Person> teenagers = mRealm.where(Person.class)
                .between("age", 13, 20)
                .findAll();
        Person firstJohn = teenagers.where()
                .equalTo("name", "John")
                .findFirst();
        //也可以在子对象上使用链式查询
        //可以查询找出所有年龄在13和20之间的Person并且他至少拥有一个1岁的Dog
        RealmResults<Person> teensWithPups = mRealm.where(Person.class)
                .between("age", 13, 20)
                .equalTo("dogs.age", 1)
                .findAll();
        //请注意，查询链最终是建立在RealmResults上而非RealmQuery，如果在某存在的RealmQuery上添加更多的查询
        //条件，那么是在修改查询本身，而非查询链。
    }

    /**
     * 使用 distinct()来查找唯一值.
     * 只有数值型和字符串字段支持该查询条件,和排序类似,也可以针对多个字段使用该查询条件。
     */
    private void distinct(){
        RealmResults<Person> unique = mRealm.where(Person.class).distinct("name");
    }

    /** 当执行完查询获得结果后，可以对它进行排序 */
    private void sorting(){
        RealmResults<User> result = mRealm.where(User.class).findAll();
        // defaul Sort ascending
        result = result.sort("age");
        result = result.sort("age", Sort.DESCENDING);
    }

    /**
     * 每个查询条件都会被被隐式地被逻辑和（&）组合在一起，而逻辑或（or）需要显式地去执行or()。
     */
    private void logicOperator(){
        //可以将查询条件组合在一起，使用beginGroup()（相当于左括号）和endGroup()（相当于右括号）
        RealmResults<User> r = mRealm.where(User.class)
                .greaterThan("age", 10)  //implicit AND
                .beginGroup()
                .equalTo("name", "Peter")
                .or()
                .contains("name", "Jo")
                .endGroup()
                .findAll();
        /**
         * 可以用not()否定一个条件,该not()运算符可以与beginGroup()/endGroup()一起使用来否定子条件,例如想
         * 查找所有名字不为“Peter”或“Jo”的User对象：
         */
        RealmResults<User> r2 = mRealm.where(User.class)
                .not()
                .beginGroup()
                .equalTo("name", "Peter")
                .or()
                .contains("name", "Jo")
                .endGroup()
                .findAll();
        //针对如上特定的查询，in()会更加的方便
        RealmResults<User> r3 = mRealm.where(User.class)
                .not()
                .in("name", new String[]{"Peter", "Jo"})
                .findAll();
    }

    /**
     * 查询返回一个RealmResults实例，其中包含名叫John和Peter的用户.
     * findAllSorted()：会返回一个排序后的结果集
     * findAllAsync()：会在后台线程异步进行查询
     * 这些对象并不会被拷贝到集合中，也就是说你得到的是一个匹配对象引用的列表，你对匹配对象所有的操作都是直接施
     * 加于它的原始对象，RealmResults继承自Java的AbstractList，行为类似，例如通过index来访问其中的某个对象。
     * 当查询没有任何匹配时，返回的RealmResults对象将不会为null，取而代之的是它的size()方法将返回 0，修改或
     * 删除RealmResults中任何一个对象都必须在写入事务中完成。
     */
    private void basicQuery(){
        // Build the query looking at all users:
        RealmQuery<User> query = mRealm.where(User.class);
        // Add query conditions:
        query.equalTo("name", "John");
        query.or().equalTo("name", "Peter");
        // Execute the query:
        RealmResults<User> result1 = query.findAll();

        // Or alternatively do the same all at once (the "Fluent interface"):
        RealmResults<User> result2 = mRealm.where(User.class)
                .equalTo("name", "John")
                .or()
                .equalTo("name", "Peter")
                .findAll();
    }

    private RealmChangeListener callback = new RealmChangeListener<RealmResults<User>>() {
        @Override
        public void onChange(RealmResults<User> results) {
            // called once the query complete and on every update
        }
    };

    /**************************** 空帮助方法 *******************************/
    private RealmResults<User> getUsers(){
        return null;
    }

    private void showUser(User user){

    }
}
