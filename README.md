### 控制 Realm 实例的生命周期
RealmObjects和RealmResults在访问其引用数据时都是懒加载的。因为这个原因，如果你仍然需要访问其中的Realm对象或者查询结果时，请不要关闭你的Realm实例。
但从另一角度来说，Realm实例以及针对该实例创建的Realm对象和查询结果都会占用一定的资源，所以在你不再需要该实例的时候也应该即时调用Realm.close()来释放它们。
为了避免不必要的Realm数据连接的打开和关闭，Realm内部有一个基于引用计数的缓存。这表示在同一线程内调用Realm.getDefaultInstance()多次是基本没有开销的，
并且底层资源会在所有实例都关闭的时候才被释放。
一个合理的选择是通过UI组件的创建和销毁来控制Realm的生命周期。以下代码展示了如何在包含RecycleView的Activity和Fragment中控制Realm的生命周期。
在两个例子中，Realm实例和RecycleView适配器均在UI组件初始化时创建，在相应的销毁函数中关闭。请注意即使这里Activty.onDestory()没有被调用，
Realm.close()未被执行，Realm的数据仍然是安全的。
当然，假如你的多个Fragment需要访问同样一份数据，那么你完全可以用Activity来控制Realm实例的生命中期而不是单独在每个Fragement中来控制它。
```
// Setup Realm in your Application
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }
}

// onCreate()/onDestroy() overlap when switching between activities.
// Activity2.onCreate() will be called before Activity1.onDestroy()
// so the call to getDefaultInstance in Activity2 will be fast.
public class MyActivity extends Activity {
    private Realm realm;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realm = Realm.getDefaultInstance();

        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setAdapter(
            new MyRecyclerViewAdapter(this, realm.where(MyModel.class).findAllAsync()));

        // ...
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}

// Use onCreateView()/onDestroyView() for Fragments.
// Note that if the db is large, getting the Realm instance may, briefly, block rendering.
// In that case it may be preferable to manage the Realm instance and RecyclerView from
// onStart/onStop instead. Returning a view, immediately, from onCreateView allows the
// fragment frame to be rendered while the instance is initialized and the view loaded.
public class MyFragment extends Fragment {
    private Realm realm;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        realm = Realm.getDefaultInstance();

        View root = inflater.inflate(R.layout.fragment_view, container, false);

        recyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(
            new MyRecyclerViewAdapter(getActivity(), realm.where(MyModel.class).findAllAsync()));

        // ...

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
    }
}
```
### 重用 RealmResults 和 RealmObjects
在UI线程和其它拥有Looper的线程中，RealmObject和RealmResults都会在Realm数据改变时自动刷新。这意味着你不需要在RealmChangeListener中重新获取这些对象。
它们已经被更新并且准备好被重绘在屏幕上了。
```
public class MyActivity extends Activity {

    private Realm realm;
    private RealmResults<Person> allPersons;
    private RealmChangeListener realmListener = new RealmChangeListener<Realm>() {
        @Override
        public void onChange(Realm realm) {
            // Just redraw the views. `allPersons` already contain the
            // latest data.
            invalidateView();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        realm.addRealmChangeListener(listener);
        allPerson = realm.where(Person.class).findAll(); // Create the "live" query result
        setupViews(); // Initial setup of views
        invalidateView(); // Redraw views with data
    }

    // ...
}
```