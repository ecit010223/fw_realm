package com.huier.fw_realm.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.huier.fw_realm.Constants;
import com.huier.fw_realm.R;
import com.huier.fw_realm.model.Dog;
import com.huier.fw_realm.model.DogLink;
import com.huier.fw_realm.model.Person;
import com.huier.fw_realm.model.DogPublic;
import com.huier.fw_realm.model.PersonLink;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;

public class DocumentActivity extends AppCompatActivity implements View.OnClickListener {
    private Realm mRealm;
    private Button btnDocSimple,btnCustomObject,btnLinkQuery;

    public static void entry(Context from){
        Intent intent = new Intent(from,DocumentActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);
        // Get a Realm instance for this thread
        mRealm = Realm.getDefaultInstance();
        initView();
        initData();
    }

    private void initView(){
        btnDocSimple = (Button)findViewById(R.id.btn_doc_simple);
        btnDocSimple.setOnClickListener(this);
        btnCustomObject = (Button)findViewById(R.id.btn_custom_object);
        btnCustomObject.setOnClickListener(this);
        btnLinkQuery = (Button)findViewById(R.id.btn_link_query);
        btnLinkQuery.setOnClickListener(this);
    }

    /** 数据库数据初始化 **/
    private void initData(){
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
        mRealm.commitTransaction();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_doc_simple:
                docSimple();
                break;
            case R.id.btn_custom_object:
                customObject();
                break;
            case R.id.btn_link_query:
                linkQuery();
                break;
        }
    }

    /** 关联查询 **/
    private void linkQuery(){
        // persons => [U1,U2]
        RealmResults<PersonLink> persons = mRealm.where(PersonLink.class)
                .equalTo("dogs.color", "Brown")
                .findAll();
        showPersons(persons);

        // r1 => [U1,U2]
        RealmResults<PersonLink> r1 = mRealm.where(PersonLink.class)
                .equalTo("dogs.name", "Fluffy")
                .findAll();
        showPersons(r1);
        // r2 => [U1,U2]
        RealmResults<PersonLink> r2 = r1.where()
                .equalTo("dogs.color", "Brown")
                .findAll();
        showPersons(r2);


    }

    /** 定制对象 **/
    private void customObject(){
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
        Dog unmanagedDog = new Dog();
        unmanagedDog.setName("Rex");
        unmanagedDog.setAge(1);
        // Query Realm for all dogs younger than 2 years old
        final RealmResults<Dog> puppies = mRealm.where(Dog.class).lessThan("age",2).findAll();
        Log.d(Constants.TAG,"out puppies.size():"+String.valueOf(puppies.size()));

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
                Log.d(Constants.TAG,"RealmChangeListener puppies.size():"+puppies.size());
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
                Log.d(Constants.TAG,"OnSuccess() puppies.size():"+puppies.size());
                // 3 the dogs age is updated
                Log.d(Constants.TAG,"OnSuccess() managedDog.getAge():"+managedDog.getAge());
            }
        });
    }

    /** 显示person列表中的数据 **/
    private void showPersons(RealmResults<PersonLink> persons){
        for(int i=0;i<persons.size();i++){
            PersonLink person = persons.get(i);
            Log.d(Constants.TAG,"id:"+person.getId()+",name:"+person.getName());
            RealmList<DogLink> dogs = person.getDogs();
            for(int j=0;j<dogs.size();j++){
                DogLink dog = dogs.get(i);
                Log.d(Constants.TAG,"id:"+dog.getId()+",name:"+dog.getName()+",color"+dog.getColor());
            }
        }
    }
}
